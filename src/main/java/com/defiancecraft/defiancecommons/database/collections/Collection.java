package com.defiancecraft.defiancecommons.database.collections;

import com.defiancecraft.defiancecommons.database.Database;
import com.mongodb.DB;
import com.mongodb.DBCollection;

public abstract class Collection {

	public Collection() {
		
		
		
	}
	
	public abstract String getDatabaseName();
	
	public abstract String getCollectionName();
	
	public DB getDB() {
		
		return Database.getClient().getDB(getDatabaseName());
		
	}
	 
	// TODO: finish collection class, add getColl method to db and findX methods to coll
	
	public DBCollection getDBC() {
		
		return getDB().getCollection(getCollectionName());
		
	}
	
}
