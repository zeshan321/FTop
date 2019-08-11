package com.zeshanaslam.ftop.utils;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.zeshanaslam.ftop.FTopStats;
import com.zeshanaslam.ftop.Main;
import com.zeshanaslam.ftop.config.challenges.FactionData;
import com.zeshanaslam.ftop.config.challenges.Objective;
import com.zeshanaslam.ftop.config.challenges.SectionObjectives;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class FTopUtils {

    private final Main main;
    private final Random random;
    public List<FTopStats> stats;

    public FTopUtils(Main main) {
        this.main = main;
        this.random = new Random();
    }

    public String getMaterialName(Block block) {
        BlockState blockState = block.getState();

        String material = block.getType().name();
        if (blockState instanceof CreatureSpawner) {
            CreatureSpawner creatureSpawner = ((CreatureSpawner) blockState);
            material = main.silkUtil.getCreatureName(creatureSpawner.getSpawnedType().getTypeId()) + "_" + material;
        }

        return material.toUpperCase();
    }

    public boolean isValidFaction(Faction faction) {
        return !(faction == null || faction.isSafeZone() || faction.isWilderness() || faction.isWarZone());
    }

    public UUID getRichestPlayer(FTopStats fTopStats) {
        double highest = Double.MIN_VALUE;
        UUID richest = null;

        HashMap<UUID, Double> players = fTopStats.players;
        for (UUID uuid : players.keySet()) {
            double points = players.get(uuid);

            if (points > highest) {
                highest = points;
                richest = uuid;
            }
        }

        // If no points logged use admin
        if (richest == null) {
            Faction faction = Factions.getInstance().getFactionById(fTopStats.factionId);
            if (faction == null || faction.getFPlayerAdmin() == null || faction.getFPlayerAdmin().getAccountId() == null) {
                return null;
            }

            return UUID.fromString(faction.getFPlayerAdmin().getAccountId());
        }

        return richest;
    }

    public double getServerTotal() {
        double total = 0;

        for (FTopStats fTopStats : stats) {
            total = total + fTopStats.totalPoints;
        }

        return total;
    }

    public String getHoverText(FTopStats fTopStats) {
        StringBuilder stringBuilder = new StringBuilder("");

        UUID richest = getRichestPlayer(fTopStats);
        String richestName = (richest != null) ? Bukkit.getOfflinePlayer(richest).getName() : "N/A";
        double points = (richest != null && fTopStats.players.containsKey(richest)) ? fTopStats.players.get(richest) : 0;

        Faction faction = Factions.getInstance().getFactionById(fTopStats.factionId);
        for (String hover : main.configStore.topHover) {
            hover = hover.replace("%leader%", faction.getFPlayerAdmin().getName())
                    .replace("%points%", String.valueOf(Math.round(fTopStats.totalPoints * 100.0) / 100.0))
                    .replace("%blocks%", String.valueOf(fTopStats.blocks.size()))
                    .replace("%richest%", richestName)
                    .replace("%richestpoints%", String.valueOf(Math.round(points * 100.0) / 100.0))
                    .replace("%dailyfinished%", String.valueOf(fTopStats.dailyFinished))
                    .replace("%weeklyfinished%", String.valueOf(fTopStats.weeklyFinished))
                    .replace("%blockpoints%", String.valueOf(Math.round(fTopStats.blockTotalPoints * 100.0) / 100.0))
                    .replace("%inventorypoints%", String.valueOf(Math.round(fTopStats.invTotalPoints * 100.0) / 100.0))
                    .replace("%ecopoints%", String.valueOf(Math.round(fTopStats.ecoTotalPoints * 100.0) / 100.0))
                    .replace("%warnings%", String.valueOf(fTopStats.warning))
                    .replace("%strikes%", String.valueOf(fTopStats.strike))
                    .replace("%koth%", String.valueOf(fTopStats.kothAdmin));

            if (hover.contains("%block-check{")) {
                String material = hover.substring(hover.indexOf("%block-check{") + 13).split("}")[0];

                String display = WordUtils.capitalize(material.replace("_", " ").toLowerCase());
                hover = hover.replace("%block-check{" + material + "}%", display)
                        .replace("%count%", (fTopStats.blocks.containsKey(material)) ? String.valueOf(fTopStats.blocks.get(material)) : String.valueOf(0));
            }

            if (hover.contains("%inv-check{")) {
                String material = hover.substring(hover.indexOf("%inv-check{") + 11).split("}")[0];

                int amount = 0;
                if (fTopStats.items.containsKey(material.toUpperCase())) {
                    amount = fTopStats.items.get(material.toUpperCase());
                } else if (fTopStats.items.containsKey(material)) {
                    amount = fTopStats.items.get(material);
                }

                String display = WordUtils.capitalize(material.replace("_", " ").toLowerCase());
                hover = hover.replace("%inv-check{" + material + "}%", display)
                        .replace("%count%", String.valueOf(amount));
            }

            stringBuilder.append(hover).append("\n");
        }

        return stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
    }

    public int getRandomNumberInRange(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    public void updateFactionStats(Player player, String key, double value) {
        String base = "";
        if (key.contains("-")) {
            base = key.split("-")[0];
        }

        updateFactionStatsDirectly(player, key, value);
        updateFactionStatsDirectly(player, base + "-*", value);
    }

    public void updateFactionStatsDirectly(Player player, String key, double value) {
        FactionData factionData = main.configStore.challengesStore.getFactionData(player);

        if (main.configStore.challengesStore.daily.objectives.containsKey(key)) {
            if (factionData != null && !factionData.dailyCompleted)
                factionData.addValue(key, false, value);
        }

        if (main.configStore.challengesStore.weekly.objectives.containsKey(key)) {
            if (factionData != null && !factionData.weeklyCompleted)
                factionData.addValue(key, true, value);
        }

        if (factionData != null)
            checkForComplete(factionData);
    }

    private void checkForComplete(FactionData factionData) {
        SectionObjectives daily = main.configStore.challengesStore.daily;

        if (!factionData.dailyCompleted && hasCompletedSection(factionData, daily, true)) {
            factionData.setDailyStatus(true);

            Faction faction = Factions.getInstance().getFactionById(factionData.factionId);
            FPlayer admin = faction.getFPlayerAdmin();

            for (String commands : main.configStore.challengesStore.dailyRewards) {
                commands = commands.replace("%player%", admin.getName())
                        .replace("%uuid%", admin.getAccountId())
                        .replace("%factionid%", faction.getId());

                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), commands);
            }

            main.dbContext.getBlockTable().asyncLogBlock(UUID.fromString(admin.getAccountId()), "DAILYFIN", faction.getId(), null, -9999, -9999, -9999);
        }


        SectionObjectives weekly = main.configStore.challengesStore.weekly;
        if (!factionData.weeklyCompleted && hasCompletedSection(factionData, weekly, false)) {
            factionData.setWeeklyStatus(true);

            Faction faction = Factions.getInstance().getFactionById(factionData.factionId);
            FPlayer admin = faction.getFPlayerAdmin();

            for (String commands : main.configStore.challengesStore.weeklyRewards) {
                commands = commands.replace("%player%", admin.getName())
                        .replace("%uuid%", admin.getAccountId())
                        .replace("%factionid%", faction.getId());

                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), commands);
            }

            main.dbContext.getBlockTable().asyncLogBlock(UUID.fromString(admin.getAccountId()), "WEEKFIN", faction.getId(), null, -9999, -9999, -9999);
        }
    }

    private boolean hasCompletedSection(FactionData factionData, SectionObjectives sectionObjectives, boolean daily) {
        boolean completed = true;
        for (String dailyObjective : sectionObjectives.objectives.keySet()) {
            Objective objective = sectionObjectives.objectives.get(dailyObjective);

            if (daily) {
                if (factionData.getDailyValue(dailyObjective) < objective.amount) {
                    completed = false;
                }
            } else {
                if (factionData.getWeeklyValue(dailyObjective) < objective.amount) {
                    completed = false;
                }
            }
        }

        return completed;
    }
}
