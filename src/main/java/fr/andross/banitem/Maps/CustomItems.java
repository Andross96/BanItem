package fr.andross.banitem.Maps;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.Utils.BannedItem;
import fr.andross.banitem.Utils.Chat;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CustomItems extends HashMap<String, BannedItem> {
    private final Map<BannedItem, String> reversed = new HashMap<>();
    private final File itemsFile;
    private FileConfiguration itemsConfig;

    public CustomItems(@NotNull final CommandSender sender) {
        final BanItem pl = BanItem.getInstance();
        this.itemsFile = new File(pl.getDataFolder(), "items.yml");

        // Checking/Creating file
        try {
            // Trying to save the custom one, else creating a new one
            if (!itemsFile.isFile()) pl.saveResource("items.yml", false);
            if (!itemsFile.isFile()) if (!itemsFile.createNewFile()) throw new Exception();
        } catch (final Exception e) {
            e.printStackTrace();
            sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &cUnable to use custom items for this session."));
            return;
        }

        // Loading custom items
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        for (final String key : itemsConfig.getKeys(false)) {
            try {
                final ItemStack itemStack = itemsConfig.getItemStack(key);
                if (itemStack == null) throw new Exception();
                put(key, new BannedItem(itemStack));
            } catch (final Exception e) {
                sender.sendMessage(Chat.color("&c[&e&lBanItem&c] &cInvalid custom item &e" + key + "&c in items.yml."));
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