package com.defiancecraft.defiancecommons.commands;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.defiancecraft.defiancecommons.database.Database;
import com.defiancecraft.defiancecommons.database.collections.Users;
import com.defiancecraft.defiancecommons.database.documents.DBUser;
import com.defiancecraft.defiancecommons.util.RegexUtils;
import com.defiancecraft.defiancecommons.util.UUIDUtils;
import com.defiancecraft.defiancecommons.util.UUIDUtils.UUIDResponse;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class PermissionCommands {

	// <user> <group> Pattern
	private static final Pattern PAT_USERGROUP = Pattern.compile("^([a-zA-Z0-9_]{1,16}) ([^ ]+)$");
	
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
			
			Users users = Database.getCollection(Users.class);
			DBObject pushValue = new BasicDBObject(DBUser.FIELD_GROUPS, group);
			
			// Try and update the user
			WriteResult result = users.update(
				new BasicDBObject(DBUser.FIELD_NAME, user), // Update where name == `user`
				new BasicDBObject("$addToSet", pushValue) // Push `group` onto FIELD_GROUPS if it doesn't exist.
			);
			
			// If there were no documents updated,
			// get their UUID from Mojang, and attempt
			// to update again, but by UUID. If this fails,
			// just make a new user with that group.
			if (result.getN() == 0) {

				// Get UUID from Mojang
				UUIDResponse uuidInfo = UUIDUtils.getUUID(user, System.currentTimeMillis() / 1000);
				
				if (uuidInfo == null 
						|| uuidInfo.name == null
						|| uuidInfo.name.isEmpty()
						|| uuidInfo.id == null
						|| uuidInfo.id.isEmpty()) {
					Bukkit.getLogger().severe("Failed to find a user with username '" + user + "'. Could not add group '" + group + "' to them as UUID was not found.");
					if (senderUUID != null)
						if (Bukkit.getPlayer(senderUUID) != null)
							Bukkit.getPlayer(senderUUID).sendMessage("§cFailed to add group. User does not exist.");
					return;
				}
				
				UUID uuid = UUIDUtils.toUUID(uuidInfo.id);
				
				// Try and update again, with UUID this time
				result = users.update(
					new BasicDBObject(DBUser.FIELD_UUID, uuid.toString()), // Update where uuid == `uuid`
					new BasicDBObject("$addToSet", pushValue) // Push `group` onto FIELD_GROUPS
				);
				
				// Create if update failed
				if (result.getN() == 0) {
					
					DBUser newUser = new DBUser(uuid, uuidInfo.name);
					newUser.addGroup(group);
					users.save(newUser);
					
				}
				
			}
			
			// Send success message if succeeded.
			if (senderUUID != null) {
				if (Bukkit.getPlayer(senderUUID) != null)
					Bukkit.getPlayer(senderUUID).sendMessage(String.format("§aAdded group '%s' to '%s'", group, user));
			} else {
				Bukkit.getLogger().info(String.format("Added group '%s' to '%s'", group, user));
			}
					
		});
		
		sender.sendMessage("Adding group...");
		return true;
		
	}
	
	public static boolean remGroup(CommandSender sender, String[] args) {
		
		String arguments = String.join(" ", args);
		Matcher matcher  = PAT_USERGROUP.matcher(arguments);
		
		final String user  = RegexUtils.getGroup(1, matcher);
		final String group = RegexUtils.getGroup(2, matcher);
		
		if (user.isEmpty() || group.isEmpty()) {
			sender.sendMessage("Usage: /perm remgroup <user> <group>");
			return true;
		}
			
		Database.getExecutorService().submit(() -> {
			
			Users users = Database.getCollection(Users.class);
			DBUser dbu = users.getByName(user);
			
			if (dbu == null) {
				// TODO
			}
			
		});
		
		return true;
		
	}
	
	/*
	public static boolean remGroup(CommandSender sender, String[] args) {}
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