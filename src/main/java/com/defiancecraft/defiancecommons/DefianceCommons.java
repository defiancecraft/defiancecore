package com.defiancecraft.defiancecommons;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.archeinteractive.defiancetools.util.command.CommandRegistry;
import com.defiancecraft.defiancecommons.commands.PermissionCommands;
import com.defiancecraft.defiancecommons.database.Database;
import com.defiancecraft.defiancecommons.listeners.ChatListener;
import com.defiancecraft.defiancecommons.listeners.PermissionListener;
import com.defiancecraft.defiancecommons.permissions.PermissionManager;

public class DefianceCommons extends JavaPlugin {

	private static PermissionManager manager;
	
	public void onEnable() {
		
		/*
		 * Setup Database
		 */
		
		try {
			
			Database.init(this);
			
		} catch (Throwable e) {
			
			getLogger().severe("==================================");
			getLogger().severe("=         CRITICAL ERROR         =");
			getLogger().severe("==================================");
			getLogger().severe("=  An exception occurred while   =");
			getLogger().severe("=   connecting to the database.  =");
			getLogger().severe("=      Shutting down server.     =");
			getLogger().severe("==================================");
			getLogger().severe("Message: " + e.getMessage());
			getLogger().severe("Exception Type: " + e.getClass().getCanonicalName());
			
			getServer().shutdown();
			return;
			
		}
		
		/*
		 * Setup permissions
		 */
		
		DefianceCommons.manager = new PermissionManager(this);
		DefianceCommons.manager.reload();
		
		/*
		 * Register event listeners (for permissions) 
		 */
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PermissionListener(DefianceCommons.manager), this);
		pm.registerEvents(new ChatListener(DefianceCommons.manager), this);
		
		/*
		 * Register commands
		 */
		
		this.registerCommands();
	
	}
	
	public void onDisable() {

		// Remove all PermissionAttachments
		manager.removeAllAttachments();
		
		// Shutdown ExecutorService
		Database.shutdownExecutorService();
		
	}
	
	private void registerCommands() {
		
		CommandRegistry.registerUniversalCommand(this, "perm", "defiancecraft.perm.*", PermissionCommands::help);
		CommandRegistry.registerUniversalSubCommand("perm", "help", "defiancecraft.perm.help", PermissionCommands::help);
		CommandRegistry.registerUniversalSubCommand("perm", "reload", "defiancecraft.perm.reload", PermissionCommands::reload);
		CommandRegistry.registerUniversalSubCommand("perm", "addgroup", "defiancecraft.perm.addgroup", PermissionCommands::addGroup);
		CommandRegistry.registerUniversalSubCommand("perm", "remgroup", "defiancecraft.perm.remgroup", PermissionCommands::remGroup);
		
		// TODO: Rest of these
		//CommandRegistry.registerUniversalSubCommand("perm", "remgroup", "defiancecraft.perm.remgroup", PermissionCommands::remGroup);
		
	}
	
	public static PermissionManager getPermissionManager() {
		
		return manager;
		
	}
	
}
