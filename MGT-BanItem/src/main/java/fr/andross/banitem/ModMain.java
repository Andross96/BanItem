package fr.andross.banitem;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ModMain.MODID)
public class ModMain {
    public static final String MODID = "mgt_banitem";
    public static final Logger LOGGER = LoggerFactory.getLogger("MGT-BanItem");
    
    private static ModMain instance;
    private BanConfig banConfig;
    private BanDatabase banDatabase;
    private BanItemAPI api;
    private BanListener listener;
    private final BanUtils utils = new BanUtils(this);

    public ModMain(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;
        LOGGER.info("MGT-BanItem mod initializing");
        
        // Register lifecycle listeners
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("MGT-BanItem common setup");
        
        event.enqueueWork(() -> {
            // Initialize configuration
            java.nio.file.Path configDir = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
            banConfig = new BanConfig(this, configDir);
            
            // Initialize database (blacklist/whitelist)
            banDatabase = new BanDatabase(this, banConfig);
            
            // Initialize API
            api = new BanItemAPI(this);
            
            // Initialize listener
            listener = new BanListener(this);
            listener.load();
            
            LOGGER.info("MGT-BanItem initialization complete - {} blacklisted items, {} whitelisted items",
                    banDatabase.getBlacklist().getTotalBlacklistedItems(),
                    banDatabase.getWhitelist().getTotalWhitelistedItems());
        });
    }
    
    public static ModMain getInstance() {
        return instance;
    }
    
    public BanConfig getBanConfig() {
        return banConfig;
    }
    
    public BanDatabase getBanDatabase() {
        return banDatabase;
    }
    
    public BanUtils getUtils() {
        return utils;
    }
    
    public BanItemAPI getApi() {
        return api;
    }
    
    public BanListener getListener() {
        return listener;
    }
    
    /**
     * Reload configuration and database.
     */
    public void reloadConfig() {
        java.nio.file.Path configDir = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
        banConfig = new BanConfig(this, configDir);
        banDatabase = new BanDatabase(this, banConfig);
        
        if (listener != null) {
            listener.load();
        }
        
        LOGGER.info("Configuration reloaded - {} blacklisted items, {} whitelisted items",
                banDatabase.getBlacklist().getTotalBlacklistedItems(),
                banDatabase.getWhitelist().getTotalWhitelistedItems());
    }
}
