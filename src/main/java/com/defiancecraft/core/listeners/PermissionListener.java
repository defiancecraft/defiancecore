package com.defiancecraft.core.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import com.defiancecraft.core.permissions.PermissionManager;
import com.mongodb.MongoException;

public class PermissionListener implements Listener {

	private PermissionManager pm;
	
	public PermissionListener(PermissionManager pm) {
		this.pm = pm;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent e) {

		// Note: not a good idea to run in ExecutorService
		// due to the fact that a Player object would be passed.
		
		if (!e.getResult().equals(Result.ALLOWED))
			return;
		
		try {
			pm.updatePlayer(e.getPlayer(), false);
		} catch (MongoException ex) {
			e.disallow(Result.KICK_OTHER, "Internal server error");
		}
		
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
