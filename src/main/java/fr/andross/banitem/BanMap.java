package fr.andross.banitem;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

class BanMap {
    private final Plugin pl;
    private final Map<String, Map<Material, Map<BanOption, String>>> blacklist = new HashMap<>();
    private final Map<String, Set<Material>> whitelist = new HashMap<>();
    private final Map<String, String> whitelist_message = new HashMap<>();
    private final Map<UUID, Long> pickup_cooldown = new HashMap<>();

    BanMap (final Plugin pl) {
        this.pl = pl;
    }

    Map<String, Map<Material, Map<BanOption, String>>> getBlacklist(){ return blacklist; }
    Map<String, Set<Material>> getWhitelist(){ return whitelist; }
    Map<String, String> getWhitelistMessage(){ return whitelist_message; }
    Map<UUID, Long> getPickupCooldown(){ return pickup_cooldown; }

    Set<BanOption> load(){
        final Set<BanOption> finalOptions = new HashSet<>();

        // Clearing maps
        blacklist.clear();
        whitelist.clear();
        whitelist_message.clear();

        // Loading blacklist
        ConfigurationSection worldsCs = pl.getConfig().getConfigurationSection("blacklist");
        if(worldsCs != null) {
            for (final String worldKey : worldsCs.getKeys(false)) { // Looping through worlds
                // Checking the world
                final Set<World> worlds = new HashSet<>();
                if (worldKey.equals("*")) worlds.addAll(pl.getServer().getWorlds());
                else {
                    for (String w : worldKey.trim().replaceAll("\\s+", "").split(",")) {
                        final World world = pl.getServer().getWorld(w);
                        if (world == null) {
                            pl.getLogger().info("Unknown world '" + w + "' set in blacklist from config.yml");
                            continue;
                        }
                        worlds.add(world);
                    }
                }
                if (worlds.isEmpty()) continue;

                // Checking the materials
                final ConfigurationSection materialsCs = worldsCs.getConfigurationSection(worldKey);
                if (materialsCs == null) continue;
                for (final String materialKey : materialsCs.getKeys(false)) {
                    // Getting material
                    final Material m = Material.matchMaterial(materialKey);
                    if (m == null) {
                        pl.getLogger().info("Unknown material '" + materialKey + "' set for world '" + worldKey + "' in blacklist from config.yml");
                        continue;
                    }

                    // Getting options
                    final ConfigurationSection optionsCs = materialsCs.getConfigurationSection(materialKey);
                    if(optionsCs == null) continue;

                    final Map<BanOption, String> options = new HashMap<>();
                    for (String optionKey : optionsCs.getKeys(false)) {
                        final String message = optionsCs.getString(optionKey);
                        if (optionKey.equals("*")) for(BanOption o : BanOption.values()) options.put(o, message);
                        else {
                            for (String option : optionKey.trim().replaceAll("\\s+", "").split(",")) {
                                try {
                                    final BanOption bo = BanOption.valueOf(option.toUpperCase());
                                    options.put(bo, message);
                                } catch (Exception e) {
                                    pl.getLogger().info("Unknown option '" + option + "' set for '" + materialKey + "' in world '" + worldKey + "' in blacklist from config.yml");
                                }
                            }
                        }
                    }

                    // Adding into the map
                    finalOptions.addAll(options.keySet());
                    for (World w : worlds) {
                        Map<Material, Map<BanOption, String>> newmap = blacklist.get(w.getName().toLowerCase());
                        if(newmap == null) newmap = new HashMap<>();
                        newmap.put(m, options);
                        blacklist.put(w.getName().toLowerCase(), newmap);
                    }
                }
            }
        }


        // Loading whitelist
        worldsCs = pl.getConfig().getConfigurationSection("whitelist");
        if(worldsCs != null) {
            for (final String worldKey : worldsCs.getKeys(false)) { // Looping through worlds
                // Checking the world
                final Set<World> worlds = new HashSet<>();
                if (worldKey.equals("*")) worlds.addAll(pl.getServer().getWorlds());
                else {
                    for (String w : worldKey.trim().replaceAll("\\s+", "").split(",")) {
                        final World world = pl.getServer().getWorld(w);
                        if (world == null) {
                            pl.getLogger().info("Unknown world '" + w + "' set in whitelist from config.yml");
                            continue;
                        }
                        worlds.add(world);

                        // Adding message
                        whitelist_message.put(world.getName(), worldsCs.getString(worldKey + ".message"));
                    }
                }
                if (worlds.isEmpty()) continue;

                // Checking the materials
                final List<String> materials = worldsCs.getStringList(worldKey + ".blocks");
                if(materials.isEmpty()) continue;
                final Set<Material> materialSet = new HashSet<>();
                for (final String material : materials) {
                    // Getting material
                    final Material m = Material.matchMaterial(material);
                    if (m == null) {
                        pl.getLogger().info("Unknown material '" + material + "' set for world '" + worldKey + "' in whitelist from config.yml");
                        continue;
                    }
                    materialSet.add(m);
                }

                // Adding into the map
                for (World w : worlds) whitelist.put(w.getName().toLowerCase(), materialSet);
            }
        }
        return finalOptions;
    }
}
