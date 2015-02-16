package com.defiancecraft.defiancecommons.permissions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import com.archeinteractive.defiancetools.util.JsonConfig;
import com.defiancecraft.defiancecommons.database.Database;
import com.defiancecraft.defiancecommons.database.collections.Users;
import com.defiancecraft.defiancecommons.database.documents.DBUser;
import com.defiancecraft.defiancecommons.permissions.PermissionConfig.Group;
import com.defiancecraft.defiancecommons.util.FileUtils;

public class PermissionManager {

	public static final String METADATA_KEY = "dcPerms";
	
	private Map<UUID, PermissionAttachment> attachments = new HashMap<UUID, PermissionAttachment>();
	private Plugin plugin;
	private PermissionConfig config;
	
	public PermissionManager(Plugin plugin) {
		
		this.plugin = plugin;
		reloadConfig();
		
	}
	
	/**
	 * Get the config
	 * 
	 * @return PermissionConfig
	 */
	public PermissionConfig getConfig() {
		
		return config;
		
	}
	
	/**
	 * Retrieves the PermissionAttachment object
	 * from the UUID. Returns null if it does not
	 * exist.
	 * 
	 * @param uuid UUID of player
	 * @return The player's PermissionAttachment, or null 
	 */
	public PermissionAttachment getAttachment(UUID uuid) {
		
		return attachments.containsKey(uuid) ? attachments.get(uuid) : null;
		
	}
	
	/**
	 * Retrieves the PermissionAttachment object
	 * for a Player, creating it if necessary, unlike
	 * the equivalent function which takes an ID
	 * 
	 * @param player The Player to get/create the attachment for
	 * @return The [current/newly created] PermissionAttachment
	 */
	public PermissionAttachment setAttachment(Player player) {
		
		UUID uuid = player.getUniqueId();
		
		if (!attachments.containsKey(uuid)) {
			PermissionAttachment attachment = player.addAttachment(plugin);
			attachments.put(uuid, attachment);
		}
		
		return attachments.get(uuid);
		
	}
	
	/**
	 * Removes the plugin's PermissionAttachment from
	 * a player, removing it from the Map too.
	 * 
	 * @param p Player to remove attachment from
	 */
	public void removeAttachment(Player p) {
		
		UUID uuid = p.getUniqueId();
		PermissionAttachment attachment = getAttachment(uuid);
		
		if (attachment == null)
			return;
		
		p.removeAttachment(attachment);
		attachments.remove(uuid);
		
	}
	
	/**
	 * Removes all PermissionAttachments from players
	 */
	public void removeAllAttachments() {
		
		Iterator<? extends Player> players = Bukkit.getOnlinePlayers().iterator();
		while (players.hasNext())
			removeAttachment(players.next());
		
	}
	
	/**
	 * Gets the prefix and suffix of a user
	 * 
	 * @param user User to get prefix & suffix of
	 * @return Array of two strings: [prefix, suffix]
	 */
	private String[] getPrefixAndSuffix(DBUser user) {
		
		List<Group> groups = config.getGroupsByPriority(true);
		String prefix = "",
			   suffix = "";
		
		for (Group g : groups) {
			
			if (!user.getGroups().contains(g.name))
				continue;

			if (g.prefix != null && !g.prefix.isEmpty())
				prefix = g.prefix;
			
			if (g.suffix != null && !g.suffix.isEmpty())
				suffix = g.suffix;
			
		}
		
		// Set custom prefix if existent
		if (user.getCustomPrefix() != null && !user.getCustomPrefix().isEmpty())
			prefix = user.getCustomPrefix();
		
		// Set custom suffix if existent
		if (user.getCustomSuffix() != null && !user.getCustomSuffix().isEmpty())
			suffix = user.getCustomSuffix();
		
		return new String[] { prefix, suffix };
		
	}
	
	/**
	 * Gets the prefix of a user
	 * 
	 * @param user User to get prefix of
	 * @return Prefix
	 */
	public String getPrefix(DBUser user) {
		
		return getPrefixAndSuffix(user)[0];
		
	}
	
	/**
	 * Gets the suffix of a user
	 * 
	 * @param user User to get suffix of
	 * @return Suffix
	 */
	public String getSuffix(DBUser user) {
		
		return getPrefixAndSuffix(user)[1];
		
	}
	
	/**
	 * Sets a player's metadata (i.e. their prefix & suffix)
	 * 
	 * @param p Player to set metadata of
	 * @param meta PermissionMetadata object
	 */
	public void setMetadata(Player p, PermissionMetadata meta) {
		
		if (p.hasMetadata(METADATA_KEY))
			p.removeMetadata(METADATA_KEY, plugin);
		
		MetadataValue value = new FixedMetadataValue(plugin, PermissionMetadata.serialize(meta));
		p.setMetadata(METADATA_KEY, value);
		
	}
	
	/**
	 * Gets a player's metadata, if it exists. Otherwise,
	 * returns null.
	 * 
	 * @param p Player to get metadata of
	 * @return PermissionMetadata Object, or null
	 */
	public PermissionMetadata getMetadata(Player p) {
		
		if (p.getMetadata(METADATA_KEY).size() > 0)
			   return PermissionMetadata.deserialize(p.getMetadata(METADATA_KEY).get(0).asString());
			  
		return null;
		
	}
	
	/**
	 * Sets the permissions on the PermissionAttachment
	 * for a player, querying the database for their groups
	 * or creating a player if necessary.
	 * 
	 * @param player Player to set permissions of
	 * @deprecated Unless wanting only to update the
	 * permissions of a player and not metadata, the {@link #updatePlayer(Player)}
	 * method should be used instead to avoid multiple DB
	 * queries.
	 */
	@Deprecated
	public void updatePermissions(Player player) {
		
		Users users = Database.getCollection(Users.class);
		DBUser user = users.getUserOrCreate(player);
		
		updatePermissions(player, user);
		
	}

	/**
	 * Sets the permissions on the PermissionAttachment
	 * for a player using an existing DBUser object.
	 * This method should be used to avoid frequent DB
	 * queries.
	 * 
	 * @param player Player to set permissions of
	 * @param user DBUser object for player
	 */
	public void updatePermissions(Player player, DBUser user) {
		
		PermissionAttachment attachment = setAttachment(player);
		
		// Clear current permissions from attachment
		for (String permission : attachment.getPermissions().keySet())
			attachment.unsetPermission(permission);
		
		for (Group g : config.getGroupsByPriority(true)) {
			
			// We'll probably want to skip if the
			// user doesn't have this group...
			if (!user.getGroups().contains(g.name))
				continue;

			// Get ALL permissions for group (i.e.
			// inherited ones too)
			for (String perm : config.getPermissions(g))
				attachment.setPermission(perm.replace("^", ""), perm.contains("^") ? false : true);
			
		}
		
		player.recalculatePermissions();
		
	}
	
	/**
	 * Updates a player's metadata, querying the database
	 * for the user first. Should not be used when, for example,
	 * a player's rank changes, use {@link #updatePlayer(Player)}
	 * for that.
	 * 
	 * This method should be used, for example, if a player's
	 * metadata is null, and needs updating.
	 *
	 * @param player Player to update metadata of
	 */
	public void updateMetadata(Player player) {
		
		DBUser user = Database
				.getCollection(Users.class)
				.getUserOrCreate(player);
		
		updateMetadata(player, user);
		
	}
	
	/**
	 * Updates a player's metadata using an existing
	 * DBUser object. This method should be used to
	 * avoid frequent DB queries.
	 * 
	 * @param player Player to update
	 * @param user DBUser object to use
	 */
	public void updateMetadata(Player player, DBUser user) {
		
		// Create PermissionMetadata from the prefix
		// and suffix of the user
		String[] data = getPrefixAndSuffix(user);
		PermissionMetadata meta = new PermissionMetadata(data[0], data[1]);
		
		setMetadata(player, meta);		
		
	}

	/**
	 * Updates the permissions and metadata for a player, using
	 * a single database query. This should be used in favour of
	 * {@link #updateMetadata(Player)} and {@link #updatePermissions(Player)}.
	 * @param player Player to update
	 */
	public void updatePlayer(Player player) {
		
		DBUser user = Database.getCollection(Users.class).getUserOrCreate(player);
		updatePermissions(player, user);
		updateMetadata(player, user);
		
	}
	
	/**
	 * Updates all players from the Database. Should be called
	 * after a reload, or on enabling the plugin.
	 */
	public void reload() {
		
		for (Player p : Bukkit.getOnlinePlayers())
			updatePlayer(p);
		
	}
	
	/**
	 * Reloads the configuration from file
	 */
	public void reloadConfig() {
		
		this.config = JsonConfig.load(FileUtils.getSharedConfig("permissions.json"), PermissionConfig.class);
		
	}
	
	/**
	 * Saves the configuration to file
	 */
	public void saveConfig() {
		
		this.config.save(FileUtils.getSharedConfig("permissions.json"));
		
	}
	
}
