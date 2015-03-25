package com.defiancecraft.core.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.defiancecraft.core.api.User;
import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.events.PlayerDBJoinEvent;

// This class listens for PlayerJoinEvents, and performs
// any necessary database updates.
public class PlayerDBUpdateListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDBJoin(PlayerDBJoinEvent e) {
		
		final String currentName = e.getPlayer().getName();
		
		Database.getExecutorService().submit(() -> {
			
			User u = e.getUser();
			if (u != null && (u.getDBU().getName() == null || !u.getDBU().getName().equals(currentName)))
				u.setName(currentName);
			
		});
		
	}
	
}
