# BanItem
BanItem is a simple lightweight & powerful per world ban item, fully configurable.

# Links
* [Spigot thread](https://www.spigotmc.org/resources/banitem.67701/) / [Bukkit thread](https://dev.bukkit.org/projects/banitem-reloaded)
* [For any bugs](https://github.com/Andross96/BanItem/issues)

# Developer API
```xml
  <repository>
    <id>andross-repo</id>
    <url>http://repo.andross.fr/</url>
  </repository>

  <dependency>
    <groupId>fr.andross</groupId>
    <artifactId>BanItemParent</artifactId>
    <version>2.3</version>
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
