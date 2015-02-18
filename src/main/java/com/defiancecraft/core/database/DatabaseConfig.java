package com.defiancecraft.core.database;

import com.archeinteractive.defiancetools.util.JsonConfig;

public class DatabaseConfig extends JsonConfig {

	public String host = "localhost";
	public String username = "minecraft";
	public String password = "";
	public String database = "minecraft";
	public String serverId = "0102030405060708090a0b0c";
	
	public int port = 27017;
	public int threads = 10;
	
	public boolean usesAuth = false;
	
}
