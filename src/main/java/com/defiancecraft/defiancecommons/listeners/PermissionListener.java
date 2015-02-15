package com.defiancecraft.defiancecommons.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.defiancecraft.defiancecommons.permissions.PermissionManager;

public class PermissionListener implements Listener {

	private PermissionManager pm;
	
	public PermissionListener(PermissionManager pm) {
		this.pm = pm;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		
		pm.updatePlayer(e.getPlayer());
		
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		
		pm.removeAttachment(e.getPlayer());
		
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent e) {
		
		pm.removeAttachment(e.getPlayer());
		
	}
	
}
