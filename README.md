# BanItem ![GPL-3.0](http://cdn.andross.fr/badges/license.svg) ![Stable](http://cdn.andross.fr/badges/stable.svg) ![Version](https://badgen.net/badge/version/3.4/blue) ![Discord](http://cdn.andross.fr/badges/discord.svg)

# BaNBT
This is a simple extension plugin for the [ItemBan plugin](https://www.spigotmc.org/resources/banitem-1-7-1-19.67701/) to make banning custom items more convenient.  
This addon is dependent on these plugins and will not run without them!
- [NBTAPI](https://www.spigotmc.org/resources/nbt-api.7939/)
- [ItemBan plugin](https://www.spigotmc.org/resources/banitem-1-7-1-19.67701/)

## Tasks
- [ ] Add and ban custom items via command
- [ ] UI for simplifying the task of banning custom items
- [ ] Log players who violated the ItemBan plugin
- [ ] Allow the option for banning player if they pickup a specific item

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

The configuration for custom items is defined in the `customitems.yml` file:
```
customItem:
  material: stick
  nbtapi:
    'tier': 4
```

Here we can see the [NBTAPI](https://www.spigotmc.org/resources/nbt-api.7939/) can be used to specify destinct items making it suitable for the use with custom or modded items.
