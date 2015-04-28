package com.defiancecraft.core.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandListener implements CommandExecutor {

    private static CommandListener listener;
    
    private CommandListener() {}
    
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
    	if (listener == null)
    		listener = new CommandListener();
    	return listener;
    }
    
    /**
     * @deprecated This method is no longer needed as the CommandListener
     * does not act as a listener, but rather a CommandExecutor for more
     * robust command handling. It exists only to preserve backward compatibility;
     * the method itself does nothing.
     * 
     * @param plugin The plugin to which to register the events
     */
    @Deprecated
    public static void setup(JavaPlugin plugin) {
    }

}
