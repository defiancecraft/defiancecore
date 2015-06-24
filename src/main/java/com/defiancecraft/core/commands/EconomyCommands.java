package com.defiancecraft.core.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.defiancecraft.core.DefianceCore;
import com.defiancecraft.core.api.Economy;
import com.defiancecraft.core.api.Economy.InsufficientFundsException;
import com.defiancecraft.core.api.Economy.UserNotFoundException;
import com.defiancecraft.core.api.User;
import com.defiancecraft.core.command.ArgumentParser;
import com.defiancecraft.core.command.ArgumentParser.Argument;
import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.util.CommandUtils;

public class EconomyCommands {

	public static boolean help(CommandSender sender, String[] args) {
		
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
			"&9&lEconomy Help\n" +
			"&b- /token\n" +
			"&b- /token give <user> <amount>\n" +
			"&b- /token take <user> <amount>\n" +
			"&b- /token reset <user>\n" +
			"&b- /token bal\n" +
			"&b- /token balother <user>\n" +
			"&b- /token pay <user> <amount>\n" +
			"&b- /token redeem <amount>"
		));
		
		return true;
		
	}
	
	/*
	 * Command:    /token give <user> <amount>
	 * Permission: defiancecraft.eco.give
	 */
	public static boolean give(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME, Argument.DOUBLE);

		if (!parser.isValid()) {
			sender.sendMessage("Usage: /tokengive <user> <amount>");
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
	 * Command:    /token take <user> <amount>
	 * Permission: defiancecraft.eco.take
	 */
	public static boolean take(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME, Argument.DOUBLE);

		if (!parser.isValid()) {
			sender.sendMessage("Usage: /tokentake <user> <amount>");
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
	 * Command:    /token reset <user>
	 * Permission: defiancecraft.eco.reset
	 */
	public static boolean reset(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME);

		if (!parser.isValid()) {
			sender.sendMessage("Usage: /tokenreset <user>");
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
	 * Command:    /token bal
	 * Permission: defiancecraft.eco.bal
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
	 * Command:    /token balother
	 * Permission: defiancecraft.eco.balother
	 */
	public static boolean balOther(CommandSender sender, String[] args) {
	
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME);
		
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /tokenbalother <user>");
			return true;
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
	 * Command:    /token pay <user> <amount>
	 * Permission: defiancecraft.eco.pay
	 */
	public static boolean pay(CommandSender sender, String[] args) {
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME, Argument.DOUBLE);
		
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /tokenpay <user> <amount>");
			return true;
		}
		
		final String user       = parser.getString(1);
		final double amount     = parser.getDouble(2);
		final String senderName = sender.getName();
		final UUID senderUUID   = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console   = !(sender instanceof Player);
		
		if (user.equalsIgnoreCase(sender.getName())) {
			sender.sendMessage(ChatColor.RED + "Nice try.");
			return true;
		}
		
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
	
	/*
	 * Command:    /token redeem <amount>
	 * Permission: defiancecraft.eco.redeem
	 */
	public static boolean redeem(CommandSender sender, String[] args) {
		
		if (!(sender instanceof Player))
			return false;
		
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.INTEGER);
		
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /token redeem <amount>");
			return true;
		} else if (DefianceCore.getVault() == null){
			sender.sendMessage(ChatColor.RED + "Vault is not enabled on this server.");
			return true;
		} else if (DefianceCore.getVault().getName().equalsIgnoreCase("DefianceCore")) {
			sender.sendMessage(ChatColor.RED + "You cannot redeem on this server.");
			return true;
		}
			
		final int amount  = parser.getInt(1);
		final UUID uuid   = ((Player)sender).getUniqueId();
		final String name = sender.getName();
		
		sender.sendMessage(ChatColor.GRAY + "Redeeming " + Economy.format(amount) + "...");
		
		Database.getExecutorService().submit(() -> {
			
			try {
				Economy.withdraw(name, amount);
				DefianceCore.getVault().depositPlayer(Bukkit.getOfflinePlayer(uuid), amount);
				CommandUtils.trySend(uuid, "&aConverted %s into %s!", false, Economy.format(amount), DefianceCore.getVault().format(amount));
			} catch (InsufficientFundsException | UserNotFoundException e) {
				CommandUtils.trySend(uuid, "&cYou do not have enough money.", false);
			}
			
			
		});
		
		return true;
			
	}
	
}
