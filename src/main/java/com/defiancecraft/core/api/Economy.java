package com.defiancecraft.core.api;

import java.util.UUID;

import com.archeinteractive.defiancetools.util.JsonConfig;
import com.defiancecraft.core.util.FileUtils;

/**
 * This class provides access to economy functions, such as
 * withdrawing amounts of money, while abstracting the process
 * of accessing the database, etc.
 * 
 * It is important to note that none of these methods are ran
 * asynchronously (i.e. in the Database's ExecutorService) as
 * different plugins (i.e. Vault) require immediate responses
 * from this API. Executing these methods asynchronously is up
 * to the class(es) which uses them.
 */
public class Economy {

	private static EconomyConfig config; 
	
	/**
	 * Formats an amount of currency to the format
	 * set in the configuration
	 * 
	 * @param amount Amount of currency
	 * @return Formatted amount
	 */
	public static String format(double amount) {
		return getConfig().format
			.replace("{symbol}", config.currencySymbol)
			.replace("{amount}", Double.toString(amount));
	}
	
	/**
	 * Gets the currency's name in plural form
	 * 
	 * @return String
	 */
	public static String getCurrencyNamePlural() {
		return getConfig().currencyPlural;
	}

	/**
	 * Gets the currency's name in singular form
	 * 
	 * @return String
	 */
	public static String getCurrencyNameSingular() {
		return getConfig().currencySingular;
	}
	
	/**
	 * Checks whether a player exists in the database.
	 * 
	 * @param name Name of player
	 * @return Whether player exists
	 */
	@SuppressWarnings("deprecation")
	public static boolean playerExists(String name) {
		return User.exists(name);
	}
	
	/**
	 * Gets the balance of a player
	 * 
	 * @param name Name of player
	 * @return Player's balance
	 */
	public static double getBalance(String name) {
		
		User u = User.findByName(name);
		if (u == null)
			return 0d;
		
		return u.getDBU().getBalance();
		
	}
	
	/**
	 * Gets the balance of a player
	 * 
	 * @param uuid UUID of player
	 * @return Player's balance
	 */
	public static double getBalance(UUID uuid) {
		
		User u = User.findByUUID(uuid);
		if (u == null)
			return 0d;
		
		return u.getDBU().getBalance();
		
	}
	
	/**
	 * Withdraws an amount of money from the player.
	 * 
	 * @param name Name of player to withdraw money from
	 * @param amount Amount to withdraw
	 * @throws UserNotFoundException Thrown when the user was not found
	 * @throws InsufficientFundsException Thrown when the user does not have sufficient money
	 */
	public static void withdraw(String name, double amount) throws UserNotFoundException, InsufficientFundsException {
		
		User u = User.findByName(name);
		if (u == null)
			throw new UserNotFoundException();
		
		if (u.getDBU().getBalance() - amount < 0)
			throw new InsufficientFundsException();
		
		u.setBalance(u.getDBU().getBalance() - amount);
		
	}
	
	/**
	 * Deposits an amount of money to a player.
	 * Will create the player if they don't exist (note - this
	 * will mean that a request is sent to Mojang, which could
	 * take a while)
	 * 
	 * @param name Name of player whose account the money should be deposited into
	 * @param amount Amount of money to deposit
	 */
	public static void deposit(String name, double amount) {
		
		// Withdraw negative-amount in order to add the money
		// into their account, rather than withdraw it.
		try {
			
			Economy.withdraw(name, -amount);
			
		} catch (InsufficientFundsException e) { // Should never be thrown, unless given amount is negative.
		} catch (UserNotFoundException e) {
			
			// Deposit directly through User class in order to
			// avoid multiple unnecessary queries (createAccount
			// + withdraw is three queries)
			User u = User.findByNameOrCreate(name);
			if (u != null)
				u.setBalance(u.getDBU().getBalance() + amount);
			
		}
		
	}
	
	public static void setBalance(String name, double amount) throws UserNotFoundException {
		
		User u = User.findByNameOrCreate(name);
		if (u == null)
			throw new UserNotFoundException();
		
		u.setBalance(amount);
		
	}
	
	/**
	 * Creates an account for a user.
	 * 
	 * @param name Name of player
	 * @return Whether the account was created.
	 */
	public static boolean createAccount(String name) {
		
		return User.findByNameOrCreate(name) != null;
		
	}
	
	/**
	 * Gets the configuration, or loads it if it
	 * wasn't loaded already.
	 * 
	 * @return EconomyConfig instance
	 */
	public static EconomyConfig getConfig() {
		if (config == null)
			config = JsonConfig.load(FileUtils.getSharedConfig("economy.json"), EconomyConfig.class);
		return config;
	}
	
	public class EconomyConfig extends JsonConfig {
		
		public String currencySingular = "token";
		public String currencyPlural = "tokens";
		public String currencySymbol = "T";
		public String format = "{symbol}{amount}";
		
	}
	
	public static class UserNotFoundException extends Exception {
		
		private static final long serialVersionUID = 6722196863427059797L;
		public String getMessage() { return "User was not found in the database while trying to perform an action on them."; };
		
	}
	
	public static class InsufficientFundsException extends Exception {
		
		private static final long serialVersionUID = 4032794242199369766L;
		public String getMessage() { return "User does not have enough money."; }
		
	}
	
}
