package com.defiancecraft.core.commands;

import java.util.IllegalFormatException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.defiancecraft.core.DefianceCore;
import com.defiancecraft.core.api.User;
import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.permissions.PermissionConfig;
import com.defiancecraft.core.permissions.PermissionManager;
import com.defiancecraft.core.permissions.PermissionConfig.Group;
import com.defiancecraft.core.util.RegexUtils;

public class PermissionCommands {

	// <user> <group> Pattern
	private static final Pattern PAT_USERGROUP = Pattern.compile("^([a-zA-Z0-9_]{1,16}) ([^ ]+)$");
	
	// <user> [prefix] or <user> [suffix]
	private static final Pattern PAT_USERMETA = Pattern.compile("^([a-zA-Z0-9_]{1,16})(?: (.*))?$");
	
	// <group>
	private static final Pattern PAT_GROUP = Pattern.compile("^([^ ]+)$");
	
	// <group> <perm>
	private static final Pattern PAT_GROUPPERM = Pattern.compile("^([^ ]+) ([^ ]+)$");
	
	// <group> [prefix] or <group> [suffix]
	private static final Pattern PAT_GROUPMETA = Pattern.compile("^([^ ]+)(?: (.*))?$");
	
	// <group> <priority>
	private static final Pattern PAT_GROUPPRIORITY = Pattern.compile("^([^ ]+) (\\d+)$"); 
	
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
		
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
			"&9&lPermissions Help\n" +
			"&3&oUser Commands:\n" +
			"&b- /perm addgroup <user> <group>\n" +
			"&b- /perm remgroup <user> <group>\n" +
			"&b- /perm setuserprefix <user> [prefix]\n" +
			"&b- /perm setusersuffix <user> [suffix]\n" +
			"&3&oGroup Commands:\n" +
			"&b- /perm reload\n" +
			"&b- /perm creategroup <group>\n" +
			"&b- /perm addperm <group> <perm>\n" +
			"&b- /perm remperm <group> <perm>\n" +
			"&b- /perm setgroupprefix <group> [prefix]\n" +
			"&b- /perm setgroupsuffix <group> [suffix]\n" +
			"&b- /perm setpriority <group> [priority]\n"
		));
	
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
			PermissionManager pm = DefianceCore.getPermissionManager();
			
			if (target != null)
				pm.updatePlayer(target, true);
				
			trySend(senderUUID, "&aSuccessfully added group '%s' to user '%s'.", true, group, user);
			
		});
		
		sender.sendMessage(ChatColor.GRAY + "Adding group...");
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
			PermissionManager pm = DefianceCore.getPermissionManager();
			
			if (target != null)
				pm.updatePlayer(target, true);
				
			trySend(senderUUID, "&aSuccessfully removed group '%s' from user '%s'.", true, group, user);
			
		});
		
		sender.sendMessage(ChatColor.GRAY + "Removing group...");
		return true;
		
	}

	// This is a generic method to set either the user's
	// suffix or prefix via commands, as these do nearly the exact
	// same thing. Instead of directly registering this method, a
	// lambda function calls this with the appropriate `prefix`
	// argument (whether it should set prefix - false implies that
	// suffix will be set).
	public static boolean setUserMeta(CommandSender sender, String[] args, boolean prefix) {

		String arguments = String.join(" ", args);
		Matcher matcher  = PAT_USERMETA.matcher(arguments);
		
		final String user   = RegexUtils.getGroup(1, matcher);
		final String meta   = RegexUtils.getGroup(2, matcher);
		final UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		
		// Friendly name for prefix/suffix, for reference in messages
		final String friendly = prefix ? "prefix" : "suffix";
		
		if (user.isEmpty()) {
			sender.sendMessage(String.format("Usage: /perm setuser%s <user> [prefix]", friendly));
			return true;
		}
		
		Database.getExecutorService().submit(() -> {
		
			User u = User.findByNameOrCreate(user);
			if (u == null) {
				trySend(senderUUID, "&cCould not find user with name '%s'", true, user);
				return;
			}
			
			boolean success = prefix ? u.setPrefix(meta) : u.setSuffix(meta);
			if (!success) {
				trySend(senderUUID, "&cFailed to set %s '%s' for user '%s'.", true, friendly, meta, user);
				return;
			}
			
			// Update player's metadata if they are online
			Player target = Bukkit.getPlayer(u.getDBU().getUUID());
			PermissionManager pm = DefianceCore.getPermissionManager();
			
			if (target != null)
				pm.updateMetadata(target);
			
			trySend(senderUUID, "&aSuccessfully updated %s for user '%s'.", true, friendly, user);
		
		});
		
		sender.sendMessage(String.format(ChatColor.GRAY + "Setting user's %s", friendly));
		return true;
		
	}

	/*
	 * Command:    /perm creategroup <group>
	 * Permission: defiancecraft.perm.creategroup
	 */
	public static boolean createGroup(CommandSender sender, String[] args) {
		
		String arguments = String.join(" ", args);
		Matcher matcher  = PAT_GROUP.matcher(arguments);
		String groupName = RegexUtils.getGroup(1, matcher);
		
		if (groupName.isEmpty()) {
			sender.sendMessage("Usage: /perm creategroup <group>");
			return true;
		}
		
		PermissionManager pm = DefianceCore.getPermissionManager();
		if (pm.getConfig().getGroup(groupName) != null) {
			sender.sendMessage(ChatColor.RED + "Group already exists.");
			return true;
		}
		
		Group g = new PermissionConfig.Group(groupName);
		
		pm.getConfig().groups.add(g);
		pm.saveConfig();
		
		sender.sendMessage(String.format(ChatColor.GREEN + "Created group %s", groupName));
		return true;
		
	}

	/*
	 * Command:    /perm addperm <group> <perm>
	 * Permission: defiancecraft.perm.addperm
	 */
	public static boolean addPerm(CommandSender sender, String[] args) {
		
		String arguments = String.join(" ", args);
		Matcher matcher  = PAT_GROUPPERM.matcher(arguments);
		String groupName = RegexUtils.getGroup(1, matcher);
		String perm      = RegexUtils.getGroup(2, matcher);
		
		if (groupName.isEmpty() || perm.isEmpty()) {
			sender.sendMessage("Usage: /perm addperm <group> <perm>");
			return true;
		}
		
		PermissionManager pm = DefianceCore.getPermissionManager();
		boolean success = pm.getConfig().addPermission(groupName, perm);
		
		if (!success) {
			sender.sendMessage(String.format(ChatColor.RED + "Could not find group '%s'", groupName));
			return true;
		}
	
		sender.sendMessage(ChatColor.GREEN + "Successfully added permission. Run /perm reload to apply this new permission to players.");
		pm.saveConfig();
		
		return true;
		
	}
	
	/*
	 * Command:    /perm remperm <group> <perm>
	 * Permission: defiancecraft.perm.remperm
	 */
	public static boolean remPerm(CommandSender sender, String[] args) {
		
		String arguments = String.join(" ", args);
		Matcher matcher  = PAT_GROUPPERM.matcher(arguments);
		String groupName = RegexUtils.getGroup(1, matcher);
		String perm      = RegexUtils.getGroup(2, matcher);
		
		if (groupName.isEmpty() || perm.isEmpty()) {
			sender.sendMessage("Usage: /perm remperm <group> <perm>");
			return true;
		}
		
		PermissionManager pm = DefianceCore.getPermissionManager();
		boolean success = pm.getConfig().removePermission(groupName, perm);
		
		if (!success) {
			sender.sendMessage(String.format(ChatColor.RED + "Could not find group '%s'", groupName));
			return true;
		}

		sender.sendMessage(ChatColor.GREEN + "Successfully removed permission. Run /perm reload to remove this permission from online players who have the group.");
		pm.saveConfig();
		
		return true;
		
	}
	
	// @see #setUserMeta(CommandSender, String[] boolean)
	public static boolean setGroupMeta(CommandSender sender, String[] args, boolean prefix) {
		
		String arguments = String.join(" ", args);
		Matcher matcher  = PAT_GROUPMETA.matcher(arguments);
		String groupName = RegexUtils.getGroup(1, matcher);
		String meta      = RegexUtils.getGroup(2, matcher);
		
		// Friendly name for prefix/suffix, for reference in messages
		String friendly  = prefix ? "prefix" : "suffix";
		
		if (groupName.isEmpty()) {
			sender.sendMessage(String.format("Usage: /perm set%1$s <group> [%1$s]", friendly));
			return true;
		}

		PermissionManager pm = DefianceCore.getPermissionManager();
		boolean success = prefix ? pm.getConfig().setGroupPrefix(groupName, meta) : pm.getConfig().setGroupSuffix(groupName, meta);
		
		if (!success) {
			sender.sendMessage(ChatColor.RED + "Group not found.");
		} else {
			sender.sendMessage(String.format(ChatColor.GREEN + "Successfully updated group %s. To re-apply this to online players with the group, run /perm reload.", friendly));
			pm.saveConfig();
		}
		
		return true;
		
	}
	
	/*
	 * Command:    /perm setpriority <group> <priority>
	 * Permission: defiancecraft.perm.setpriority
	 */
	public static boolean setPriority(CommandSender sender, String[] args) {
	
		String arguments = String.join(" ", args);
		Matcher matcher  = PAT_GROUPPRIORITY.matcher(arguments);
		String groupName = RegexUtils.getGroup(1, matcher);
		String priString = RegexUtils.getGroup(2, matcher);
		
		if (groupName.isEmpty() || priString.isEmpty()) {
			sender.sendMessage("Usage: /perm setpriority <group> <priority>");
			return true;
		}
		
		int priority = Integer.parseInt(priString);
		
		PermissionManager pm = DefianceCore.getPermissionManager();
		boolean success = pm.getConfig().setGroupPriority(groupName, priority);
		
		if (!success) {
			sender.sendMessage(ChatColor.RED + "Group not found.");
		} else {
			sender.sendMessage(ChatColor.GREEN + "Successfully set group priority.");
			pm.saveConfig();
		}
		
		return true;
		
	}
	
	/*
	 * Command:    /perm reload
	 * Permission: defiancecraft.perm.reload
	 */
	public static boolean reload(CommandSender sender, String[] args) {
	
		sender.sendMessage(ChatColor.GRAY + "Reloading permissions...");
		
		PermissionManager pm = DefianceCore.getPermissionManager();
		pm.reloadConfig();
		pm.reload();
		
		sender.sendMessage(ChatColor.GREEN + "Reloaded permissions from file and database.");
		
		return true;
		
	}
	
}