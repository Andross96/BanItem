package fr.andross.Utils;

import fr.andross.Plugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class BanMap {
    private final Plugin pl;
    private final Map<String, Map<Material, BanOptions>> blacklist = new HashMap<>();
    private final Map<String, Set<Material>> whitelist = new HashMap<>();
    private final Map<String, String> whitelist_message = new HashMap<>();

    public BanMap (final Plugin pl) {
        this.pl = pl;
    }

    public Map<String, Map<Material, BanOptions>> getBlacklist(){ return blacklist; }
    public Map<String, Set<Material>> getWhitelist(){ return whitelist; }
    public Map<String, String> getWhitelistMessage(){ return whitelist_message; }

    public Set<BanOption> load(){
        final Set<BanOption> finalOptions = new HashSet<>();

        // Loading blacklist
        blacklist.clear();
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
                    final Set<BanOption> options = new HashSet<>();
                    final String configOptions = materialsCs.getString(materialKey + ".options");
                    if (configOptions == null || configOptions.isEmpty()) {
                        pl.getLogger().info("Empty options for '" + materialKey + "' set for world '" + worldKey + "' in blacklist from config.yml: ban ignored.");
                        continue;
                    }
                    if (configOptions.equals("*")) Collections.addAll(options, BanOption.values());
                    else
                        for (String option : configOptions.trim().replaceAll("\\s+", "").split(",")) {
                            try {
                                options.add(BanOption.valueOf(option.toUpperCase()));
                            } catch (Exception e) {
                                pl.getLogger().info("Unknown option '" + option + "' for '" + materialKey + "' set for world '" + worldKey + "' in blacklist from config.yml: ban ignored.");
                            }
                        }
                    if (options.isEmpty()) {
                        pl.getLogger().info("Empty options for '" + materialKey + "' set for world '" + worldKey + "' in blacklist from config.yml: ban ignored.");
                        continue;
                    }

                    // Getting message
                    final String message = materialsCs.getString(materialKey + ".message");

                    // Adding into the map
                    finalOptions.addAll(options);
                    final BanOptions bo = new BanOptions(options, message);
                    for (World w : worlds) {
                        Map<Material, BanOptions> newmap = blacklist.get(w.getName());
                        if (newmap == null) newmap = new HashMap<>();
                        newmap.put(m, bo);
                        blacklist.put(w.getName(), newmap);
                    }
                }
            }
        }


        // Loading whitelist
        whitelist.clear();
        whitelist_message.clear();
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
                for (World w : worlds) whitelist.put(w.getName(), materialSet);
            }
        }
        return finalOptions;
    }
}
