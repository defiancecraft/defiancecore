DefianceCore
============

This is the core library for DefianceCraft servers, containing features primarily for 
**modules**, but also features which work on the server too. 

## Contents
- [Modules](#modules)
  * [Description](#mod_description)
  * [Enabling/Disabling Modules](#mod_enabledisable)
  * [Naming Modules](#mod_naming)
  * [Creating Modules](#mod_creating)
  * [Configuration Files](#mod_config)
- [Database](#database)
  * [Description](#db_desc)
  * [Configuring the Database](#db_config)
  * [Server IDs](#db_serverids)
  * [Using the Database](#db_using)
- [Permissions](#permissions)
  * [Description](#perm_desc)
  * [Configuring Permissions](#perm_config)
- [Links](#links)
  * [JavaDoc](./target/apidocs/)
  * [Command Reference](https://docs.google.com/spreadsheets/d/1AsjNJZAYPjH1NP42P3e5Ux7jehqOX95aOjguHJ_ikxg/edit?usp=sharing)
- [License](#license)

## Modules<a name="modules"></a>

### Description<a name="mod_description"></a>
A module is an _independent_ extension of the core to provide new functionality. Note that modules are not intended to act as libraries, and should be able to work in the presence of other modules.

### Enabling/Disabling Modules<a name="mod_enabledisable"></a>
Modules can be enabled or disabled through the [config file](#configuration), `modules.json`. Modules will be loaded from the `modules` folder, in the DefianceCore root folder (`plugins/DefianceCraft/`). They must **not be renamed**, as they are loaded based on their names.

To enable a module, simply add the name of the module (case sensitive) to `enabledModules`, e.g.
```json
{
  "enabledModules": [ "HeroNames", "BanHammer" ]
}
```

### Naming Modules<a name="mod_naming"></a>
Module jars may be named anything, however they **must** end with .dc.jar. If they are not named so, or are renamed in the modules folder, they will **not be loaded**.

e.g. `MyModule.dc.jar`

### Creating Modules<a name="mod_creating"></a>
All modules must implement the `Module` class, although the most common (and useful) implementation contained within DefianceCore is the `JavaModule` class, used like so:
```java
public class MyModule extends JavaModule {
  
    public void onEnable() {
        // TODO: onEnable()
    }
  
}
```

In a JavaModule, the canonical name for a module (i.e. the name used for config files) is the [simple name](http://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getSimpleName(%29) of the class. This method (and others) may be overridden, if the canonical name is different to the class name. See the [JavaDoc](./target/apidocs) to see methods that may be overridden.

### Configuration Files<a name="mod_config"></a>
As of DefianceCore v1.2.0, modules have their own configuration files, stored in the `configs` folder in the DefianceCore root folder. This functionality may be obtained if a module extends JavaModule, which implements the method [Module#getConfig(Class)](./target/apidocs/com/defiancecraft/core/modules/Module.html#getConfig-java.lang.Class-).

A module may simply call this method to retrieve an instance of given Class loaded with the config values. If the config is not present, it will be created. See the [JavaModule](TODO/com/defiancecraft/core/modules/impl/JavaModule.html) for the methods that may be used to interact with configs.

JavaModule configs may be in either YAML or JSON format. The preferred format, defined in the `modules.json` file, will be used if the config is present in both formats.

## Database<a name="database"></a>

### Description<a name="db_desc"></a>
DefianceCore uses a MongoDB database to store and retrieve data. The server **will not load** unless the server ID is present in the database config, and the database can be accessed.

### Configuring the Database<a name="db_config"></a>
The database config is stored in the JSON file, `db.json` (in the DefianceCore root folder). An example config with explanations of fields is shown below:
```javascript
{
  "host": "localhost", // Hostname of the MongoDB server
  "password": "", // Password of account
  "username": "", // Username of account with access to 'database'
  "database": "defiancecore", // Database to use
  "serverId": "54ff770b407ca72489365982", // The server ID (explained below)
  "port": 27017, // MongoDB server port
  "threads": 10, // Number of threads to use in Database's ExecutorService
  "usesAuth": false // Whether the database used authentication
}
```

### Server IDs<a name="db_serverids"></a>
Each server using DefianceCore must have a unique server ID. A server ID is the `_id` field of a server in the `servers` collection, under the same DB as stated in the config. Server IDs currently (as of v1.2.0) have no practical use other than to verify the working of the database, but may in future.

Server IDs may be generated using these simple mongo commands:
```javascript
use <dbname>
db.createCollection('servers');
db.servers.insert({
  name: '<name to identify server>'
});
db.servers.find(); // Will list servers and their IDs; may be modified with server name as search criteria
```

### Using the Database<a name="db_using"></a>
Modules may override the method [Module#getCollections()](./target/apidocs/com/defiancecraft/core/modules/Module.html#getCollections--) to have their Collection classes loaded and registered. They may then be retrieved via [Database#getCollection(Class)](./target/apidocs/com/defiancecraft/core/database/Database.html#getCollection-java.lang.Class-). Consult the JavaDoc for further information on using the Database functionality in DefianceCore.

In general, Database tasks should be run using the provided [ExecutorService](./target/apidocs/com/defiancecraft/core/database/Database.html#getExecutorService--) to prevent blocking tasks from slowing the server. There are exceptions to this, however.

For examples of Database usage, see the collections and documents used within DefianceCore in the `com.defiancecraft.core.database` package (and subpackages).

## Permissions<a name="permissions"></a>

### Description<a name="perm_desc"></a>
In general, permissions work the same as they do in Bukkit, and may be checked in the same way. However, permissions are given to players based on their groups, which are stored in the database. The permissions given are dependent on the contents of the permissions file, `permissions.json`, in each server. Players can be given permissions via command; see the [command reference](https://docs.google.com/spreadsheets/d/1AsjNJZAYPjH1NP42P3e5Ux7jehqOX95aOjguHJ_ikxg/edit?usp=sharing) for details on this.

### Configuring Permissions
The permissions given to groups, group prefixes, and chat formatting are configured per server in the `permissions.json` file. An example of this file and explanations below are given.
```javascript
{
  "chatFormat": "{prefix}{suffix} {name}&e {message}", // How messages are formatted in chat
  "groups": [
    {
      "name": "player",          // Name of group to give permissions to
      "prefix": "[Player]",      // Prefix of players with group
      "permissions": [           // List of permissions given to players in this group
        "defiancecraft.eco.bal",
        "defiancecraft.eco.pay"
      ], 
      "priority": 0             // Priority of group (higher = prefix prioritised over lower priority)
    }
    // ...
  ],
  "defaultGroups": [ // Default groups given to players with no groups
    "player"
  ]
}
```

## Building<a name="building"></a>
DefianceCore (and all modules **should**) uses Maven for building and compiling. Simply run `mvn clean install` to build DefianceCore, adding the appropriate options for exporting javadoc/source if desired.

## Links<a name="links"></a>

### [JavaDoc](./target/apidocs)
### [Command Reference](https://docs.google.com/spreadsheets/d/1AsjNJZAYPjH1NP42P3e5Ux7jehqOX95aOjguHJ_ikxg/edit?usp=sharing)

## License<a name="license"></a>
You may:
- Contribute to DefianceCore through pull requests and commits to source
- Use DefianceCore and associated modules on the designated DefianceCraft-owned servers and on local testing servers

You may not:
- Disclose the source of DefianceCore, modules, or any associated code.
- Redistribute DefianceCore or module binaries/jars/source code.
- Use DefianceCore for commercial or personal use outside of DefianceCraft