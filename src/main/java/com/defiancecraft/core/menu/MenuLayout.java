package com.defiancecraft.core.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * A layout manager for a menu; this handles placing of MenuOptions
 * within the Minecraft inventory, as well as handling click events
 * of the menu (as only the layout knows which option is in which slot) 
 */
public interface MenuLayout {

	/**
	 * Adds an item to this layout; this is called internally by Menu
	 * @param option Option to add
	 */
	void addItem(MenuOption option);
	
	/**
	 * Adds an item to this layout using the given argument; this is
	 * called internally by Menu. Argument can be anything; slots to add
	 * to, position, alignment, etc. - it is decided by the layout.
	 * 
	 * @param option Option to add
	 * @param argument Argument to pass to the layout
	 */
	void addItem(MenuOption option, Object argument);
	
	/**
	 * Renders this layout into the given inventory.
	 * @param inventory Inventory to render into
	 */
	void render(Inventory inventory);
	
	/**
	 * Handles the click event by passing to the appropriate MenuOption. This
	 * must be handled by the layout rather than menu because layouts can
	 * re-arrange options, and thus raw slots may mean nothing to the menu.
	 * 
	 * @param player Player who clicked the inventory
	 * @param event InventoryClickEvent instance of click
	 * @return Whether the event should be cancelled.
	 */
	boolean onClick(Player player, InventoryClickEvent event);
	
}
