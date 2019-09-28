package fr.andross.banitem.Maps;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Utils.BannedItem;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CustomItems extends HashMap<String, BannedItem> {
    private final Map<BannedItem, String> reversed = new HashMap<>();
    private final File itemsFile;
    private FileConfiguration itemsConfig;

    public CustomItems(final BanItem pl, final CommandSender sender) {
        this.itemsFile = new File(pl.getDataFolder(), "items.yml");

        // Checking/Creating file
        try {
            // Trying to save the custom one, else creating a new one
            if (!itemsFile.isFile()) pl.saveResource("items.yml", false);
            if (!itemsFile.isFile()) if (!itemsFile.createNewFile()) throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(pl.color("&c[&e&lBanItem&c] &cUnable to use custom items for this session."));
            return;
        }

        // Loading custom items
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        for (String key : itemsConfig.getKeys(false)) {
            try {
                final ItemStack itemStack = (ItemStack) itemsConfig.get(key);
                if (itemStack == null) continue;
                put(key, new BannedItem(itemStack));
            } catch (Exception e) {
                sender.sendMessage(pl.color("&c[&e&lBanItem&c] &cInvalid custom item &e" + key + "&c in items.yml."));
            }
        }
    }

    @Override
    public BannedItem put(final String key, final BannedItem value) {
        reversed.put(value, key);
        return super.put(key, value);
    }

    @Override
    public void clear(){
        reversed.clear();
        super.clear();
    }

    @Override
    public BannedItem remove(final Object key) {
        reversed.remove(get(key));
        return super.remove(key);
    }

    @Nullable
    public String getName(final BannedItem value){
        if (value == null) return null;
        return reversed.get(value);
    }

    // File operations:
    public FileConfiguration getItemsConfig() { return itemsConfig; }
    public File getItemsFile() { return itemsFile; }
}