package com.defiancecraft.defiancecommons.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Evan Lindsay (Favorlock)
 */
public class CommandRegistry {

    private static final Map<String, VirtualCommand> virtual = new HashMap<>();
    
    public static void registerUniversalCommand(JavaPlugin plugin, String label, CommandAction<? super CommandSender> action) {
        if (virtual.containsKey(label) && (virtual.get(label).hasConsoleExecution() || virtual.get(label).hasPlayerExecution())) {
            throw new IllegalArgumentException("A virtual command is already registered under the given label!");
        }
        
        registerPlayerCommand(plugin, label, action);
        registerConsoleCommand(plugin, label, action);
    }

    public static void registerUniversalCommand(JavaPlugin plugin, String label, String permission, CommandAction<? super CommandSender> action) {
        if (virtual.containsKey(label) && (virtual.get(label).hasConsoleExecution() || virtual.get(label).hasPlayerExecution())) {
            throw new IllegalArgumentException("A virtual command is already registered under the given label!");
        }

        registerPlayerCommand(plugin, label, permission, action);
        registerConsoleCommand(plugin, label, action);
    }
    
    public static void registerPlayerCommand(JavaPlugin plugin, String label, CommandAction<? super Player> action) {
        VirtualCommand v = virtual.get(label);
        
        if (v == null) {
            v = new VirtualCommand(plugin.getClass());
            virtual.put(label, v);
        }
        
        if (v.hasPlayerExecution()) {
            throw new IllegalArgumentException("A virtual command is already registered under the given label!");
        }
        
        v.player = action;
    }

    public static void registerPlayerCommand(JavaPlugin plugin, String label, String permission, CommandAction<? super Player> action) {
        VirtualCommand v = virtual.get(label);

        if (v == null) {
            v = new VirtualCommand(plugin.getClass(), permission);
            virtual.put(label, v);
        }

        if (v.hasPlayerExecution()) {
            throw new IllegalArgumentException("A virtual command is already registered under the given label!");
        }

        v.player = action;
    }

    public static void registerConsoleCommand(JavaPlugin plugin, String label, CommandAction<? super ConsoleCommandSender> action) {
        VirtualCommand v = virtual.get(label);

        if (v == null) {
            v = new VirtualCommand(plugin.getClass());
            virtual.put(label, v);
        }

        if (v.hasConsoleExecution()) {
            throw new IllegalArgumentException("A virtual command is already registered under the given label!");
        }

        v.console = action;
    }
    
    public static void registerUniversalSubCommand(String main_label, String sub_label, CommandAction<? super CommandSender> action) {
        if (virtual.get(main_label) == null) {
            throw new IllegalStateException("No virtual command has been registered under the given main label!");
        }
        
        VirtualCommand v = virtual.get(main_label);
        
        if (v.getSubCommand(sub_label) != null) {
            throw new IllegalArgumentException("A subcommand of the given sub label has already been registered for the given main label!");
        }
        
        VirtualCommand.VirtualSubCommand s = v.new VirtualSubCommand();
        v.subcommands.put(sub_label, s);
        
        s.console = action;
        s.player = action;
    }

    public static void registerUniversalSubCommand(String main_label, String sub_label, String permission, CommandAction<? super CommandSender> action) {
        if (virtual.get(main_label) == null) {
            throw new IllegalStateException("No virtual command has been registered under the given main label!");
        }

        VirtualCommand v = virtual.get(main_label);

        if (v.getSubCommand(sub_label) != null) {
            throw new IllegalArgumentException("A subcommand of the given sub label has already been registered for the given main label!");
        }

        VirtualCommand.VirtualSubCommand s = v.new VirtualSubCommand(permission);
        v.subcommands.put(sub_label, s);

        s.console = action;
        s.player = action;
    }
    
    public static void registerPlayerSubCommand(String main_label, String sub_label, CommandAction<? super Player> action) {
        if (virtual.get(main_label) == null) {
            throw new IllegalStateException("No virtual command has been registered under the given main label!");
        }
        
        VirtualCommand v = virtual.get(main_label);
        
        if (v.getSubCommand(sub_label) != null) {
            throw new IllegalStateException("A subcommand of the given sub label has already been registered for the given main label!");
        }
        
        VirtualCommand.VirtualSubCommand s = v.new VirtualSubCommand();
        v.subcommands.put(sub_label, s);
        
        s.player = action;
    }

    public static void registerPlayerSubCommand(String main_label, String sub_label, String permission, CommandAction<? super Player> action) {
        if (virtual.get(main_label) == null) {
            throw new IllegalStateException("No virtual command has been registered under the given main label!");
        }

        VirtualCommand v = virtual.get(main_label);

        if (v.getSubCommand(sub_label) != null) {
            throw new IllegalStateException("A subcommand of the given sub label has already been registered for the given main label!");
        }

        VirtualCommand.VirtualSubCommand s = v.new VirtualSubCommand(permission);
        v.subcommands.put(sub_label, s);

        s.player = action;
    }
    
    public static void registerConsoleSubCommand(String main_label, String sub_label, CommandAction<? super ConsoleCommandSender> action) {
        if (virtual.get(main_label) == null) {
            throw new IllegalStateException("No virtual command has been registered under the given main label!");
        }
        
        VirtualCommand v = virtual.get(main_label);
        
        if (v.getSubCommand(sub_label) != null) {
            throw new IllegalStateException("A subcommand of the given sub label has already been registered for the given main label!");
        }
        
        VirtualCommand.VirtualSubCommand s = v.new VirtualSubCommand();
        v.subcommands.put(sub_label, s);
        
        s.console = action;
    }
    
    public static void unregisterVirtualCommand(String label) {
        virtual.remove(label);
    }
    
    static VirtualCommand getCommand(String label) {
        return virtual.get(label);
    }
}
