package com.defiancecraft.core.util;

import java.util.IllegalFormatException;
import java.util.UUID;
import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class CommandUtils {

	/**
	 * Gets a group from a matched string, returning
	 * an empty string if the operation fails.
	 * 
	 * @param index Index of group
	 * @param m Matcher object
	 * @deprecated This method is obsolete; use ArgumentParser class instead.
	 * @return Matched group, or ""
	 */
	@Deprecated
	public static String getGroup(int index, Matcher m) {
		
		if (!m.matches())
			return "";
		
		try {
			return m.group(index);
		} catch (Exception e) { /*[DEBUG]*/e.printStackTrace();/*[/DEBUG]*/ }
		
		return "";
		
	}
	
	/**
	 * Convenice method to attempt to send a message to UUID `u`.
	 * This will work if `u` isn't null, and the player with
	 * that UUID is online. Colour codes will also be translated
	 * from ampersands (&). 
	 * 
	 * @param u UUID of Player - can be null
	 * @param msg Message to send, can be String.format compatible
	 * @param log Whether to log to the console
	 * @param formatArguments Any arguments to pass to String.format; optional
	 */
	public static void trySend(UUID u, String msg, boolean log, Object... formatArguments) {

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
	
}
