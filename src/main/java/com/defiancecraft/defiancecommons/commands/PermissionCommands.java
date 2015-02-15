package com.defiancecraft.defiancecommons.commands;

import java.util.IllegalFormatException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.defiancecraft.defiancecommons.DefianceCommons;
import com.defiancecraft.defiancecommons.api.User;
import com.defiancecraft.defiancecommons.database.Database;
import com.defiancecraft.defiancecommons.permissions.PermissionManager;
import com.defiancecraft.defiancecommons.util.RegexUtils;

public class PermissionCommands {

	// <user> <group> Pattern
	private static final Pattern PAT_USERGROUP = Pattern.compile("^([a-zA-Z0-9_]{1,16}) ([^ ]+)$");
	
	/**
	 * Convenice method to attempt to send a
	 * message to UUID `u`. This will work if
	 * `u` isn't null, and the player with that
	 * UUID is online. Colour codes will also
	 * be translated from ampersands (&).
	 * 
	 * @param u UUID of Player - can be null
	 * @param msg Message to send, can be String.format compatible
	 * @param log Whether to log to the console
	 * @param formatArguments Any arguments to pass to String.format; optional
	 */
	private static void trySend(UUID u, String msg, boolean log, Object... formatArguments) {

		// Format & translate colour codes
		try {
			msg = String.format(msg, formatArguments);
		} catch (IllegalFormatException e) {}
		
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		
		// Send message if player is online
		if (u != null && Bukkit.getPlayer(u) != null)
			Bukkit.getPlayer(u).sendMessage(msg);
		
		if (log)
			Bukkit.getLogger().info(ChatColor.stripColor(msg));
		
	}
	
	/*
	 * Command:    /perm
	 * Permission: defiancecraft.perm.help
	 */
	public static boolean help(CommandSender sender, String[] args) {
		
		sender.sendMessage(
			"§9§lPermissions Help\n" +
			"§3§oUser Commands:\n" +
			"§b- /perm addgroup <user> <group>\n" +
			"§b- /perm remgroup <user> <group>\n" +
			"§b- /perm setuserprefix <user> [prefix]\n" +
			"§b- /perm setusersuffix <user> [suffix]\n" +
			"§3§oGroup Commands:\n" +
			"§b- /perm reload\n" +
			"§b- /perm addperm <group> <perm>\n" +
			"§b- /perm remperm <group> <perm>\n" +
			"§b- /perm setgroupprefix <group> [prefix]\n" +
			"§b- /perm setgroupsuffix <group> [suffix]\n" +
			"§b- /perm setpriority <group> [priority]\n"
		);
	
		return true;
		
	}
	
	/*
	 * Command:    /perm addgroup <user> <group>
	 * Permission: defiancecraft.perm.addgroup
	 */
	public static boolean addGroup(CommandSender sender, String[] args) {
		
		String arguments = String.join(" ", args);
		Matcher matcher  = PAT_USERGROUP.matcher(arguments);
		
		final String user      = RegexUtils.getGroup(1, matcher).trim();
		final String group     = RegexUtils.getGroup(2, matcher).trim();
		final UUID senderUUID  = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		
		if (user.isEmpty() || group.isEmpty()) {
			sender.sendMessage("Usage: /perm addgroup <user> <group>");
			return true;
		}
		
		Database.getExecutorService().submit(() -> {
			
			User u = User.findByNameOrCreate(user);
			if (u == null) {
				trySend(senderUUID, "&cCould not find user with name '%s'", true, user);
				return;
			}
			
			boolean added = u.addGroup(group);
			if (!added) {
				trySend(senderUUID, "&cCould not add group to user; database error", true);
				return;
			}
			
			// Update player's perms if they are online
			Player target = Bukkit.getPlayer(u.getDBU().getUUID());
			PermissionManager pm = DefianceCommons.getPermissionManager();
			
			if (target != null)
				pm.updatePlayer(target);
				
			trySend(senderUUID, "&aSuccessfully added group '%s' to user '%s'.", true, group, user);
			
		});
		
		sender.sendMessage("Adding group...");
		return true;
		
	}
	
	/*
	 * Command:    /perm remgroup <user> <group>
	 * Permission: defiancecraft.perm.remgroup
	 */
	public static boolean remGroup(CommandSender sender, String[] args) {
		
		String arguments = String.join(" ", args);
		Matcher matcher  = PAT_USERGROUP.matcher(arguments);
		
		final String user  = RegexUtils.getGroup(1, matcher);
		final String group = RegexUtils.getGroup(2, matcher);
		final UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		
		if (user.isEmpty() || group.isEmpty()) {
			sender.sendMessage("Usage: /perm remgroup <user> <group>");
			return true;
		}
		
		Database.getExecutorService().submit(() -> {
			
			User u = User.findByName(user);
			if (u == null) {
				trySend(senderUUID, "&cCould not find user with name '%s'", true, user);
				return;
			}
			
			boolean success = u.removeGroup(group);
			if (!success) {
				trySend(senderUUID, "&cFailed to remove group '%s' from user '%s'", true, group, user);
				return;
			}
			
			// Update player's perms if they are online
			Player target = Bukkit.getPlayer(u.getDBU().getUUID());
			PermissionManager pm = DefianceCommons.getPermissionManager();
			
			if (target != null)
				pm.updatePlayer(target);
				
			trySend(senderUUID, "&aSuccessfully removed group '%s' from user '%s'.", true, group, user);
			
		});
		
		return true;
		
	}
	
	/*
	 * Command:    /perm reload
	 * Permission: defiancecraft.perm.reload
	 */
	public static boolean reload(CommandSender sender, String[] args) {
	
		PermissionManager pm = DefianceCommons.getPermissionManager();
		pm.reloadConfig();
		pm.reload();
		
		return true;
		
	}
	
	/*
	public static boolean setUserPrefix(CommandSender sender, String[] args) {}
	public static boolean setUserSuffix(CommandSender sender, String[] args) {}
	
	public static boolean reloadGroups(CommandSender sender, String[] args) {}
	public static boolean createGroup(CommandSender sender, String[] args) {}
	public static boolean addPerm(CommandSender sender, String[] args) {}
	public static boolean remPerm(CommandSender sender, String[] args) {}
	public static boolean setGroupPrefix(CommandSender sender, String[] args) {}
	public static boolean setGroupSuffix(CommandSender sender, String[] args) {}
	public static boolean setGroupPriority(CommandSender sender, String[] args) {}
	public static boolean clearGroupPrefix(CommandSender sender, String[] args) {}
	public static boolean clearGroupSuffix(CommandSender sender, String[] args) {}
	*/
	
}