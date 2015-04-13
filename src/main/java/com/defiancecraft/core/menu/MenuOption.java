package com.defiancecraft.core.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * An option within a menu, represented by an ItemStack and
 * placed into the inventory. Each MenuOption has a click event,
 * which is passed the player and event instance itself.
 */
public interface MenuOption {

	/**
	 * Gets an ItemStack to represent this option
	 * @return ItemStack
	 */
	public ItemStack getItemStack();
	
	/**
	 * Called when the MenuOption is clicked by a player
	 * 
	 * @param player Player who clicked the MenuOption
	 * @param event The event that was called
	 * @return Whether to cancel the InventoryClickEvent
	 */
	public boolean onClick(Player player, InventoryClickEvent event);
	
}
