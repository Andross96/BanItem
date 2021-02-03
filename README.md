# BanItem ![GPL-3.0](http://cdn.andross.fr/badges/license.svg) ![Stable](http://cdn.andross.fr/badges/stable.svg) ![Version](http://cdn.andross.fr/badges/v3.0.1.svg) ![Discord](http://cdn.andross.fr/badges/discord.svg)

### Description
Lightweight, powerful & configurable per world ban item plugin

### Features:
* Support all bukkit versions;
* Blacklist: players will not be able to use the item, per option configurable;
* Whitelist: (reversed blacklist) - all items & options will be blocked , you'll have to set which items is allowed with which option;
* Per world configurable;
* Per world per item permissions;
* Per world per item per option permissions;
* Support of custom/modded items (items with specific item data/meta, like modded items, potions, egg spawners for old versions...);
* Usefull commands;
* Lightweight & activating only necessary listeners;
* Developer API available;

![stats](https://bstats.org/signatures/bukkit/BanItem.svg)

# Developer API
```xml
<repository>
  <id>andross-repo</id>
  <url>http://repo.andross.fr/</url>
</repository>

<dependency>
  <groupId>fr.andross</groupId>
  <artifactId>BanItemPlugin</artifactId>
  <version>3.0.1</version>
  <scope>provided</scope>
</dependency>
```

For any questions/suggestions: PM me on spigot or on discord (Andross#5254).

# Get the API
```// Get the BanItemAPI: the correct way:
final BanItem banItem = (BanItem) getServer().getPluginManager().getPlugin("BanItem");
final BanItemAPI banItemApi = banItem.getApi();
// or
// Get the BanItemAPI directly from the API instance:
final BanItemAPI banItemApi = BanItemAPI.getInstance();
// or
// Get the BanItemAPI from BanItem instance:
final BanItemAPI banItemApi = BanItem.getInstance().getApi();
```

### Requirements
* Java 1.8
* Any bukkit based server

### Links and Contacts
* [Spigot page](https://www.spigotmc.org/resources/banitem.67701/)
* [Bukkit page](https://dev.bukkit.org/projects/banitem-reloaded)
* [Documentation](http://banitem.andross.fr/)

For any bug/suggestions: `Andross#5254`

