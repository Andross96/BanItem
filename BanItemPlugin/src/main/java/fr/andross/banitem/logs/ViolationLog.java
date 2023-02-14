package fr.andross.banitem.logs;

import fr.andross.banitem.BanItem;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@SuppressWarnings("unchecked")
public final class ViolationLog extends LogFiles {
    private static final String fileName = "log.json";

    public ViolationLog(BanItem pl) {
        super(fileName, pl);
    }

    @Override
    public void addLogData(OfflinePlayer p, ItemStack item, Object... otherData) {
        final UUID id = p.getUniqueId();
        JSONObject logData = createPlayerLog(item, ((String) otherData[0]));
        JSONObject playerData = getPlayerLog(id);
        playerData.put("playerName", p.getName());
        JSONArray logs = (JSONArray) playerData.getOrDefault("logs", new JSONArray());
        logs.add(logData);
        playerData.put("logs", logs);
        jsonLog.put(id.toString(), playerData);
        saveLog();
    }

    private JSONObject createPlayerLog(ItemStack item, String reason) {
        boolean isNull = (item == null);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        JSONObject log = new JSONObject();
        log.put("timeFormatted", dtf.format(now));
        log.put("timeStamp", Instant.now().getEpochSecond());
        log.put("reason", reason);
        log.put("itemMaterial", (isNull) ? null : item.getType().name());
        return log;
    }
}
