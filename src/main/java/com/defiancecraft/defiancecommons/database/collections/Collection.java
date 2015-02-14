package com.defiancecraft.defiancecommons.database.collections;

import com.defiancecraft.defiancecommons.database.Database;
import com.defiancecraft.defiancecommons.database.documents.Document;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public abstract class Collection {

	public Collection() {}

	public abstract String getCollectionName();
	
	/**
	 * Gets the name of the database associated
	 * with this collection (by default, this is
	 * the database specified in the config)
	 * 
	 * Can be overridden if Collection uses
	 * different database.
	 *  
	 * @return Database Name
	 */
	public String getDatabaseName() {
		
		return Database.getDB().getName();
		
	}
	
	/**
	 * Gets the database associated with this collection
	 * 
	 * @return DB
	 */
	public DB getDB() {
		
		return Database.getClient().getDB(getDatabaseName());
		
	}
	 
	/**
	 * Gets a DBCollection object for this Collection
	 * 
	 * @return DBCollection
	 */
	public DBCollection getDBC() {
		
		return getDB().getCollection(getCollectionName());
		
	}
	
	/**
	 * Saves a document in the collection
	 * 
	 * @param doc Document to save
	 */
	public void save(Document doc) {
		
		DBObject obj = doc.getDBO();
		getDBC().save(obj);
		
	}
	
	/**
	 * Performs an update operation
	 * 
	 * @param query Query for document to update
	 * @param data Update data
	 * @return WriteResult
	 */
	public WriteResult update(DBObject query, DBObject data) {
		
		return getDBC().update(query, data);
		
	}
	
}
