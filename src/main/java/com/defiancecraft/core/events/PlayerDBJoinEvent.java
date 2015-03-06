package com.defiancecraft.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import com.defiancecraft.core.api.User;

/**
 * Wrapper class for PlayerJoinEvent containing a lazily
 * populated user field in order to prevent multiple DB
 * queries from listeners.
 */
public class PlayerDBJoinEvent extends PlayerJoinEvent {

	private User user;
	
	public PlayerDBJoinEvent(Player playerJoined, String joinMessage) {
		super(playerJoined, joinMessage);
	}
	
	/**
	 * Lazily populates a user field; upon first request, the
	 * user will be looked up by UUID. If they do not exist,
	 * null is returned (user is not created).
	 * 
	 * @return User object, or null if non-existent.
	 */
	public User getUser() {
		if (user == null)
			user = User.findByUUID(this.getPlayer().getUniqueId());
		return user;
	}

}
