package com.zeshanaslam.ftop.config.challenges;

import com.google.gson.Gson;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.zeshanaslam.ftop.Main;
import com.zeshanaslam.ftop.utils.FileHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ChallengesStore {

    private final Main main;
    private final Gson gson;
    public List<String> dailyRewards;
    public int dailyMin;
    public int dailyMax;
    public HashMap<String, Objective> dailyObjectives;
    public List<String> weeklyRewards;
    public int weeklyMin;
    public int weeklyMax;
    public HashMap<String, Objective> weeklyObjectives;
    public HashMap<String, FactionData> factionData;
    public long dailyStart;
    public long weeklyStart;
    public SectionObjectives weekly;
    public SectionObjectives daily;
    public List<String> text;
    public List<String> completed;

    public ChallengesStore(Main main) {
        this.main = main;
        this.gson = new Gson();

        if (!FileHandler.fileExists("plugins/FTop/challenges.yml")) {
            main.saveResource("challenges.yml", false);
        }

        FileHandler fileHandler = new FileHandler("plugins/FTop/challenges.yml");

        text = new ArrayList<>();
        for (String tempText : fileHandler.getStringList("Text")) {
            text.add(ChatColor.translateAlternateColorCodes('&', tempText));
        }

        completed = new ArrayList<>();
        for (String tempText : fileHandler.getStringList("Completed")) {
            completed.add(ChatColor.translateAlternateColorCodes('&', tempText));
        }

        dailyRewards = new ArrayList<>();
        dailyRewards = fileHandler.getStringList("Daily.Data.Rewards");
        dailyMin = fileHandler.getInteger("Daily.Data.Amount.Min");
        dailyMax = fileHandler.getInteger("Daily.Data.Amount.Max");
        dailyObjectives = new HashMap<>();
        for (String key : fileHandler.getConfigurationSection("Daily.Objectives").getKeys(false)) {
            int amountMin;
            int amountMax;
            String amount = fileHandler.getString("Daily.Objectives." + key + ".Amount");
            if (amount.contains("-")) {
                String[] amountData = amount.split("-");
                amountMin = Integer.parseInt(amountData[0]);
                amountMax = Integer.parseInt(amountData[1]);
            } else {
                amountMin = Integer.parseInt(amount);
                amountMax = amountMin;
            }

            String text = fileHandler.getString("Daily.Objectives." + key + ".Text");
            dailyObjectives.put(key.toUpperCase(), new Objective(key.toUpperCase(), amountMin, amountMax, text));
        }

        weeklyRewards = new ArrayList<>();
        weeklyRewards = fileHandler.getStringList("Weekly.Data.Rewards");
        weeklyMin = fileHandler.getInteger("Weekly.Data.Amount.Min");
        weeklyMax = fileHandler.getInteger("Weekly.Data.Amount.Max");
        weeklyObjectives = new HashMap<>();
        for (String key : fileHandler.getConfigurationSection("Weekly.Objectives").getKeys(false)) {
            int amountMin;
            int amountMax;
            String amount = fileHandler.getString("Weekly.Objectives." + key + ".Amount");
            if (amount.contains("-")) {
                String[] amountData = amount.split("-");
                amountMin = Integer.parseInt(amountData[0]);
                amountMax = Integer.parseInt(amountData[1]);
            } else {
                amountMin = Integer.parseInt(amount);
                amountMax = amountMin;
            }

            String text = fileHandler.getString("Weekly.Objectives." + key + ".Text");
            weeklyObjectives.put(key.toUpperCase(), new Objective(key.toUpperCase(), amountMin, amountMax, text));
        }

        FileHandler data = new FileHandler("plugins/FTop/cdata.yml");
        if (!data.contains("Start")) {
            data.set("Start.Daily", String.valueOf(System.currentTimeMillis()));
            data.set("Start.DailyData", gson.toJson(generateObjectives(dailyObjectives, dailyMin, dailyMax)));
            data.set("Start.Weekly", String.valueOf(System.currentTimeMillis()));
            data.set("Start.WeeklyData", gson.toJson(generateObjectives(weeklyObjectives, weeklyMin, weeklyMax)));
            data.save();
        }

        dailyStart = Long.valueOf(data.getString("Start.Daily"));
        daily = gson.fromJson(data.getString("Start.DailyData"), SectionObjectives.class);
        weeklyStart = Long.valueOf(data.getString("Start.Weekly"));
        weekly = gson.fromJson(data.getString("Start.WeeklyData"), SectionObjectives.class);

        factionData = new HashMap<>();
        if (data.contains("FactionData")) {
            for (String json : data.getStringList("FactionData")) {
                FactionData factionDataObject = gson.fromJson(json, FactionData.class);
                factionData.put(factionDataObject.factionId, factionDataObject);
            }
        }
    }

    public void save() {
        FileHandler data = new FileHandler("plugins/FTop/cdata.yml");
        data.set("Start.Daily", String.valueOf(dailyStart));
        data.set("Start.DailyData", gson.toJson(daily));
        data.set("Start.Weekly", String.valueOf(weeklyStart));
        data.set("Start.WeeklyData", gson.toJson(weekly));

        List<String> json = new ArrayList<>();
        for (FactionData factionDataObject : factionData.values()) {
            json.add(gson.toJson(factionDataObject));
        }

        data.set("FactionData", json);
        data.save();
    }

    public SectionObjectives generateObjectives(HashMap<String, Objective> objectives, int min, int max) {
        int amount = main.fTopUtils.getRandomNumberInRange(min, max);
        List<Objective> objectiveData = new ArrayList<>(objectives.values());

        SectionObjectives sectionObjectives = new SectionObjectives();

        Random random = new Random();
        while (amount > 0) {
            Objective objective = objectiveData.get(random.nextInt(objectiveData.size()));
            objective.amount = main.fTopUtils.getRandomNumberInRange(objective.amountMin, objective.amountMax);

            if(!sectionObjectives.objectives.containsKey(objective.type)) {
                amount--;
                sectionObjectives.objectives.put(objective.type, objective);
            }
        }

        return sectionObjectives;
    }

    public FactionData getFactionData(Player player) {
        FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);

        if (fplayer != null && fplayer.hasFaction()) {
            Faction faction = fplayer.getFaction();

            if (factionData.containsKey(faction.getId())) {
                return factionData.get(faction.getId());
            } else {
                FactionData tempData = new FactionData(faction.getId());
                factionData.put(faction.getId(), tempData);
                return tempData;
            }
        }

        return null;
    }
}
