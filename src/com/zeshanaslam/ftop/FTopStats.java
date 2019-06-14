package com.zeshanaslam.ftop;

import java.util.HashMap;
import java.util.UUID;

public class FTopStats {

    public String factionId;
    public double totalPoints;
    public HashMap<String, Integer> blocks;
    public HashMap<String, Integer> items;
    public HashMap<UUID, Double> players;
    public int dailyFinished;
    public int weeklyFinished;
    public double blockTotalPoints;
    public double ecoTotalPoints;
    public double invTotalPoints;

    public FTopStats(String factionId) {
        this.factionId = factionId;
        blocks = new HashMap<>();
        items = new HashMap<>();
        players = new HashMap<>();
    }

    public void incrementBlocks(String material) {
        if (blocks.containsKey(material)) {
            blocks.put(material, blocks.get(material) + 1);
        } else {
            blocks.put(material, 1);
        }
    }

    public void addItems(String material, int amount) {
        if (items.containsKey(material)) {
            items.put(material, items.get(material) + amount);
        } else {
            items.put(material, amount);
        }
    }

    public void addPointsToPlayers(UUID player, double points) {
        if (players.containsKey(player)) {
            players.put(player, players.get(player) + points);
        } else {
            players.put(player, points);
        }
    }
}
