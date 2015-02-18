package com.defiancecraft.core.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.archeinteractive.defiancetools.util.JsonConfig;

public class PermissionConfig extends JsonConfig {

	public String chatFormat = "{prefix}{suffix} {name}> {message}";
	public List<Group> groups = Arrays.asList(
		new Group("noob", "&4[NOOB]", "&9[Mod]", Arrays.asList("be.a.noob", "have.permissions"), Arrays.asList("inheritMe"), 7)
	);
	public List<String> defaultGroups = Arrays.asList("noob");
	
	public static class Group {
		
		public String name = "";
		public String prefix = "";
		public String suffix = "";
		public List<String> permissions = new ArrayList<String>();
		public List<String> inherit = new ArrayList<String>();
		public int priority = 0;
		
		// Private constructor for defining default groups
		Group(String name, String prefix, String suffix, List<String> permissions, List<String> inherit, int priority) {
			this.name = name;
			this.prefix = prefix;
			this.suffix = suffix;
			this.permissions = permissions;
			this.inherit = inherit;
			this.priority = priority;
		}
		
		// Public constructor for creating new groups
		public Group(String name) {
			this.name = name;
		}
		
		public List<String> getInherit() {
			return inherit != null ? inherit : new ArrayList<String>();
		}
		
		public List<String> getPermissions() {
			return permissions != null ? permissions : new ArrayList<String>();
		}
		
	}
	
	/**
	 * Gets a list of groups by their priority from
	 * the permissions.json file
	 * 
	 * @param ascending Whether it should return results in ascending order
	 * @return List<Group>
	 */
	public List<Group> getGroupsByPriority(boolean ascending) {
		
		List<Group> sorted = groups;
		Collections.sort(sorted, (groupA, groupB) -> {
			return Integer.compare(groupA.priority, groupB.priority);
		});
		
		if (!ascending)
			Collections.reverse(sorted);
		
		return sorted;
		
	}
	
	/**
	 * Gets a Group from the permissions.json file
	 * by name.
	 * 
	 * @param name Name of group
	 * @return Group object, or null if it wasn't found.
	 */
	public Group getGroup(String name) {
		
		for (Group g : groups)
			if (g.name.equalsIgnoreCase(name))
				return g;
		
		return null;
		
	}
	
	/**
	 * Gets all the permissions of a group in
	 * order (inherited first, the group itself's
	 * last)
	 * 
	 * @param g Group to get permissions of
	 * @return List of permissions
	 */
	public List<String> getPermissions(Group g) {
		
		List<String> perms = new ArrayList<String>();
		
		// Temporary group to prevent multiple getGroup() calls
		Group temp;
		
		// Iterate over inherited groups and recursively
		// add permissions
		for (String group : g.getInherit())
			if ((temp = getGroup(group)) != null)
				perms.addAll(getPermissions(temp));
		
		for (String perm : g.getPermissions())
			perms.add(perm);
		
		return perms;
		
	}
	
	/**
	 * Adds a permission to a group
	 * 
	 * @param groupName Name of group
	 * @param permission Permission to add
	 * @return Whether the group was found
	 */
	public boolean addPermission(String groupName, String permission) {
		
		for (int i = 0; i < groups.size(); i++) {
			Group g = groups.get(i);
			if (g.name.equals(groupName)) {
				g.permissions.add(permission);
				groups.set(i, g);
				return true;
			}
		}
		
		return false;
		
	}
	
	/**
	 * Removes a permission from a group
	 * 
	 * @param groupName Name of group
	 * @param permission Permission to remove
	 * @return Whether the group was found
	 */
	public boolean removePermission(String groupName, String permission) {
		
		for (int i = 0; i < groups.size(); i++) {
			Group g = groups.get(i);
			if (g.name.equals(groupName)) {
				g.permissions.remove(permission);
				groups.set(i, g);
				return true;
			}
		}
		
		return false;
		
	}
	
	/**
	 * Sets a group's prefix
	 * 
	 * @param groupName Name of group
	 * @param prefix New prefix value
	 * @return Whether the group was found
	 */
	public boolean setGroupPrefix(String groupName, String prefix) {
		
		for (int i = 0; i < groups.size(); i++) {
			Group g = groups.get(i);
			if (g.name.equals(groupName)) {
				g.prefix = prefix;
				groups.set(i, g);
				return true;
			}
		}
		
		return false;
		
	}
	
	/**
	 * Sets a group's suffix
	 * 
	 * @param groupName Name of group
	 * @param suffix New suffix value
	 * @return Whether the group was found
	 */
	public boolean setGroupSuffix(String groupName, String suffix) {
		
		for (int i = 0; i < groups.size(); i++) {
			Group g = groups.get(i);
			if (g.name.equals(groupName)) {
				g.suffix = suffix;
				groups.set(i, g);
				return true;
			}
		}
		
		return false;
		
	}
	
	/**
	 * Sets a group's priority
	 * 
	 * @param groupName Name of group
	 * @param priority New priority value
	 * @return Whether the group was found
	 */
	public boolean setGroupPriority(String groupName, int priority) {
		
		for (int i = 0; i < groups.size(); i++) {
			Group g = groups.get(i);
			if (g.name.equals(groupName)) {
				g.priority = priority;
				groups.set(i, g);
				return true;
			}
		}
		
		return false;
		
	}
	
}
