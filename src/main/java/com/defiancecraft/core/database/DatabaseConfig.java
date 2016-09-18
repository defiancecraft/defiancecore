package com.defiancecraft.core.database;

import com.defiancecraft.core.util.JsonConfig;

public class DatabaseConfig extends JsonConfig {

	public String host = "localhost";
	public String username = "minecraft";
	public String password = "";
	public String database = "minecraft";
	public String serverId = "0102030405060708090a0b0c";
	
	public int port = 27017;
	public int threads = 10;
	
	public boolean usesAuth = false;
	
	// The following config options are here because they are semi-relevant, 
	// and I am a lazy developer (saves creating a new config)
	public String baltopTitle = "&2&lTop Balances";
	public String baltopRow = "&a{place}) {name}, {balance}";
	public String baltopFooter = "&7Page {page}/{pageMax}, type &f/baltop {pageNext}&7 for next page";
	public int baltopPageMax = 10;
	public int baltopCacheSeconds = 10;
	
}
