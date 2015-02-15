package com.defiancecraft.defiancecommons.permissions;

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
	
	class Group {
		
		public String name = "";
		public String prefix = "";
		public String suffix = "";
		public List<String> permissions = new ArrayList<String>();
		public List<String> inherit = new ArrayList<String>();
		public int priority = 0;
		
		Group(String name, String prefix, String suffix, List<String> permissions, List<String> inherit, int priority) {
			this.name = name;
			this.prefix = prefix;
			this.suffix = suffix;
			this.permissions = permissions;
			this.inherit = inherit;
			this.priority = priority;
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
		if (g.inherit != null) {
			for (String group : g.inherit)
				if ((temp = getGroup(group)) != null)
					perms.addAll(getPermissions(temp));
		}
		
		for (String perm : g.permissions)
			perms.add(perm);
		
		return perms;
		
	}
	
}
