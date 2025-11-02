# MGT-BanItem - Command Reference

Complete guide to all administrative commands available in MGT-BanItem.

## Table of Contents
- [Permission Requirements](#permission-requirements)
- [Command Aliases](#command-aliases)
- [Commands](#commands)
  - [Help](#help)
  - [Info](#info)
  - [Reload](#reload)
  - [Add](#add)
  - [Remove](#remove)
  - [Check](#check)
  - [Log](#log)
  - [MetaItem](#metaitem)
- [Advanced Usage](#advanced-usage)

## Permission Requirements

All administrative commands require **operator level 2** or higher. This means:
- Players must be OP level 2+ to use admin commands
- Console has full access to all commands

## Command Aliases

- `/bi` - Short alias for `/banitem`
- `/bi mi` - Short alias for `/banitem metaitem`

## Commands

### Help

**Usage:** `/banitem help` or `/bi help`

**Description:** Displays a list of all available commands with brief descriptions.

**Example:**
```
/banitem help
```

---

### Info

**Usage:** `/banitem info` or `/bi info`

**Permission Required:** Operator Level 2

**Description:** Shows detailed information about the mod including:
- Number of blacklisted items
- Number of whitelisted items
- Number of active event listeners
- Mod version

**Example:**
```
/banitem info
```

---

### Reload

**Usage:** `/banitem reload` or `/bi reload`

**Permission Required:** Operator Level 2

**Description:** Reloads the configuration file and database from disk without restarting the server. Useful after manually editing config files.

**Example:**
```
/banitem reload
```

---

### Add

**Usage:** `/banitem add <actions> [-m materials] [-w worlds] [message]`

**Short:** `/bi add <actions> [-m materials] [-w worlds] [message]`

**Permission Required:** Operator Level 2

**Description:** Adds items to the blacklist with specified actions. When players try to perform these actions with the banned items, they will be prevented and optionally receive a message.

**Parameters:**
- `<actions>` (required) - Comma-separated list of actions to ban, or `*` for all actions
  - Valid actions: `break`, `click`, `consume`, `craft`, `drop`, `drops`, `hold`, `interact`, `inventoryclick`, `pickup`, `place`, `transfer`, `use`, `wear`, `attack`, `delete`
- `-m <materials>` (optional) - Comma-separated list of materials, or `*` for all materials
  - If not specified, uses the item in your hand (players only)
  - Examples: `minecraft:diamond_sword`, `minecraft:tnt`, `*`
- `-w <worlds>` (optional) - Comma-separated list of worlds, or `*` for all worlds
  - If not specified, uses your current world (players only)
  - Examples: `minecraft:overworld`, `minecraft:nether`, `minecraft:the_end`, `*`
- `[message]` (optional) - Custom message to send to players when they try to use the banned item
  - Supports color codes with `&` (e.g., `&c` for red, `&a` for green)

**Examples:**

Ban placing and breaking diamond swords in the overworld:
```
/bi add place,break -m minecraft:diamond_sword -w minecraft:overworld
```

Ban TNT everywhere with a custom message:
```
/bi add * -m minecraft:tnt -w * &cTNT is completely banned!
```

Ban the item in your hand for all "use" actions in the current world (player only):
```
/bi add use &eYou cannot use this item!
```

Ban multiple items at once:
```
/bi add place,break -m minecraft:diamond_block,minecraft:netherite_block -w minecraft:overworld
```

**Notes:**
- Console must specify both `-m` and `-w` parameters
- Players can omit parameters to use item in hand and current world
- Wildcards (`*`) work for actions, materials, and worlds

---

### Remove

**Usage:** `/banitem remove [-m materials] [-w worlds]`

**Short:** `/bi remove [-m materials] [-w worlds]`

**Permission Required:** Operator Level 2

**Description:** Removes items from the blacklist, unbanning them in the specified worlds.

**Parameters:**
- `-m <materials>` (optional) - Comma-separated list of materials to unban
  - If not specified, uses the item in your hand (players only)
- `-w <worlds>` (optional) - Comma-separated list of worlds to unban in
  - If not specified, uses your current world (players only)

**Examples:**

Unban diamond swords in the overworld:
```
/bi remove -m minecraft:diamond_sword -w minecraft:overworld
```

Unban TNT everywhere:
```
/bi remove -m minecraft:tnt -w *
```

Unban the item in your hand in the current world (player only):
```
/bi remove
```

**Notes:**
- Console must specify both `-m` and `-w` parameters
- Removing an item from blacklist removes ALL actions for that item in the specified worlds

---

### Check

**Usage:** 
- `/banitem check` - Check for banned items
- `/banitem check delete` - Check and remove banned items

**Short:** `/bi check [delete]`

**Permission Required:** Operator Level 2

**Description:** Scans all online players' inventories for banned items. Optionally removes them automatically.

**Parameters:**
- `delete` (optional) - If specified, automatically removes all found banned items

**Examples:**

Check which players have banned items:
```
/bi check
```

Check and automatically remove all banned items from players:
```
/bi check delete
```

**Output:**
- Lists all players who have banned items in their inventory
- Shows the total number of players affected
- If `delete` is used, confirms items were removed

**Notes:**
- Only checks items currently in player inventories
- Does not check ender chests or other storage
- Useful for cleaning up inventories after adding new bans

---

### Log

**Usage:** `/banitem log` or `/bi log`

**Permission Required:** Operator Level 2

**Player Only:** Yes (cannot be used from console)

**Description:** Toggles debug logging for the executing player. When enabled, detailed information about ban checks and actions will be logged to the console.

**Examples:**

Toggle debug logging:
```
/bi log
```

**Output:**
- Shows whether logging is now ON or OFF
- When ON, detailed ban check information is logged to server console

**Notes:**
- Useful for debugging why items are or aren't being banned
- Each player has their own logging state
- Logging persists until toggled off or server restart

---

### MetaItem

**Usage:** `/banitem metaitem <subcommand>` or `/bi mi <subcommand>`

**Permission Required:** Operator Level 2

**Description:** Manages "meta items" - saved items with complete NBT/component data. Useful for banning specific enchanted items, named items, or items with custom data.

#### Subcommands

##### mi add

**Usage:** `/bi mi add <name> [actions] [message]`

**Player Only:** Yes

**Description:** Saves the item currently in your hand as a meta item. Optionally bans it immediately with specified actions.

**Parameters:**
- `<name>` (required) - Unique name for this meta item
- `[actions]` (optional) - Comma-separated list of actions to ban
- `[message]` (optional) - Custom ban message

**Examples:**

Save item in hand as "custom_sword":
```
/bi mi add custom_sword
```

Save and ban item in hand with actions:
```
/bi mi add op_pickaxe place,break &cThis pickaxe is too powerful!
```

**Notes:**
- Item must be in your main hand
- Saves complete item data including enchantments, name, lore, etc.
- If actions are specified, item is added to blacklist in current world

##### mi get

**Usage:** `/bi mi get <name>`

**Player Only:** Yes

**Description:** Gives you a copy of a saved meta item.

**Parameters:**
- `<name>` (required) - Name of the meta item to retrieve

**Examples:**

Get a copy of saved item:
```
/bi mi get custom_sword
```

**Notes:**
- Requires empty inventory slot
- Gives exact copy with all NBT data

##### mi remove

**Usage:** `/bi mi remove <name>`

**Description:** Deletes a saved meta item and unbans it everywhere.

**Parameters:**
- `<name>` (required) - Name of the meta item to delete

**Examples:**

Delete saved meta item:
```
/bi mi remove custom_sword
```

**Notes:**
- Removes from database and unloads from memory
- Also removes from blacklist if it was banned

##### mi list

**Usage:** `/bi mi list`

**Description:** Lists all saved meta item names.

**Examples:**

Show all meta items:
```
/bi mi list
```

---

## Advanced Usage

### Using Wildcards

Wildcards (`*`) allow you to apply bans broadly:

**All actions:**
```
/bi add * -m minecraft:tnt -w minecraft:overworld
```

**All materials:**
```
/bi add place -m * -w minecraft:overworld
```

**All worlds:**
```
/bi add break -m minecraft:diamond_ore -w *
```

**Everything everywhere:**
```
/bi add * -m * -w *
```

### Color Codes in Messages

Use `&` followed by a color/format code:

- `&0` - Black
- `&1` - Dark Blue
- `&2` - Dark Green
- `&3` - Dark Aqua
- `&4` - Dark Red
- `&5` - Dark Purple
- `&6` - Gold
- `&7` - Gray
- `&8` - Dark Gray
- `&9` - Blue
- `&a` - Green
- `&b` - Aqua
- `&c` - Red
- `&d` - Light Purple
- `&e` - Yellow
- `&f` - White
- `&l` - Bold
- `&m` - Strikethrough
- `&n` - Underline
- `&o` - Italic
- `&r` - Reset

**Example:**
```
/bi add use -m minecraft:diamond_sword &c&lWARNING: &r&7This sword is banned!
```

### World Names

You can use common names or full resource locations:

**Common names:**
- `overworld` or `world` → `minecraft:overworld`
- `nether` or `the_nether` → `minecraft:nether`
- `end` or `the_end` → `minecraft:the_end`

**Full resource location:**
- `minecraft:overworld`
- `modname:custom_dimension`

### Material Names

Always use the full Minecraft resource location:
- `minecraft:diamond_sword`
- `minecraft:stone`
- `modname:custom_item`

### Batch Operations

You can ban multiple items/worlds at once by using comma-separated lists:

```
/bi add place,break -m minecraft:diamond_block,minecraft:netherite_block,minecraft:bedrock -w minecraft:overworld,minecraft:nether
```

### Meta Items for Custom Items

Meta items are essential for banning items with specific properties:

1. Get/create the specific item you want to ban
2. Hold it in your hand
3. Save it as a meta item: `/bi mi add my_item`
4. Ban it with actions: `/bi mi add my_item place,use &cThis specific item is banned!`

This ensures you're banning the EXACT item with all its NBT data, not just any item of that type.

---

## Troubleshooting

**Problem:** Command says "Mod not initialized"
- **Solution:** The mod may not have loaded properly. Check server logs and try `/bi reload`

**Problem:** "You must specify materials with -m"
- **Solution:** You're running from console. Console requires all parameters to be explicitly specified.

**Problem:** Banned items aren't being blocked
- **Solution:** 
  - Check if you have bypass permissions (OPs may bypass bans)
  - Use `/bi log` to enable debug logging
  - Use `/bi check` to verify the item is actually banned
  - Try `/bi reload` to reload the configuration

**Problem:** "Invalid materials" error
- **Solution:** Make sure you're using the full resource location (e.g., `minecraft:diamond_sword` not just `diamond_sword`)

---

## See Also

- [README.md](README.md) - Main mod documentation
- [TODO.md](TODO.md) - Future features and implementation status
- [CONVERSION_SUMMARY.md](CONVERSION_SUMMARY.md) - Details on plugin-to-mod conversion
