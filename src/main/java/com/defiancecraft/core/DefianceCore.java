package com.defiancecraft.core;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.archeinteractive.defiancetools.util.command.CommandRegistry;
import com.defiancecraft.core.commands.EconomyCommands;
import com.defiancecraft.core.commands.PermissionCommands;
import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.listeners.ChatListener;
import com.defiancecraft.core.listeners.PermissionListener;
import com.defiancecraft.core.listeners.PlayerDBJoinEventListener;
import com.defiancecraft.core.listeners.PlayerDBUpdateListener;
import com.defiancecraft.core.permissions.PermissionManager;

public class DefianceCore extends JavaPlugin {

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
		
		DefianceCore.manager = new PermissionManager(this);
		DefianceCore.manager.reload();
		
		/*
		 * Register event listeners (for permissions) 
		 */
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerDBJoinEventListener(), this);
		pm.registerEvents(new PermissionListener(DefianceCore.manager), this);
		pm.registerEvents(new ChatListener(DefianceCore.manager), this);
		pm.registerEvents(new PlayerDBUpdateListener(), this);
		
		/*
		 * Register commands
		 */
		
		this.registerCommands();
	
	}
	
	public void onDisable() {

		// Remove all PermissionAttachments
		if (manager != null)
			manager.removeAllAttachments();
		
		// Shutdown ExecutorService
		Database.shutdownExecutorService();
		
	}
	
	private void registerCommands() {
		
		// Permission Commands
		CommandRegistry.registerUniversalCommand(this, "perm", "defiancecraft.perm.*", PermissionCommands::help);
		CommandRegistry.registerUniversalSubCommand("perm", "help", "defiancecraft.perm.help", PermissionCommands::help);
		CommandRegistry.registerUniversalSubCommand("perm", "reload", "defiancecraft.perm.reload", PermissionCommands::reload);
		CommandRegistry.registerUniversalSubCommand("perm", "addgroup", "defiancecraft.perm.addgroup", PermissionCommands::addGroup);
		CommandRegistry.registerUniversalSubCommand("perm", "remgroup", "defiancecraft.perm.remgroup", PermissionCommands::remGroup);
		CommandRegistry.registerUniversalSubCommand("perm", "setuserprefix", "defiancecraft.perm.setuserprefix", (sender, args) -> PermissionCommands.setUserMeta(sender, args, true));
		CommandRegistry.registerUniversalSubCommand("perm", "setusersuffix", "defiancecraft.perm.setusersuffix", (sender, args) -> PermissionCommands.setUserMeta(sender, args, false));
		CommandRegistry.registerUniversalSubCommand("perm", "creategroup", "defiancecraft.perm.creategroup", PermissionCommands::createGroup);
		CommandRegistry.registerUniversalSubCommand("perm", "addperm", "defiancecraft.perm.addperm", PermissionCommands::addPerm);
		CommandRegistry.registerUniversalSubCommand("perm", "remperm", "defiancecraft.perm.remperm", PermissionCommands::remPerm);
		CommandRegistry.registerUniversalSubCommand("perm", "setgroupprefix", "defiancecraft.perm.setgroupprefix", (sender, args) -> PermissionCommands.setGroupMeta(sender, args, true));
		CommandRegistry.registerUniversalSubCommand("perm", "setgroupsuffix", "defiancecraft.perm.setgroupsuffix", (sender, args) -> PermissionCommands.setGroupMeta(sender, args, false));
		CommandRegistry.registerUniversalSubCommand("perm", "setpriority", "defiancecraft.perm.setpriority", PermissionCommands::setPriority);
		
		// Economy Commands
		CommandRegistry.registerUniversalCommand(this, "eco", "defiancecraft.eco.*", EconomyCommands::help);
		CommandRegistry.registerUniversalSubCommand("eco", "give", "defiancecraft.eco.give", EconomyCommands::give);
		CommandRegistry.registerUniversalSubCommand("eco", "take", "defiancecraft.eco.take", EconomyCommands::take);
		CommandRegistry.registerUniversalSubCommand("eco", "reset", "defiancecraft.eco.reset", EconomyCommands::reset);
		CommandRegistry.registerPlayerCommand(this, "bal", "defiancecraft.eco.bal", EconomyCommands::bal);
		CommandRegistry.registerPlayerCommand(this, "pay", "defiancecraft.eco.pay", EconomyCommands::pay);
		
	}
	
	public static PermissionManager getPermissionManager() {
		
		return manager;
		
	}
	
}
