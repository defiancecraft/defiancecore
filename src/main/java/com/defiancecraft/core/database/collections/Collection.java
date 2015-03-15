package com.defiancecraft.core.database.collections;

import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.database.documents.Document;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
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
	 * @throws MongoException Thrown if a database error occurs
	 * @return WriteResult
	 */
	public WriteResult save(Document doc) throws MongoException {
		
		DBObject obj = doc.getDBO();
		return getDBC().save(obj);
		
	}
	
	/**
	 * Performs an update operation
	 * 
	 * @param query Query for document to update
	 * @param data Update data
	 * @throws MongoException Thrown if a database error occurs
	 * @return WriteResult
	 */
	public WriteResult update(DBObject query, DBObject data) throws MongoException {
		
		return getDBC().update(query, data);
		
	}
	
	/**
	 * Performs an update operation with multi set to true
	 * 
	 * @param query Query for document to update
	 * @param data Update data
	 * @throws MongoException Thrown if a database error occurs
	 * @return WriteResult
	 */
	public WriteResult updateMulti(DBObject query, DBObject data) throws MongoException {
		
		return getDBC().updateMulti(query, data);
		
	}
	
	/**
	 * Gets a list of unique fields on the Collection (ones to
	 * be indexed with db.collection.createIndex())
	 * 
	 * @return Array of field names that must be unique
	 */
	public String[] getUniqueFields() {
		return new String[]{};
	}
	
}
