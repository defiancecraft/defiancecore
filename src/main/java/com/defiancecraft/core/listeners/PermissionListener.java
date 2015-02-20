package com.defiancecraft.core.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.defiancecraft.core.permissions.PermissionManager;

public class PermissionListener implements Listener {

	private PermissionManager pm;
	
	public PermissionListener(PermissionManager pm) {
		this.pm = pm;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		
		pm.updatePlayer(e.getPlayer(), false);
		
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
