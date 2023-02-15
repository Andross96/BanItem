# BanItem ![GPL-3.0](http://cdn.andross.fr/badges/license.svg) ![Stable](http://cdn.andross.fr/badges/stable.svg) ![Version](https://badgen.net/badge/version/3.4/blue) ![Discord](http://cdn.andross.fr/badges/discord.svg)

<<<<<<< HEAD
# BaNBT
This is a simple extension plugin for the [ItemBan plugin](https://www.spigotmc.org/resources/banitem-1-7-1-19.67701/) to make banning custom items more convenient.  
This addon is dependent on these plugins and will not run without them!
- [NBTAPI](https://www.spigotmc.org/resources/nbt-api.7939/)
- [ItemBan plugin](https://www.spigotmc.org/resources/banitem-1-7-1-19.67701/)

## Tasks
- [ ] Add and ban custom items via command
- [ ] UI for simplifying the task of banning custom items
- [x] Log players who violated the ItemBan plugin
- [x] Allow the option for banning player if they pickup a specific item

## Info
The [ItemBan plugin](https://www.spigotmc.org/resources/banitem-1-7-1-19.67701/) uses the following formatting to achieve its goals:
```
blacklist:
  world:
    stone_sword:
      attack: 'You cannot attack using this weapon!'
     
    customItem:
      '*': This item is banned!
```
=======
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
  <url>https://repo.andross.fr/</url>
</repository>
>>>>>>> parent of 7919543 (Update README.md)

<dependency>
  <groupId>fr.andross.banitem</groupId>
  <artifactId>BanItemPlugin</artifactId>
  <version>3.4</version>
  <scope>provided</scope>
</dependency>
```

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
* [Documentation](https://banitem.andross.fr/)

For any questions/suggestions: PM me on spigot or on discord (Andross#5254).

