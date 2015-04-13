package com.defiancecraft.core.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class CommandListener implements Listener, CommandExecutor {

    private static CommandListener listener;
    
    private CommandListener() {}
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        String[] command = e.getMessage().split(" ");
        
        if (command.length > 0 && command[0].startsWith("/")) {
            command[0] = command[0].substring(1);
        }
        
        VirtualCommand v;
        
        
        if (command.length > 0 && (v = CommandRegistry.getCommand(command[0])) != null && v.hasPlayerExecution()) {
            if (v.invokePlayer(e.getPlayer(), Arrays.copyOfRange(command, 1, command.length))) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onConsoleCommand(ServerCommandEvent e) {
        String[] command = e.getCommand().split(" ");
        VirtualCommand v;
        
        if (command.length > 0 && (v = CommandRegistry.getCommand(command[0])) != null && v.hasConsoleExecution()) {
            if (v.invokeConsole(Bukkit.getConsoleSender(), Arrays.copyOfRange(command, 1, command.length))) {
                e.setCommand(""); // ServerCommandEvents aren't cancellable
            }
        }
    }
    
    @Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
    	VirtualCommand v = CommandRegistry.getCommand(cmd.getName());
    	if (v != null) {
    		if (sender instanceof Player && v.hasPlayerExecution())
    			return v.invokePlayer((Player)sender, args);
    		else if (sender instanceof ConsoleCommandSender && v.hasConsoleExecution())
    			return v.invokeConsole(Bukkit.getConsoleSender(), args);
    	}
    	
    	return false;
    	
	}
    
    static CommandListener getInstance() {
    	return listener;
    }
    
    /**
     * Ensure that the virtual command listener has been setup. If it has not,
     * the given JavaPlugin will be used to register the listener.
     * <p>
     * Once the listener is setup, all plugins will be able to utilize the
     * virtual commands system, not only the one that has setup the listener.
     * 
     * @param plugin The plugin to register the listener under, if needed.
     */
    public static void setup(JavaPlugin plugin) {
        if (listener == null) {
            CommandListener.listener = new CommandListener();
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }

}
