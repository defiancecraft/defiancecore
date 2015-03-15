package com.defiancecraft.core.database;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field of a Mongo Document to be indexed via
 * {@link com.mongodb.DBCollection#createIndex(com.mongodb.DBObject, com.mongodb.DBObject)},
 * with unique set as true.
 * 
 * Must be applied to a 'field', i.e.
 * <code>
 * @UniqueField
 * public static String FIELD_MYFIELD = "my_field";
 * </code>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface UniqueField {	
}
