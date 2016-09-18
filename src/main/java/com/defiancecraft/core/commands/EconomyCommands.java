package com.defiancecraft.core.commands;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.defiancecraft.core.api.Economy;
import com.defiancecraft.core.api.Economy.InsufficientFundsException;
import com.defiancecraft.core.api.Economy.UserNotFoundException;
import com.defiancecraft.core.api.User;
import com.defiancecraft.core.command.ArgumentParser;
import com.defiancecraft.core.command.ArgumentParser.Argument;
import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.database.collections.Users;
import com.defiancecraft.core.database.documents.DBUser;
import com.defiancecraft.core.util.CommandUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class EconomyCommands {

	private static LoadingCache<Integer, List<DBUser>> baltopCache;
	
	public EconomyCommands() {
		 baltopCache = CacheBuilder.newBuilder()
						.expireAfterWrite(Database.getConfig().baltopCacheSeconds, TimeUnit.SECONDS)
						.build(new CacheLoader<Integer, List<DBUser>>() {

							@Override
							public List<DBUser> load(Integer page) throws Exception {
								int pageMax = Database.getConfig().baltopPageMax;
								return Database.getCollection(Users.class).findRichestUsers()
									.skip(page * pageMax)
									.limit(pageMax)
									.toArray(pageMax)
									.stream()
									.map((u) -> new DBUser(u))
									.collect(Collectors.toList());
							}
							
						});
	}
			
	
	public boolean help(CommandSender sender, String[] args) {
		
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
			"&9&lEconomy Help\n" +
			"&b- /eco help\n" +
			"&b- /eco give <user> <amount>\n" +
			"&b- /eco take <user> <amount>\n" +
			"&b- /eco reset <user>\n" +
			"&b- /bal [user]\n" +
			"&b- /balother <user>\n" +
			"&b- /pay <user> <amount>"
		));
		
		return true;
		
	}
	
	/*
	 * Command:    /eco give <user> <amount>
	 * Permission: defiancecraft.eco.give
	 */
	public boolean give(CommandSender sender, String[] args) {
		
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
	public boolean take(CommandSender sender, String[] args) {
		
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
	public boolean reset(CommandSender sender, String[] args) {
		
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
	public boolean bal(CommandSender sender, String[] args) {
		
		final UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console = !(sender instanceof Player);
		final String target = args.length > 0 ? args[0] : null;
		
		Database.getExecutorService().submit(() -> {
			
			if (target == null) {
				double balance = Economy.getBalance(senderUUID);
				CommandUtils.trySend(senderUUID, "&aBalance: %s", console, Economy.format(balance));
			} else {
				User u = User.findByName(target);
				
				// If user does not exist, inform sender
				if (u == null)
					CommandUtils.trySend(senderUUID, "&cUser '%s' not found.", console, target);
				else
					CommandUtils.trySend(senderUUID, "&aBalance for %s: %s", console, target, Economy.format(u.getDBU().getBalance()));
			}
			
		});
	
		return true;
		
	}
	
	/*
	 * Command:    /baltop
	 * Permission: defiancecraft.baltop
	 */
	public boolean balTop(CommandSender sender, String[] args) {
		
		final UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
		final boolean console = !(sender instanceof Player);
		int parsedPage = 0;
		
		if (args.length > 0) {
			try {
				int pageInt = Integer.parseInt(args[0]);
				
				// Ensure valid page
				if (pageInt <= 0)
					pageInt = 1;
				
				// Subtract 1 (pages start at 0)
				parsedPage = pageInt - 1;
			} catch (NumberFormatException e) {}
		}

		// *sigh*
		final int finalParsedPage = parsedPage;
		
		Database.getExecutorService().submit(() -> {
			
			long numUsers = Database.getCollection(Users.class).getNumberOfUsers(); // Number of users to calculate pages
			
			// Calculate pages
			int pageLimit = Database.getConfig().baltopPageMax;	
			int maxPages  = (int)Math.ceil(numUsers / (double)pageLimit);
			int page = finalParsedPage;

			// If page exceeds maximum, set to last page
			if (page >= maxPages)
				page = maxPages - 1;

			// Create StringBuilder for output message and add title
			StringBuilder baltopBuilder = new StringBuilder();
			baltopBuilder.append(
				Database.getConfig().baltopTitle
					.replace("{page}", Integer.toString(page + 1))
					.replace("{pageNext}", Integer.toString(page + 2))
					.replace("{pageMax}", Integer.toString(maxPages))
			).append("\n");
			
			// Retrieve from cache (or write to cache)
			List<DBUser> dbUsers;
			try {
				dbUsers = baltopCache.get(page);
			} catch (Exception e) {
				CommandUtils.trySend(senderUUID, "An error occurred.", console);
				return;
			}
			
			// Iterate over DBUsers
			for (int i = 0; i < dbUsers.size(); i++) {
				DBUser user = dbUsers.get(i);
				String place = Integer.toString(i + 1 + page * pageLimit); // Find place, factoring in skipped users
				String name = user.getName(); // Name can be null if player hasn't connected and their name not present
				
				// Append row in set format
				baltopBuilder.append(
					Database.getConfig().baltopRow
						.replace("{place}", place)
						.replace("{balance}", Economy.format(user.getBalance()))
						.replace("{name}", name == null ? "<unknown>" : name)
				).append("\n");
			}
			
			// Append footer to builder
			baltopBuilder.append(
				Database.getConfig().baltopFooter
					.replace("{page}", Integer.toString(page + 1))
					.replace("{pageNext}", Integer.toString(page + 2))
					.replace("{pageMax}", Integer.toString(maxPages))
			);
			
			// Finally, send message to user
			CommandUtils.trySend(senderUUID, baltopBuilder.toString(), console);
			
		});
		
		
		
		return true;
		
	}
	
	/*
	 * Command:    /balother
	 * Permission: defiancecraft.balother
	 */
	public boolean balOther(CommandSender sender, String[] args) {
	
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.USERNAME);
		
		if (!parser.isValid()) {
			sender.sendMessage("Usage: /balother <user>");
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
	 * Command:    /pay <user> <amount>
	 * Permission: defiancecraft.pay
	 */
	public boolean pay(CommandSender sender, String[] args) {
		
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
	
}
