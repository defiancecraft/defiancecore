package com.defiancecraft.core.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.plugin.Plugin;

public class MenuListener implements Listener {

	private static boolean registered = false;

	@EventHandler(priority = EventPriority.LOWEST)
	public void onInventoryClick(InventoryClickEvent e) {
		
		if (!(e.getInventory().getHolder() instanceof Menu))
			return;
		
		Menu menu = (Menu) e.getInventory().getHolder();
		
		// Cancel & return if it's not a player. 
		if (!(e.getWhoClicked() instanceof Player)) {
			e.setCancelled(true);
			return;
		}
		
		Player player = (Player) e.getWhoClicked();
		boolean cancel = true;
		
		// Ensure click type wasn't outside. If it was,
		// check if closeOnClickOutside, then close.
		if (e.getSlotType().equals(SlotType.OUTSIDE)) {
			if (menu.getCloseOnClickOutside())
				menu.closeMenu(player);
		} else {
			if (e.getRawSlot() < menu.getSlots()) {
				cancel = menu.onClick(player, e);
			} else if (menu.getCloseOnClickOutside()) {
				menu.closeMenu(player);
			} else {
				// Cancel if not left click; can be double click, shift click, etc.
				cancel = !ClickType.LEFT.equals(e.getClick());
			}
		}
		
		e.setCancelled(cancel);
		e.setResult(cancel ? Result.DENY : Result.ALLOW);
		
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onInventoryDrag(InventoryDragEvent e) {
		
		if (e.getInventory() != null
				&& e.getInventory().getHolder() instanceof Menu)
			e.setCancelled(true);
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent e) {
		
		if (e.getInventory().getHolder() instanceof Menu
				&& e.getPlayer() instanceof Player)
			((Menu)e.getInventory().getHolder()).onClose((Player)e.getPlayer());
		
	}
	
	/**
	 * Registers the MenuListener with the given plugin,
	 * if it has not already been registered.
	 * 
	 * @param p Plugin to register the MenuListener to
	 */
	public static void register(Plugin p) {
		if (!registered) {
			Bukkit.getPluginManager().registerEvents(new MenuListener(), p);
			MenuListener.registered = true;
		}
	}
	
}
