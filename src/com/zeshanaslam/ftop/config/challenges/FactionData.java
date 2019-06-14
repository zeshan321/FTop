package com.zeshanaslam.ftop.config.challenges;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.zeshanaslam.ftop.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.UUID;

public class FactionData {

    public String factionId;
    public HashMap<String, Double> dailyObjectiveValues = new HashMap<>();
    public HashMap<String, Double> weeklyObjectiveValues = new HashMap<>();
    public boolean dailyCompleted;
    public boolean weeklyCompleted;

    public FactionData(String factionId) {
        this.factionId = factionId;
        this.dailyCompleted = false;
        this.weeklyCompleted = false;
    }

    public void addValue(String key, boolean weekly, double value) {
        if (weekly) {
            if (weeklyObjectiveValues.containsKey(key)) {
                weeklyObjectiveValues.put(key, weeklyObjectiveValues.get(key) + value);
            } else {
                weeklyObjectiveValues.put(key, value);
            }
        } else {
            if (dailyObjectiveValues.containsKey(key)) {
                dailyObjectiveValues.put(key, dailyObjectiveValues.get(key) + value);
            } else {
                dailyObjectiveValues.put(key, value);
            }
        }
    }

    public void setDailyStatus(boolean status) {
        dailyCompleted = status;
    }

    public void setWeeklyStatus(boolean status) {
        weeklyCompleted = status;
    }

    public double getDailyValue(String key) {
        if (key.equals("MAKE")) {
            return getFactionBal();
        }

        if (dailyObjectiveValues.containsKey(key)) {
            return dailyObjectiveValues.get(key);
        }

        return 0;
    }

    public double getWeeklyValue(String key) {
        if (key.equals("MAKE")) {
            return getFactionBal();
        }

        if (weeklyObjectiveValues.containsKey(key)) {
            return weeklyObjectiveValues.get(key);
        }

        return 0;
    }

    private double getFactionBal() {
        double bal = 0;

        Faction faction = Factions.getInstance().getFactionById(factionId);
        for (FPlayer player: faction.getFPlayers()){
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(player.getAccountId()));

            bal = bal + Main.economy.getBalance(offlinePlayer);
        }

        return bal;
    }
}
