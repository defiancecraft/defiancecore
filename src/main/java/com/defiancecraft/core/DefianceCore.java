package com.defiancecraft.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import com.archeinteractive.defiancetools.util.JsonConfig;
import com.archeinteractive.defiancetools.util.command.CommandRegistry;
import com.archeinteractive.defiancetools.util.command.VirtualCommand;
import com.defiancecraft.core.commands.EconomyCommands;
import com.defiancecraft.core.commands.PermissionCommands;
import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.database.collections.Collection;
import com.defiancecraft.core.listeners.ChatListener;
import com.defiancecraft.core.listeners.PermissionListener;
import com.defiancecraft.core.listeners.PlayerDBJoinEventListener;
import com.defiancecraft.core.listeners.PlayerDBUpdateListener;
import com.defiancecraft.core.modules.Module;
import com.defiancecraft.core.modules.ModuleConfig;
import com.defiancecraft.core.permissions.PermissionManager;
import com.defiancecraft.core.util.FileUtils;

public class DefianceCore extends JavaPlugin {

	private static PermissionManager manager;
	private static ModuleConfig moduleConfig;
	
	// Array of Modules - are all assignable from Module class.
	private List<Plugin> loadedModules = new ArrayList<Plugin>();
	
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
	
		/*
		 * Load modules
		 */
		this.loadModules();
		
	}
	
	public void onDisable() {

		// Disable all modules
		this.unloadModules();
		
		// Remove all PermissionAttachments
		if (manager != null)
			manager.removeAllAttachments();
		
		// Shutdown ExecutorService
		Database.shutdownExecutorService();
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if (commandLabel == null || commandLabel.isEmpty())
			return false;
		
		// Execute the VirtualCommand (or at least try to)
		// This would occur if the execution was not directly
		// by a player/console (i.e. BuyCraft)
		VirtualCommand vc = CommandRegistry.getCommand(cmd.getName());
		if (vc != null)
			if (sender instanceof Player && vc.hasPlayerExecution())
				return vc.invokePlayer((Player)sender, args);
			else if (sender instanceof ConsoleCommandSender && vc.hasConsoleExecution())
				return vc.invokeConsole((ConsoleCommandSender)sender, args);
		
		return false;
		
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
		CommandRegistry.registerUniversalSubCommand("perm", "groups", "defiancecraft.perm.groups", PermissionCommands::groups);
		
		// Economy Commands
		CommandRegistry.registerUniversalCommand(this, "eco", "defiancecraft.eco.*", EconomyCommands::help);
		CommandRegistry.registerUniversalSubCommand("eco", "give", "defiancecraft.eco.give", EconomyCommands::give);
		CommandRegistry.registerUniversalSubCommand("eco", "take", "defiancecraft.eco.take", EconomyCommands::take);
		CommandRegistry.registerUniversalSubCommand("eco", "reset", "defiancecraft.eco.reset", EconomyCommands::reset);
		CommandRegistry.registerPlayerCommand(this, "bal", "defiancecraft.eco.bal", EconomyCommands::bal);
		CommandRegistry.registerPlayerCommand(this, "pay", "defiancecraft.eco.pay", EconomyCommands::pay);
		CommandRegistry.registerUniversalCommand(this, "balother", "defiancecraft.eco.balother", EconomyCommands::balOther);
		
	}
	
	private void loadModules() {
		
		ModuleConfig config = DefianceCore.getModuleConfig();
		
		// Plugin loader to load the module
		PluginLoader loader = this.getPluginLoader();
		
		for (String moduleName : config.enabledModules) {
			
			File moduleFile = new File(FileUtils.getModulesDirectory(), String.format("%s.dc.jar", moduleName));
			
			// Check that the module file exists.
			if (!moduleFile.exists() || !moduleFile.isFile()) {
				getLogger().severe(String.format("[Modules] Failed to find module '%s'! File not found.", moduleName));
				continue;
			}
			
			Plugin modulePlugin;
			
			// Load the module using JavaPluginLoader
			try {
				modulePlugin = loader.loadPlugin(moduleFile);
			} catch (UnknownDependencyException | InvalidPluginException e) {
				getLogger().severe(
						String.format(
								"[Modules] Failed to load module '%s'! %s: %s",
								moduleName,
								e.getClass().getSimpleName(),
								e.getMessage()));
				continue;
			}
			
			// Check that it implements Module
			if (!(modulePlugin instanceof Module)) {
				getLogger().severe(String.format("[Modules] Module '%s' does not extend Module class. Skipping module.", moduleName));
				continue;
			}

			loadedModules.add(modulePlugin);
			
			// Register any Collections it needs
			Module module = (Module)modulePlugin;
			for (Collection c : module.getCollections())
				Database.registerCollection(c);
			
			// Now attempt to enable the module!
			getLogger().info(String.format("[Modules] Enabling module '%s'...", module.getCanonicalName()));
			loader.enablePlugin(modulePlugin);
			
		}
		
	}

	/**
	 * Unloads all loaded modules.
	 */
	private void unloadModules() {
		
		PluginLoader loader = this.getPluginLoader();
		
		for (Plugin module : loadedModules) {
			getLogger().info(String.format("[Modules] Disabling module '%s'...", ((Module)module).getCanonicalName()));
			loader.disablePlugin(module);
		}
		
		loadedModules.clear();
		
	}
	
	/**
	 * Gets/loads the module configuration.
	 * @return ModuleConfig
	 */
	public static ModuleConfig getModuleConfig() {
		
		if (DefianceCore.moduleConfig == null)
			reloadModuleConfig();
		
		return DefianceCore.moduleConfig;
		
	}
	
	/**
	 * Reloads the module config from file.
	 */
	public static void reloadModuleConfig() {
		DefianceCore.moduleConfig = JsonConfig.load(FileUtils.getSharedConfig("modules.json"), ModuleConfig.class);
	}
	
	/**
	 * Gets the shared PermissionManager, for use by plugins
	 * or internal classes without access to DefianceCore
	 * instance.
	 * 
	 * @return PermissionManager
	 */
	public static PermissionManager getPermissionManager() {
		
		return manager;
		
	}
	
}
