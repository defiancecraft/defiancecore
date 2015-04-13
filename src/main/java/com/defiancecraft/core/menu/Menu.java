package com.defiancecraft.core.menu;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * An implementation of InventoryHolder with menu-like
 * functionality. This class should be extended with the
 * desired functionality, e.g. adding options to the menu.
 * 
 * Note that for a Menu created in this way, a MenuListener
 * must be registered (see {@link MenuListener#register(Plugin)})
 */
public abstract class Menu implements InventoryHolder {

	private String title;
	private int rows;
	private Inventory inventory;
	private boolean closeOnClickOutside = false;
	private Consumer<Player> closeHandler = null;
	
	private MenuLayout layout;
	
	/**
	 * Creates a menu using the given title, rows, and layout.
	 * The number of slots is given by `rows * 9`.
	 * 
	 * Subclasses should implement {@link #addMenuOptions()}, rather
	 * than trying to add them in the constructor, as {@link #init()}
	 * is called in the constructor, and would thus be called before
	 * any menu options have been added.
	 * 
	 * @param title The title to display on the menu
	 * @param rows Number of rows in the menu
	 * @param layout Menu's layout
	 */
	protected Menu(String title, int rows, MenuLayout layout) {
		
		if (layout == null)
			throw new IllegalArgumentException("Menu layout cannot be null.");
		
		// Set title if empty
		if (title == null || title.isEmpty())
			title = "Menu";
		
		// Truncate title, cannot exceed 32 characters.
		if (title.length() > 32)
			title = title.substring(0, 32);
		
		this.title = title;
		this.rows = rows;
		this.inventory = Bukkit.createInventory(this, rows * 9, title);
		this.layout = layout;
		
		this.init();
		
	}
	
	@Override
	public Inventory getInventory() {
		return inventory;
	}

	/**
	 * Adds in the menu options for this menu. This should be overriden in
	 * order to add the necessary options before 'rendering' the inventory.
	 */
	protected abstract void addMenuOptions();
	
	/**
	 * Initializes the menu, calling {@link #addMenuOptions()} in the
	 * process in order to add any necessary menu options.
	 */
	protected void init() {
		this.addMenuOptions();
		this.layout.render(inventory);
	}

	/**
	 * Adds an option to the menu
	 * @param option MenuOption to add
	 */
	protected void addMenuOption(MenuOption option) {
		this.layout.addItem(option);
	}
	
	/**
	 * Adds an option to the menu, passing a given argument to
	 * the menu's layout.
	 * 
	 * @param option MenuOption to add
	 * @param param Argument to pass to the layout
	 */
	protected void addMenuOption(MenuOption option, Object param) {
		this.layout.addItem(option, param);
	}
	
	/**
	 * Sets whether the menu should close when a player clicks outside
	 * of it.
	 * 
	 * @param value Whether the menu should close when clicked outside
	 */
	protected void setCloseOnClickOutside(boolean value) {
		this.closeOnClickOutside = value;
	}
	
	/**
	 * Sets the handler to use when the menu is closed.
	 * @param handler A callback for when the menu is closed.
	 */
	protected void setCloseHandler(Consumer<Player> handler) {
		this.closeHandler = handler;
	}
	
	/**
	 * Re-renders the inventory of the menu via the given layout.
	 */
	protected void rerender() {
		this.layout.render(inventory);
	}
	
	/**
	 * Called internally when the inventory is clicked.
	 * @param whoClicked Who clicked on the inventory
	 * @param event The InventoryClickEvent instance of the click
	 * @return Whether to cancel the InventoryClickEvent
	 */
	@SuppressWarnings("deprecation")
	boolean onClick(Player whoClicked, InventoryClickEvent event) {
		boolean cancel = this.layout.onClick(whoClicked, event);
		whoClicked.updateInventory();
		return cancel;
	}
	
	/**
	 * Called internally when the inventory is closed.
	 * @param player The player that was viewing the inventory.
	 */
	void onClose(Player player) {
		if (this.closeHandler != null)
			this.closeHandler.accept(player);
	}

	/**
	 * Gets the title of this Menu
	 * @return Menu's title
	 */
	public String getTitle() {
		return this.title;
	}
	
	/**
	 * Gets the maximum number of slots in the inventory.
	 * @return Slots
	 */
	public int getSlots() {
		return this.rows * 9;
	}
	
	/**
	 * Gets the number of rows of this inventory
	 * @return Number of rows
	 */
	public int getRows() {
		return this.rows;
	}

	/**
	 * Gets whether the menu should close when outside
	 * (i.e. not an inventory) is clicked.
	 * @return Whether the menu should close
	 */
	public boolean getCloseOnClickOutside() {
		return closeOnClickOutside;
	}
	
	/**
	 * Opens the menu to the player. Note that this method should be
	 * ran inside of a BukkitRunnable in order to avoid problems.
	 * 
	 * @param p Player to show menu to
	 * @throws IllegalStateException if player is already viewing the menu.
	 */
	public void openMenu(Player p) {
		if (getInventory().getViewers().contains(p))
			throw new IllegalStateException(String.format("Player '%s' (UUID: %s) is already viewing the menu.", p.getName(), p.getUniqueId()));
		
		p.openInventory(getInventory());
	}
	
	/**
	 * Closes the menu if player is viewing it.
	 * 
	 * @param p Player to close menu for
	 */
	public void closeMenu(Player p) {
		if (getInventory().getViewers().contains(p)) {
			getInventory().getViewers().remove(p);
			p.closeInventory();
		}
	}
	
	/**
	 * Switches from one menu to another for given viewer.
	 * Note: this does not need to be ran asynchronously (i.e. in a BukkitRunnable)
	 * as this is included within the method.
	 * 
	 * @param viewer Viewer of the menu
	 * @param from Menu to switch from
	 * @param to Menu to switch to
	 * @param plugin Plugin instance to run opening menu task
	 */
	public static void switchMenu(final Player viewer, Menu from, Menu to, Plugin plugin) {
		
		from.closeMenu(viewer);
		new BukkitRunnable() {
			@Override
			public void run() {
				to.openMenu(viewer);
			}
		}.runTask(plugin);
		
	}
	
}
