package com.zeshanaslam.ftop.config;

import com.zeshanaslam.ftop.Main;
import com.zeshanaslam.ftop.config.challenges.ChallengesStore;
import com.zeshanaslam.ftop.config.npc.NpcStore;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigStore {

    private final Main main;
    public HashMap<Messages, String> messages;
    public HashMap<String, WorthData> placed;
    public HashMap<String, WorthData> inventories;
    public int refresh;
    public double ecoAmount;
    public double ecoWorth;
    public NpcStore npcStore;
    public ChallengesStore challengesStore;
    public List<String> topText;
    public List<String> topHover;
    public int maxThreads;
    public double strikePercentRemoved;
    public double warningPercentRemoved;

    public ConfigStore(Main main) {
        this.main = main;

        messages = new HashMap<>();
        for (String key : main.getConfig().getConfigurationSection("Messages").getKeys(false)) {
            messages.put(Messages.valueOf(key), ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("Messages." + key)));
        }

        placed = new HashMap<>();
        for (String key : main.getConfig().getConfigurationSection("Worth.Placed").getKeys(false)) {
            double points = main.getConfig().getDouble("Worth.Placed." + key + ".Points");
            int graceHours = 0;
            if (main.getConfig().contains("Worth.Placed." + key + ".GraceHours")) {
                graceHours = main.getConfig().getInt("Worth.Placed." + key + ".GraceHours");
            }

            WorthData worthData = new WorthData(points, graceHours);
            placed.put(key, worthData);
        }

        inventories = new HashMap<>();
        for (String key : main.getConfig().getConfigurationSection("Worth.Inventories").getKeys(false)) {
            double points = main.getConfig().getDouble("Worth.Inventories." + key + ".Points");
            int graceHours = 0;
            if (main.getConfig().contains("Worth.Inventories." + key + ".GraceHours")) {
                graceHours = main.getConfig().getInt("Worth.Inventories." + key + ".GraceHours");
            }

            WorthData worthData = new WorthData(points, graceHours);
            inventories.put(key, worthData);
        }

        refresh = main.getConfig().getInt("Refresh");
        ecoAmount = main.getConfig().getDouble("Worth.Eco.Amount");
        ecoWorth = main.getConfig().getDouble("Worth.Eco.Points");

        npcStore = new NpcStore(main);

        topText = new ArrayList<>();
        for (String text : main.getConfig().getStringList("Top.Text")) {
            topText.add(ChatColor.translateAlternateColorCodes('&', text));
        }

        topHover = new ArrayList<>();
        for (String text : main.getConfig().getStringList("Top.Hover")) {
            topHover.add(ChatColor.translateAlternateColorCodes('&', text));
        }

        challengesStore = new ChallengesStore(main);
        maxThreads = main.getConfig().getInt("MaxThreads");
        strikePercentRemoved = main.getConfig().getDouble("StrikePercentRemoved");
        warningPercentRemoved = main.getConfig().getDouble("WarningPercentRemoved");
    }

    public void save() {
        npcStore.save();
        challengesStore.save();
    }

    public boolean isNumeric(String strNum) {
        return strNum.matches("-?\\d+(\\.\\d+)?");
    }

    public enum Messages {
        CreatedNpcs,
        LookAtNpc,
        DeletedNpc,
        NoMorePages,
        NoFaction
    }
}
