package com.defiancecraft.defiancecommons.database.documents;

import java.util.List;
import java.util.UUID;

import com.mongodb.DBObject;

public class DBUser extends Document {

	public static final String FIELD_UUID = "uuid";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_GROUPS = "groups";
	
	public DBUser(DBObject obj) {
		super(obj);
	}
	
	public UUID getUUID() {
		return UUID.fromString(getString(FIELD_UUID)); 
	}

	public String getName() {
		return getString(FIELD_NAME);
	}
	
	public List<String> getGroups() {
		return getStringList(FIELD_GROUPS);
	}
	
}
