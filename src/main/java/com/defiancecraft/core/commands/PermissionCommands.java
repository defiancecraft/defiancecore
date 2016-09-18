package com.defiancecraft.core.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.defiancecraft.core.DefianceCore;
import com.defiancecraft.core.api.User;
import com.defiancecraft.core.command.ArgumentParser;
import com.defiancecraft.core.command.ArgumentParser.Argument;
import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.permissions.PermissionConfig;
import com.defiancecraft.core.permissions.PermissionConfig.Group;
import com.defiancecraft.core.permissions.PermissionManager;
import com.defiancecraft.core.util.CommandUtils;
import com.mongodb.MongoException;


public class PermissionCommands {

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
			"&b- /perm setuserprefix <user> <prefix|->\n" +
			"&b- /perm setusersuffix <user> <suffix|->\n" +
			"&b- /perm groups <user>\n" +
			"&3&oGroup Commands:\n" +
			"&b- /perm reload\n" +
			"&b- /perm creategroup <group>\n" +
			"&b- /perm addperm <group> <perm>\n" +
			"&b- /perm remperm <group> <perm>\n" +
			"&b- /perm setgroupprefix <group> <prefix|->\n" +
			"&b- /perm setgroupsuffix <group> <suffix|->\n" +
			"&b- /perm setpriority <group> <priority>\n"
		));
	
		return true;
		
	}
	
	/*
	 * Command:    /perm addgroup <user> <group>
	 * Permission: defiancecraft.perm.addgroup
	 */
	public static boolean addGroup(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME, Argument.WORD);
		
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /perm addgroup <user> <group>");
			return true;
		}
		
		final String user      = parser.getString(1);
		final String group     = parser.getString(2);
		final UUID senderUUID  = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console  = !(sender instanceof Player);
		
		if (user.isEmpty() || group.isEmpty()) {
			sender.sendMessage("Usage: /perm addgroup <user> <group>");
			return true;
		}
		
		Database.getExecutorService().submit(() -> {
			
			User u = User.findByNameOrCreate(user);
			if (u == null) {
				CommandUtils.trySend(senderUUID, "&cCould not find user with name '%s'", console, user);
				return;
			}
			
			boolean added = u.addGroup(group);
			if (!added) {
				CommandUtils.trySend(senderUUID, "&cCould not add group to user; database error", console);
				return;
			}
			
			// Update player's perms if they are online
			Player target = Bukkit.getPlayer(u.getDBU().getUUID());
			PermissionManager pm = DefianceCore.getPermissionManager();
			
			if (target != null)
				pm.updatePlayer(target, true);
				
			CommandUtils.trySend(senderUUID, "&aSuccessfully added group '%s' to user '%s'.", console, group, user);
			
		});
		
		sender.sendMessage(ChatColor.GRAY + "Adding group...");
		return true;
		
	}
	
	/*
	 * Command:    /perm uaddgroup <uuid> <group>
	 * Permission: defiancecraft.perm.addgroup
	 */
	public static boolean addGroupUuid(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.WORD, Argument.WORD);
		
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /perm uaddgroup <uuid> <group>");
			return true;
		}
		
		String uuidString = parser.getString(1);
		final UUID uuid;
		final String group      = parser.getString(2);
		final UUID senderUUID   = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console   = !(sender instanceof Player);
		
		if (uuidString.isEmpty() || group.isEmpty()) {
			sender.sendMessage("Usage: /perm uaddgroup <user> <group>");
			return true;
		}
		
		// Ensure uuidString is valid
		try {
			uuid = UUID.fromString(uuidString);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + "Invalid UUID");
			return true;
		}
		
		Database.getExecutorService().submit(() -> {
			
			User u = User.findByUUIDOrCreate(uuid, Bukkit.getOfflinePlayer(uuid).getName()/* nullable */);
			
			boolean added = u.addGroup(group);
			if (!added) {
				CommandUtils.trySend(senderUUID, "&cCould not add group to user; database error", console);
				return;
			}
			
			// Update player's perms if they are online
			Player target = Bukkit.getPlayer(u.getDBU().getUUID());
			PermissionManager pm = DefianceCore.getPermissionManager();
			
			if (target != null)
				pm.updatePlayer(target, true);
				
			String userName = u.getDBU().getName();
			CommandUtils.trySend(senderUUID, "&aSuccessfully added group '%s' to user '%s' (UUID %s).", console, group, userName == null ? "<null>" : userName, uuid.toString());
			
		});
		
		sender.sendMessage(ChatColor.GRAY + "Adding group...");
		return true;
		
	}
	
	/*
	 * Command:    /perm remgroup <user> <group>
	 * Permission: defiancecraft.perm.remgroup
	 */
	public static boolean remGroup(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME, Argument.WORD);
		
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /perm remgroup <user> <group>");
			return true;
		}
		
		final String user  = parser.getString(1);
		final String group = parser.getString(2);
		final UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console = !(sender instanceof Player);
		
		Database.getExecutorService().submit(() -> {
			
			User u = User.findByName(user);
			if (u == null) {
				CommandUtils.trySend(senderUUID, "&cCould not find user with name '%s'", console, user);
				return;
			}
			
			boolean success = u.removeGroup(group);
			if (!success) {
				CommandUtils.trySend(senderUUID, "&cFailed to remove group '%s' from user '%s'", console, group, user);
				return;
			}
			
			// Update player's perms if they are online
			Player target = Bukkit.getPlayer(u.getDBU().getUUID());
			PermissionManager pm = DefianceCore.getPermissionManager();
			
			if (target != null)
				pm.updatePlayer(target, true);
				
			CommandUtils.trySend(senderUUID, "&aSuccessfully removed group '%s' from user '%s'.", console, group, user);
			
		});
		
		sender.sendMessage(ChatColor.GRAY + "Removing group...");
		return true;
		
	}
	
	/*
	 * Command:    /perm uremgroup <uuid> <group>
	 * Permission: defiancecraft.perm.remgroup
	 */
	public static boolean remGroupUuid(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.WORD, Argument.WORD);
		
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /perm uremgroup <uuid> <group>");
			return true;
		}
		
		String uuidString = parser.getString(1);
		final UUID uuid;
		final String group = parser.getString(2);
		final UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console = !(sender instanceof Player);
		
		// Ensure uuidString is valid
		try {
			uuid = UUID.fromString(uuidString);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + "Invalid UUID");
			return true;
		}
		
		Database.getExecutorService().submit(() -> {
			
			User u = User.findByUUIDOrCreate(uuid, Bukkit.getOfflinePlayer(uuid).getName()/* nullable */);
			String name = u.getDBU().getName();
			
			boolean success = u.removeGroup(group);
			if (!success) {
				CommandUtils.trySend(senderUUID, "&cFailed to remove group '%s' from user '%s'", console, group, name == null ? "<null>" : name);
				return;
			}
			
			// Update player's perms if they are online
			Player target = Bukkit.getPlayer(u.getDBU().getUUID());
			PermissionManager pm = DefianceCore.getPermissionManager();
			
			if (target != null)
				pm.updatePlayer(target, true);
				
			CommandUtils.trySend(senderUUID, "&aSuccessfully removed group '%s' from user '%s'.", console, group, name == null ? "<null>" : name);
			
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

		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME, Argument.STRING);
		final String friendly = prefix ? "prefix" : "suffix"; // Friendly name for prefix/suffix, for reference in messages
		
		if (!parser.isValid()) {
			sender.sendMessage(String.format("Usage: /perm setuser%s <user> <prefix|->", friendly));
			return true;
		}
		
		final String user     = parser.getString(1);
		final String meta     = parser.getString(2).equalsIgnoreCase("-") ? "" : parser.getString(2);
		final UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console = !(sender instanceof Player);
		
		Database.getExecutorService().submit(() -> {
		
			User u = User.findByNameOrCreate(user);
			if (u == null) {
				CommandUtils.trySend(senderUUID, "&cCould not find user with name '%s'", console, user);
				return;
			}
			
			boolean success = prefix ? u.setPrefix(meta) : u.setSuffix(meta);
			if (!success) {
				CommandUtils.trySend(senderUUID, "&cFailed to set %s '%s' for user '%s'.", console, friendly, meta, user);
				return;
			}
			
			// Update player's metadata if they are online
			Player target = Bukkit.getPlayer(u.getDBU().getUUID());
			PermissionManager pm = DefianceCore.getPermissionManager();
			
			if (target != null)
				pm.updateMetadata(target);
			
			CommandUtils.trySend(senderUUID, "&aSuccessfully updated %s for user '%s'.", true, friendly, user);
		
		});
		
		sender.sendMessage(String.format(ChatColor.GRAY + "Setting user's %s", friendly));
		return true;
		
	}

	/*
	 * Command:    /perm creategroup <group>
	 * Permission: defiancecraft.perm.creategroup
	 */
	public static boolean createGroup(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.WORD);
		
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /perm creategroup <group>");
			return true;
		}
		
		String groupName     = parser.getString(1);
		PermissionManager pm = DefianceCore.getPermissionManager();
		
		if (pm.getConfig().getGroup(groupName) != null) {
			sender.sendMessage(ChatColor.RED + "Group already exists.");
			return true;
		}
		
		// Add group to config, as it does not exist already.
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
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.WORD, Argument.WORD);

		if (!parser.isValid()) {
			sender.sendMessage("Usage: /perm addperm <group> <perm>");
			return true;
		}
			
		String groupName = parser.getString(1);
		String perm      = parser.getString(2);
		
		// Attempt to add permission to group
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
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.WORD, Argument.WORD);

		if (!parser.isValid()) {
			sender.sendMessage("Usage: /perm remperm <group> <perm>");
			return true;
		}
		
		String groupName = parser.getString(1);
		String perm      = parser.getString(2);
		
		// Attempt to remove permission
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
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.WORD, Argument.WORD);
		String friendly  	  = prefix ? "prefix" : "suffix"; // Friendly name for prefix/suffix, for reference in messages
		
		if (!parser.isValid()) {
			sender.sendMessage(String.format("Usage: /perm set%1$s <group> <%1$s|->", friendly));
			return true;
		}
		
		String groupName = parser.getString(1);
		String meta      = parser.getString(2).equalsIgnoreCase("-") ? "" :parser.getString(2); // The new prefix or suffix to set
		
		// Attempt to set the prefix/suffix.
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
	
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.WORD, Argument.INTEGER);
	
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /perm setpriority <group> <priority>");
			return true;
		}
		
		String groupName = parser.getString(1);
		int priority     = parser.getInt(2);
		
		// Attempt to set the group priority
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
	 * Command:	   /perm groups <user>
	 * Permission: defiancecraft.perm.groups
	 */
	public static boolean groups(CommandSender sender, String[] args) {
	
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME);
		
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /perm groups <user>");
			return true;
		}
		
		final String playerName = parser.getString(1);
		final boolean console   = !(sender instanceof Player);
		final UUID senderUUID   = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		
		Database.getExecutorService().submit(() -> {
			
			User u = User.findByName(playerName);
			if (u == null) {
				CommandUtils.trySend(senderUUID, "&cUser not found", console);
				return;
			}
			
			List<String> groups = u.getDBU().getGroups();
			StringBuilder builder = new StringBuilder();
			builder.append("&9&l");
			builder.append(playerName);
			builder.append("'s Groups\n");
			
			for (String group : groups)
				builder.append("&b- ")
					.append(group)
					.append("\n");
			
			CommandUtils.trySend(senderUUID, ChatColor.translateAlternateColorCodes('&', builder.toString()), console);
			
		});
		
		return true;
		
	}
	
	/*
	 * Command:    /perm reload
	 * Permission: defiancecraft.perm.reload
	 */
	public static boolean reload(CommandSender sender, String[] args) {
	
		sender.sendMessage(ChatColor.GRAY + "Reloading permissions...");
		
		// Reload group configuration
		PermissionManager pm = DefianceCore.getPermissionManager();
		pm.reloadConfig();
		
		// Re-apply PermissionAttachments to players which are online
		try {
			pm.reload();
		} catch (MongoException e) {
			sender.sendMessage(ChatColor.RED + "A database error occurred while trying to reload permissions!");
			e.printStackTrace();
			return true;
		}
		
		sender.sendMessage(ChatColor.GREEN + "Reloaded permissions from file and database.");
		return true;
		
	}
	
}