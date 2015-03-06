package com.defiancecraft.core.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.defiancecraft.core.events.PlayerDBJoinEvent;

// This class simply handles PlayerJoinEvents and
// emits a PlayerDBJoinEvent.
public class PlayerDBJoinEventListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Bukkit.getPluginManager().callEvent(new PlayerDBJoinEvent(e));
	}
	
}
