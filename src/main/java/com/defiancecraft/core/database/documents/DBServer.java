package com.defiancecraft.core.database.documents;

import com.mongodb.DBObject;

public class DBServer extends Document {

	// Fields
	public static final String FIELD_NAME = "name";
	public static final String FIELD_IP = "ip";
	public static final String FIELD_PORT = "port";
	public static final String FIELD_GAMEMODE = "gamemode";
	public static final String FIELD_ONLINE = "online";
	public static final String FIELD_MAP = "map";
	public static final String FIELD_SLOTS = "slots";
	public static final String FIELD_OVERFLOW_SLOTS = "overflow_slots";
	public static final String FIELD_STATE = "state";
	
	public DBServer(DBObject obj) {
		super(obj);
	}
	
	public String getName() {
		return getString(FIELD_NAME);
	}
	
	public String getGamemode() {
		return getString(FIELD_GAMEMODE);
	}
	
	public int getOnline() {
		return getInt(FIELD_ONLINE);
	}
	
	public String getMap() {
		return getString(FIELD_MAP);
	}
	
	public int getSlots() {
		return getInt(FIELD_SLOTS);
	}
	
	public int getOverflowSlots() {
		return getInt(FIELD_OVERFLOW_SLOTS);
	}

	public String getIP() {
		return getString(FIELD_IP);
	}
	
	public int getPort() {
		return getInt(FIELD_PORT);
	}
	
	public String getState() {
		return getString(FIELD_STATE);
	}
	
	public void setState(String state) {
		this.dbo.put(FIELD_STATE, state);
	}

}
