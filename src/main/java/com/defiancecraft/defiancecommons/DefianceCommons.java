package com.defiancecraft.defiancecommons;

import org.bukkit.plugin.java.JavaPlugin;

import com.defiancecraft.defiancecommons.database.Database;

public class DefianceCommons extends JavaPlugin {

	public void onEnable() {
		
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
			
			e.printStackTrace();
			getServer().shutdown();
			
		}
		
	}
	
	public void onDisable() {
		
		Database.shutdownExecutorService();
		
	}
	
}
