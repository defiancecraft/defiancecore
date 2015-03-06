package com.defiancecraft.core.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.defiancecraft.core.api.User;

/**
 * Wrapper class for PlayerJoinEvent containing a lazily
 * populated user field in order to prevent multiple DB
 * queries from listeners.
 */
public class PlayerDBJoinEvent extends PlayerEvent {

	private static HandlerList handlers = new HandlerList();
	private User user;
	private PlayerJoinEvent delegated;
	
	public PlayerDBJoinEvent(PlayerJoinEvent delegated) {
		super(delegated.getPlayer());
		this.delegated = delegated;
	}
	
	/**
	 * @see PlayerJoinEvent#getJoinMessage()
	 */
	public String getJoinMessage() { return delegated.getJoinMessage(); }
	
	/**
	 * @see PlayerJoinEvent#setJoinMessage(String)
	 */
	public void setJoinMessage(String joinMessage) { delegated.setJoinMessage(joinMessage); }
	
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
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
