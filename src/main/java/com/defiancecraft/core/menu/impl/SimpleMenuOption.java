package com.defiancecraft.core.menu.impl;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.defiancecraft.core.menu.MenuOption;

/**
 * A MenuOption implementation using a given ItemStack and callback,
 * which simply accepts a Player.
 */
public class SimpleMenuOption implements MenuOption {

	private ItemStack item;
	private Predicate<Player> callback;
	
	/**
	 * Constructs a new SimpleMenuOption represented by the given item
	 * which will, when clicked, execute `callback`.
	 * 
	 * @param item ItemStack to represent the option
	 * @param callback Callback for when option is clicked
	 */
	public SimpleMenuOption(ItemStack item, Predicate<Player> callback) {
		this.item = item;
		this.callback = callback;
	}
	
	@Override
	public ItemStack getItemStack() {
		return item;
	}

	@Override
	public boolean onClick(Player p, InventoryClickEvent c) {
		if (!ClickType.LEFT.equals(c.getClick()))
			return true;
		else
			return this.callback.test(p);
	}
	
	/**
	 * A builder for SimpleMenuOptions, allowing for easy setting of
	 * display names, lore, quantity, etc.
	 */
	public static class SimpleMenuOptionBuilder {
		
		private ItemStack item;
		private Predicate<Player> callback;
		
		/**
		 * Constructs a new builder with the material given and callback.
		 * 
		 * @param material Material of the item to represent this MenuOption
		 * @param callback Callback for when the option is clicked
		 */
		public SimpleMenuOptionBuilder(Material material, Predicate<Player> callback) {
			this.item = new ItemStack(material, 1);
			this.callback = callback;
		}
		
		/**
		 * Constructs a new builder with the material given and callback (will return
		 * true in order to cancel event as this is the default)
		 * 
		 * @param material Material of the item to represent this MenuOption
		 * @param callback Callback for when the option is clicked
		 */
		public SimpleMenuOptionBuilder(Material material, Consumer<Player> callback) {
			this.item = new ItemStack(material, 1);
			this.callback = (p) -> { callback.accept(p); return true; };
		}
		
		/**
		 * Sets the item's quantity
		 * @param quantity Quantity of item to represent MenuOption
		 * @return this
		 */
		public SimpleMenuOptionBuilder quantity(int quantity) {
			this.item.setAmount(quantity);
			return this;
		}
		
		/**
		 * Sets the item's display name 
		 * @param text Item's new display name (can contain ampersand colour codes)
		 * @return this
		 */
		public SimpleMenuOptionBuilder text(String text) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', text));
			item.setItemMeta(meta);
			return this;
		}
		
		/**
		 * Sets the item's lore
		 * @param lore New lore of item (can contain ampersand colour codes)
		 * @return this
		 */
		public SimpleMenuOptionBuilder lore(String... lore) {
			
			// Translate colour codes
			for (int i = 0; i < lore.length; i++) {
				if (lore[i] != null && !lore[i].isEmpty())
					lore[i] = ChatColor.translateAlternateColorCodes('&', lore[i]);
			}
			
			// Set item meta
			ItemMeta meta = item.getItemMeta();
			meta.setLore(Arrays.asList(lore));
			item.setItemMeta(meta);
			
			return this;
			
		}

		/**
		 * Sets the durability of the item
		 * @param durability New item durability
		 * @return this
		 */
		public SimpleMenuOptionBuilder durability(short durability) {
			item.setDurability(durability);
			return this;
		}
		
		/**
		 * Creates the SimpleMenuOption
		 * @return SimpleMenuOption
		 */
		public SimpleMenuOption build() {
			return new SimpleMenuOption(this.item, this.callback);
		}
		
	}
	
}
