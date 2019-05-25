package fr.banitem;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class BanItem extends JavaPlugin {
    private final BannedItems banned = new BannedItems();

    @Override
    public void onEnable() {
        // Config:
        saveDefaultConfig();

        // Loading banned items:
        banned.clearAll();

        for(String material : getConfig().getKeys(false)){
            final Material m = Material.getMaterial(material.toUpperCase());
            if(m == null){
                getLogger().warning("Unknown material '" + m + "'");
                continue;
            }

            boolean blockPlace = getConfig().getBoolean(material + ".blockPlace");
            boolean blockBreak = getConfig().getBoolean(material + ".blockBreak");
            boolean pickUp = getConfig().getBoolean(material + ".pickUp");
            boolean drop = getConfig().getBoolean(material + ".drop");
            String notAllowedMessage = getConfig().getString(material + ".notAllowedMessage");
            final BannedItem bi = new BannedItem(blockPlace, blockBreak, pickUp, drop, notAllowedMessage);


            List<String> worlds = new ArrayList<String>();
            if(getConfig().isString(material + ".worlds")) worlds.add(getConfig().getString(material + ".worlds"));
            else worlds = getConfig().getStringList(material + ".worlds");

            if(worlds.isEmpty()) getServer().getWorlds().forEach(w -> banned.addBannedItem(w.getName(), m, bi));
            else{
                for(String world : worlds){
                    World w = getServer().getWorld(world);
                    if(w == null) {
                        getLogger().warning("Unknown world '" + world + "' for item '" + material + "'.");
                        continue;
                    }
                    banned.addBannedItem(w.getName(), m, bi);
                }
            }
        }

        // Registering listener
        getServer().getPluginManager().registerEvents(new BanItemListener(banned), this);

        getLogger().info("BanItem: Enabled");
    }

}
