package com.defiancecraft.core.menu.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.defiancecraft.core.menu.MenuLayout;
import com.defiancecraft.core.menu.MenuOption;

public class AbsoluteMenuLayout implements MenuLayout {

	private int rows;
	private MenuOption[] items;
	
	public AbsoluteMenuLayout(int rows) {
		this.rows = rows;
		this.items = new MenuOption[rows * 9];
	}
	
	@Override
	public void addItem(MenuOption option) {
		
		int slot = -1;
		
		for (int i = 0; i < items.length; i++)
			if (items[i] == null) {
				slot = i;
				break;
			}
		
		if (slot == -1)
			throw new IllegalStateException("Menu is full; add items using Menu#addItem(MenuOption, Object) with a slot instead.");
		
		items[slot] = option;
		
	}

	@Override
	public void addItem(MenuOption option, Object argument) {
		
		if (!(argument instanceof Integer)
				|| ((Integer)argument).intValue() < 0
				|| ((Integer)argument).intValue() >= this.rows * 9)
			throw new IllegalStateException("Argument must be a valid slot.");
		
		int slot = ((Integer)argument).intValue();
		items[slot] = option;
		
	}

	@Override
	public void render(Inventory inventory) {
		
		ItemStack[] contents = new ItemStack[rows * 9];
		for (int i = 0; i < items.length; i++)
			contents[i] = items[i] == null ? null : items[i].getItemStack();
		
		inventory.setContents(contents);
		
	}

	@Override
	public boolean onClick(Player player, InventoryClickEvent event) {
		
		if (event.getRawSlot() < items.length
				&& event.getRawSlot() >= 0
				&& items[event.getRawSlot()] != null)
			return items[event.getRawSlot()].onClick(player, event);
		
		return true;
		
	}

}
