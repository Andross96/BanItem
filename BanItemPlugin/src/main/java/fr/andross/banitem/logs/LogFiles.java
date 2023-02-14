package fr.andross.banitem.logs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.andross.banitem.BanItem;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public abstract class LogFiles {

    private final BanItem pl;
    private final String fileName;
    private final File logFile;
    JSONObject jsonLog;

    public LogFiles(String fileName, BanItem pl) {
        this.pl = pl;
        this.fileName = fileName;
        this.logFile = createLogFile();
        this.jsonLog = parseFile(logFile);
    }

    File createLogFile() {
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

    JSONObject parseFile(File file) {
        JSONParser parser = new JSONParser();
        FileReader reader = null;
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

    void saveLog() {
        try {
            FileWriter writer = new FileWriter(logFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(jsonLog));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void addLogData(OfflinePlayer p, ItemStack item, Object... otherData);

    JSONObject getPlayerLog(UUID id) {
        return ((JSONObject) jsonLog.getOrDefault(id.toString(), new JSONObject()));
    }
}
