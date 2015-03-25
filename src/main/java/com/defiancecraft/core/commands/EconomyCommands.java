package com.defiancecraft.core.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.defiancecraft.core.api.Economy;
import com.defiancecraft.core.api.Economy.InsufficientFundsException;
import com.defiancecraft.core.api.Economy.UserNotFoundException;
import com.defiancecraft.core.api.User;
import com.defiancecraft.core.commands.ArgumentParser.Argument;
import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.util.CommandUtils;

public class EconomyCommands {

	public static boolean help(CommandSender sender, String[] args) {
		
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
			"&9&lEconomy Help\n" +
			"&b- /eco help\n" +
			"&b- /eco give <user> <amount>\n" +
			"&b- /eco take <user> <amount>\n" +
			"&b- /eco reset <user>\n" +
			"&b- /bal\n" +
			"&b- /balother <user>\n" +
			"&b- /pay <user> <amount>"
		));
		
		return true;
		
	}
	
	/*
	 * Command:    /eco give <user> <amount>
	 * Permission: defiancecraft.eco.give
	 */
	public static boolean give(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME, Argument.DOUBLE);

		if (!parser.isValid()) {
			sender.sendMessage("Usage: /eco give <user> <amount>");
			return true;
		}
		
		final String user      = parser.getString(1);
		final double amount    = parser.getDouble(2);
		final UUID senderUUID  = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console = !(sender instanceof Player);
		
		Database.getExecutorService().submit(() -> {
			
			Economy.deposit(user, amount);
			CommandUtils.trySend(senderUUID, "&aFunds added.", console);
			
		});
		
		sender.sendMessage(String.format(ChatColor.GRAY + "Giving %s %s.", user, Economy.format(amount)));
		return true;
		
	}
	
	/*
	 * Command:    /eco take <user> <amount>
	 * Permission: defiancecraft.eco.take
	 */
	public static boolean take(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME, Argument.DOUBLE);

		if (!parser.isValid()) {
			sender.sendMessage("Usage: /eco take <user> <amount>");
			return true;
		}
		
		final String user      = parser.getString(1);
		final double amount    = parser.getDouble(2);
		final UUID senderUUID  = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console  = !(sender instanceof Player);
		
		Database.getExecutorService().submit(() -> {
			
			try {
				Economy.withdraw(user, amount);
				CommandUtils.trySend(senderUUID, "&aFunds added.", console);
			} catch (InsufficientFundsException e) {
				CommandUtils.trySend(senderUUID, "&cPlayer does not have eough money.", console);
			} catch (UserNotFoundException e) {
				CommandUtils.trySend(senderUUID, "&cPlayer not found.", console);
			}
			
		});
		
		sender.sendMessage(String.format(ChatColor.GRAY + "Taking %s from %s.", Economy.format(amount), user));
		return true;
		
	}
	
	/*
	 * Command:    /eco reset <user>
	 * Permission: defiancecraft.eco.reset
	 */
	public static boolean reset(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME);

		if (!parser.isValid()) {
			sender.sendMessage("Usage: /eco reset <user>");
			return true;
		}
		
		final String user     = parser.getString(1);
		final UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console = !(sender instanceof Player);
		
		Database.getExecutorService().submit(() -> {

			try {
				Economy.setBalance(user, 0);
				CommandUtils.trySend(senderUUID, "&aReset user's balance.", console);
			} catch (UserNotFoundException e) {
				CommandUtils.trySend(senderUUID, "&cPlayer not found.", console);
			}
			
		});
		
		sender.sendMessage(String.format(ChatColor.GRAY + "Resetting balance of user '%s'.", user));
		return true;
		
	}
	
	/*
	 * Command:    /bal
	 * Permission: defiancecraft.bal
	 */
	public static boolean bal(CommandSender sender, String[] args) {
		
		final UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console = !(sender instanceof Player);
		
		Database.getExecutorService().submit(() -> {
			
			double balance = Economy.getBalance(senderUUID);
			CommandUtils.trySend(senderUUID, "&aBalance: %s", console, Economy.format(balance));
			
		});
	
		return true;
		
	}
	
	/*
	 * Command:    /balother
	 * Permission: defiancecraft.balother
	 */
	public static boolean balOther(CommandSender sender, String[] args) {
	
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME);
		
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /balother <user>");
			
		}
		
		final String user     = parser.getString(1);
		final UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console = !(sender instanceof Player);
		
		Database.getExecutorService().submit(() -> {
			
			User u = User.findByName(user);
			if (u == null) {
				CommandUtils.trySend(senderUUID, "&cUser '%s' not found.", console, user);
				return;
			}
			
			CommandUtils.trySend(senderUUID, "&aBalance: %s", console, Economy.format(u.getDBU().getBalance())); 
			
		});
		
		return true;
		
	}
	
	/*
	 * Command:    /pay <user> <amount>
	 * Permission: defiancecraft.pay
	 */
	public static boolean pay(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME, Argument.DOUBLE);
		
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /pay <user> <amount>");
			return true;
		}
		
		final String user       = parser.getString(1);
		final double amount     = parser.getDouble(2);
		final String senderName = sender.getName();
		final UUID senderUUID   = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console   = !(sender instanceof Player);
		
		Database.getExecutorService().submit(() -> {

			User u = User.findByNameOrCreate(user);
			if (u == null) {
				CommandUtils.trySend(senderUUID, "&cUser %s not found.", console, user);
				return;
			}
			
			try {
				
				Economy.withdraw(senderName, amount);
				u.setBalance(u.getDBU().getBalance() + amount);
				
				if (u.getDBU().getUUID() != null
						&& Bukkit.getPlayer(u.getDBU().getUUID()) != null)
					Bukkit.getPlayer(u.getDBU().getUUID()).sendMessage(ChatColor.GREEN + senderName + " sent you " + Economy.format(amount) + "!");
				
				CommandUtils.trySend(senderUUID, "&aSent %s to %s!", console, Economy.format(amount), user);
				
			} catch (UserNotFoundException e) {
				CommandUtils.trySend(senderUUID, "&cA database error occurred.", console);
				return;
			} catch (InsufficientFundsException e) {
				CommandUtils.trySend(senderUUID, "&cYou do not have enough money.", console);
				return;
			}
			
		});
		
		return true;
		
	}
	
}
