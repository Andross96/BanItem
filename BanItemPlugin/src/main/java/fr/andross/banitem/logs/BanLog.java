package fr.andross.banitem.logs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.andross.banitem.BanItem;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class BanLog {

    private static final String fileName = "bans.json";

    private final BanItem pl;
    private File banFile;
    private JSONObject jsonLog;

    public BanLog(BanItem pl) {
        this.pl = pl;
        this.banFile = createLogFile();
        this.jsonLog = parseFile(banFile);
    }

    private File createLogFile() {
        File file = new File(pl.getDataFolder().getAbsolutePath() + File.separator + fileName);
        if (file.exists()) return file;

        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write("{}");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            String errMsg = "§cCould not create log file at \"" + pl.getDataFolder().getAbsolutePath() + File.pathSeparator + fileName + "\"!";
            pl.getUtils().sendMessage(pl.getServer().getConsoleSender(), errMsg);
        }

        return file;
    }

    private JSONObject parseFile(File file) {
        JSONParser parser = new JSONParser();
        FileReader reader;
        try {
            reader = new FileReader(file);
            return (JSONObject) parser.parse(reader);

        } catch (ParseException | IOException e) {
            e.printStackTrace();
            String errMsg = "§cCould not parse log file!\nPlease make sure that the §7" + fileName + "§c file is valid JSON!";
            pl.getUtils().sendMessage(pl.getServer().getConsoleSender(), errMsg);
        }

        return null;
    }

    public void addBan(OfflinePlayer p, ItemStack item) {
        final UUID id = p.getUniqueId();
        JSONObject logData = createPlayerBan(item);
        JSONObject playerData = getPlayerBans(id);
        playerData.put("playerName", p.getName());
        JSONArray logs = (JSONArray) playerData.getOrDefault("logs", new JSONArray());
        logs.add(logData);
        jsonLog.put(id.toString(), playerData);
        saveLog();
    }

    private void saveLog() {
        try {
            FileWriter writer = new FileWriter(banFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(jsonLog));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        this.banFile = createLogFile();
        this.jsonLog = parseFile(banFile);
    }

    private JSONObject createPlayerBan(ItemStack item) {
        boolean isNull = (item == null);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        JSONObject log = new JSONObject();
        log.put("timeFormatted", dtf.format(now));
        log.put("timeStamp", Instant.now().getEpochSecond());
        log.put("itemMaterial", (isNull) ? null : item.getType().name());
        return log;
    }

    private JSONObject getPlayerBans(UUID id) {
        return ((JSONObject) jsonLog.getOrDefault(id.toString(), new JSONObject()));
    }

    public static String getFileName() {
        return fileName;
    }
}
