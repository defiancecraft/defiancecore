package com.defiancecraft.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import com.defiancecraft.core.api.Economy;
import com.defiancecraft.core.command.CommandRegistry;
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
import com.defiancecraft.core.util.JsonConfig;
import com.defiancecraft.core.util.Lang;

public class DefianceCore extends JavaPlugin {

	private static PermissionManager manager;
	private static ModuleConfig moduleConfig;
	private static net.milkbowl.vault.economy.Economy vault;
	
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
		 * Setup Vault
		 */
		if (Economy.getConfig().enabled)
			this.setupVault();
		
		/*
		 * Register commands
		 */
		
		this.registerCommands();
	
		/*
		 * Load language file
		 */
		
		try {
			Lang.reload();
		} catch (IOException e) {
			getLogger().warning("Language file could not be created! A blank file will be used instead.");
		}
		
		/*
		 * Load modules
		 */
		this.loadModules();
		
	}
	
	public void onDisable() {

		// Disable all modules
		this.unloadModules();
		
		// Save language file
		try {
			Lang.save();
		} catch (IOException e) {
			getLogger().severe("Failed to save language file!");
			e.printStackTrace();
		}
		
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
		CommandRegistry.registerUniversalSubCommand("perm", "groups", "defiancecraft.perm.groups", PermissionCommands::groups);
		
		// Economy Commands
		if (Economy.getConfig().enabled) {
			
			CommandRegistry.registerUniversalCommand(this, "token", "defiancecraft.eco.*", EconomyCommands::help);
			CommandRegistry.registerUniversalCommand(this, "tokenhelp", "defiancecraft.eco.*", EconomyCommands::help);
			
			CommandRegistry.registerUniversalSubCommand("token", "g", "defiancecraft.eco.give", EconomyCommands::give);
			CommandRegistry.registerUniversalSubCommand("token", "give", "defiancecraft.eco.give", EconomyCommands::give);
			CommandRegistry.registerUniversalCommand(this, "tokengive", "defiancecraft.eco.give", EconomyCommands::give);
			
			CommandRegistry.registerUniversalSubCommand("token", "t", "defiancecraft.eco.take", EconomyCommands::take);
			CommandRegistry.registerUniversalSubCommand("token", "take", "defiancecraft.eco.take", EconomyCommands::take);
			CommandRegistry.registerUniversalCommand(this, "tokentake", "defiancecraft.eco.take", EconomyCommands::take);
			
			CommandRegistry.registerUniversalSubCommand("token", "r", "defiancecraft.eco.reset", EconomyCommands::reset);
			CommandRegistry.registerUniversalSubCommand("token", "reset", "defiancecraft.eco.reset", EconomyCommands::reset);
			CommandRegistry.registerUniversalCommand(this, "tokenreset", "defiancecraft.eco.reset", EconomyCommands::reset);
			
			CommandRegistry.registerPlayerSubCommand("token", "b", "defiancecraft.eco.bal", EconomyCommands::bal);
			CommandRegistry.registerPlayerSubCommand("token", "bal", "defiancecraft.eco.bal", EconomyCommands::bal);
			CommandRegistry.registerPlayerCommand(this, "tokenbal", "defiancecraft.eco.bal", EconomyCommands::bal);
			
			CommandRegistry.registerPlayerSubCommand("token", "p", "defiancecraft.eco.pay", EconomyCommands::pay);
			CommandRegistry.registerPlayerSubCommand("token", "pay", "defiancecraft.eco.pay", EconomyCommands::pay);
			CommandRegistry.registerPlayerCommand(this, "tokenpay", "defiancecraft.eco.pay", EconomyCommands::pay);
			
			CommandRegistry.registerUniversalSubCommand("token", "o", "defiancecraft.eco.balother", EconomyCommands::balOther);
			CommandRegistry.registerUniversalSubCommand("token", "balother", "defiancecraft.eco.balother", EconomyCommands::balOther);
			CommandRegistry.registerUniversalCommand(this, "tokenbalother", "defiancecraft.eco.balother", EconomyCommands::balOther);
		
			CommandRegistry.registerPlayerSubCommand("token", "d", "defiancecraft.eco.redeem", EconomyCommands::redeem);
			CommandRegistry.registerPlayerSubCommand("token", "redeem", "defiancecraft.eco.redeem", EconomyCommands::redeem);
			CommandRegistry.registerPlayerCommand(this, "tokenredeem", "defiancecraft.eco.redeem", EconomyCommands::redeem);
			
		}
		
	}
	
	private void setupVault() {
		
		if (!getServer().getPluginManager().isPluginEnabled("Vault"))
			return;
		
		RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (rsp != null)
			DefianceCore.vault = rsp.getProvider();
		
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
			this.getServer().getPluginManager().enablePlugin(modulePlugin);
			
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
	 * Internal method; modules should obtain their own instance
	 * of Vault.
	 * 
	 * @return Vault Economy instance
	 */
	public static net.milkbowl.vault.economy.Economy getVault() {
		return vault;
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
	 * 
	 * @deprecated Modules use their own configuration files now; there should be no need to reload the ModuleConfig.
	 */
	@Deprecated
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
