package com.defiancecraft.core.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.defiancecraft.core.permissions.PermissionManager;
import com.defiancecraft.core.permissions.PermissionMetadata;
import com.mongodb.MongoException;

public class ChatListener implements Listener {

	private PermissionManager pm;
	
	public ChatListener(PermissionManager pm) {
		this.pm = pm;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {

		if (e.isCancelled())
			return;
		
		Player player = e.getPlayer();
		PermissionMetadata meta = pm.getMetadata(player);
		
		// Meta should never theoretically be
		// null, but just in case...
		if (meta == null) {
			
			try {
				pm.updateMetadata(player);
			} catch (MongoException ex) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "An internal server error occurred.");
				ex.printStackTrace();
				return;
			}
			
			meta = pm.getMetadata(player);
		}
		
		String chatFormat = pm.getConfig().chatFormat
				.replace("{prefix}", meta.getPrefix())
				.replace("{suffix}", meta.getSuffix())
				.replace("{name}", "%1$s")
				.replace("{message}", "%2$s")
				.replace("&", "\u00A7"); // Section Symbol U+00A7
		
		e.setFormat(chatFormat);
		
	}
	
}
