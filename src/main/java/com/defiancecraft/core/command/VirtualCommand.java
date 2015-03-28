package com.defiancecraft.core.command;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A Class for representing a VirtualCommand, which is registered from an
 * implementation of the functional interfaces
 */
public class VirtualCommand {

    final Class<? extends JavaPlugin> plugincls;
    final Map<String, VirtualSubCommand> subcommands;
    volatile CommandAction<? super Player> player;
    volatile CommandAction<? super ConsoleCommandSender> console;
    volatile String permission = "";

    VirtualCommand(Class<? extends JavaPlugin> plugincls) {
        this.plugincls = plugincls;
        this.subcommands = new HashMap<>();
        this.player = null;
        this.console = null;
    }

    VirtualCommand(Class<? extends JavaPlugin> plugincls, String permission) {
        this(plugincls);
        this.permission = permission;
    }
    
    public boolean invokeConsole(ConsoleCommandSender sender, String[] args) {
        VirtualSubCommand s;
        
        if (args.length < 1 || (s = getSubCommand(args[0])) == null) {
            if (hasConsoleExecution()) {
                console.invoke(sender, args);
                return true;
            }
            
            return false;
        }
        
        if (s.hasConsoleExecution()) {
            s.console.invoke(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }
        
        return false;
    }
    
    public boolean invokePlayer(Player sender, String[] args) {
        VirtualSubCommand s;
        
        if (args.length < 1 || (s = getSubCommand(args[0])) == null) {
            if (hasPlayerExecution()) {
                if (permission.equalsIgnoreCase("") || sender.hasPermission(permission)) {
                    player.invoke(sender, args);
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                }
                return true;
            }
            
            return false;
        }
        
        if (s.hasPlayerExecution()) {
            if (s.permission.equalsIgnoreCase("") || sender.hasPermission(s.permission)) {
                s.player.invoke(sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            }
            return true;
        }
        
        return false;
    }

    public String toString() {
        return String.format("VirtualCommand{plugin=%s,p exec=%s, c exec=%s}",
                plugincls.getSimpleName(), hasPlayerExecution(), hasConsoleExecution());
    }

    public boolean hasPlayerExecution() {
        return player != null;
    }

    public boolean hasConsoleExecution() {
        return console != null;
    }
    
    VirtualSubCommand getSubCommand(String sub_label) {
        for (Map.Entry<String, VirtualSubCommand> entry : subcommands.entrySet()) {
            if (sub_label.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return null;
    }

    class VirtualSubCommand {
    
        volatile CommandAction<? super Player> player;
        volatile CommandAction<? super ConsoleCommandSender> console;
        volatile String permission;

        public VirtualSubCommand() {}

        public VirtualSubCommand(String permission) {
            this.permission = permission;
        }
        
        boolean hasPlayerExecution() {
            return player != null;
        }
        
        boolean hasConsoleExecution() {
            return console != null;
        }
    }
}
