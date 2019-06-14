package com.zeshanaslam.ftop.database.handlers;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.zeshanaslam.ftop.FTopStats;
import com.zeshanaslam.ftop.Main;
import com.zeshanaslam.ftop.config.WorthData;
import com.zeshanaslam.ftop.database.DBContext;
import com.zeshanaslam.ftop.utils.FTopStatsCompare;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Date;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class BlockTable {

    private final DBContext dbContext;

    public BlockTable(DBContext dbContext) {
        this.dbContext = dbContext;
    }

    public void create() {
        String sql = "CREATE TABLE blocks (" +
                "    Id INTEGER NOT NULL PRIMARY KEY," +
                "    Player TEXT NOT NULL," +
                "    Material TEXT NOT NULL," +
                "    FactionId TEXT NOT NULL," +
                "    World TEXT NULL," +
                "    LocationX INTEGER NOT NULL," +
                "    LocationY INTEGER NOT NULL," +
                "    LocationZ INTEGER NOT NULL," +
                "    PlacedDate NUMERIC NOT NULL);";

        try {
            Connection connection = dbContext.getConnection();
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void asyncLogBlock(UUID playerUUID, String material, String factionId, UUID world, int blockX, int blockY, int blockZ) {
        dbContext.getInstance().getServer().getScheduler().runTaskAsynchronously(dbContext.getInstance(), () -> {
            logBlock(playerUUID, material, factionId, world, blockX, blockY, blockZ);
        });
    }

    public void logBlock(UUID playerUUID, String material, String factionId, UUID world, int blockX, int blockY, int blockZ) {
        String sql = "INSERT INTO blocks (Player, Material, FactionId, World, LocationX, LocationY, LocationZ, PlacedDate) VALUES(?,?,?,?,?,?,?,?)";

        try (PreparedStatement preparedStatement = dbContext.getConnection().prepareStatement(sql)) {
            preparedStatement.setString(1, playerUUID.toString());
            preparedStatement.setString(2, material);
            preparedStatement.setString(3, factionId);
            preparedStatement.setString(4, (world == null) ? null : world.toString());
            preparedStatement.setInt(5, blockX);
            preparedStatement.setInt(6, blockY);
            preparedStatement.setInt(7, blockZ);
            preparedStatement.setDate(8, new Date(Calendar.getInstance().getTimeInMillis()));

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void asyncRemoveBlock(UUID world, int blockX, int blockY, int blockZ) {
        dbContext.getInstance().getServer().getScheduler().runTaskAsynchronously(dbContext.getInstance(), () -> {
            removeBlock(world, blockX, blockY, blockZ);
        });
    }

    public void asyncRemoveBlock(List<RemoveBlock> removeBlocks, OnRemoveComplete onRemoveComplete) {
        dbContext.getInstance().getServer().getScheduler().runTaskAsynchronously(dbContext.getInstance(), () -> {
            for (RemoveBlock removeBlock: removeBlocks) {
                removeBlock(removeBlock.world, removeBlock.blockX, removeBlock.blockY, removeBlock.blockZ);
            }

            onRemoveComplete.onComplete();
        });
    }

    public void removeBlock(UUID world, int blockX, int blockY, int blockZ) {
        String sql = "DELETE FROM blocks WHERE World = ? AND LocationX = ? AND LocationY = ? AND LocationZ = ?";

        try (PreparedStatement preparedStatement = dbContext.getConnection().prepareStatement(sql)) {
            preparedStatement.setString(1, world.toString());
            preparedStatement.setInt(2, blockX);
            preparedStatement.setInt(3, blockY);
            preparedStatement.setInt(4, blockZ);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeBlockByMaterial(String material) {
        String sql = "DELETE FROM blocks WHERE " + material;

        try (PreparedStatement preparedStatement = dbContext.getConnection().prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteFactionData(String factionId) {
        String sql = "DELETE FROM blocks WHERE FactionId = ?";

        try (PreparedStatement preparedStatement = dbContext.getConnection().prepareStatement(sql)) {
            preparedStatement.setString(1, factionId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void asyncDeleteFactionData(String factionId) {
        dbContext.getInstance().getServer().getScheduler().runTaskAsynchronously(dbContext.getInstance(), () -> {
            deleteFactionData(factionId);
        });
    }

    public void updateOwnership(UUID playerUUID, String factionId, UUID world, int blockX, int blockY, int blockZ, String material) {
        String sql = "UPDATE blocks SET Player = ?, FactionId = ?, Material = ? WHERE World = ? AND LocationX = ? AND LocationY = ? AND LocationZ = ?";

        try (PreparedStatement preparedStatement = dbContext.getConnection().prepareStatement(sql)) {
            preparedStatement.setString(1, playerUUID.toString());
            preparedStatement.setString(2, factionId);
            preparedStatement.setString(3, material);
            preparedStatement.setString(4, world.toString());
            preparedStatement.setInt(5, blockX);
            preparedStatement.setInt(6, blockY);
            preparedStatement.setInt(7, blockZ);

            if (preparedStatement.executeUpdate() <= 0) {
                logBlock(playerUUID, material, factionId, world, blockX, blockY, blockZ);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void asyncUpdateOwnership(UUID playerUUID, String factionId, UUID world, int blockX, int blockY, int blockZ, String material) {
        dbContext.getInstance().getServer().getScheduler().runTaskAsynchronously(dbContext.getInstance(), () -> {
            updateOwnership(playerUUID, factionId, world, blockX, blockY, blockZ, material);
        });
    }

    public void updateMaterial(UUID world, int blockX, int blockY, int blockZ, String material) {
        dbContext.getInstance().getServer().getScheduler().runTaskAsynchronously(dbContext.getInstance(), () -> {
            String sql = "UPDATE blocks SET Material = ? WHERE World = ? AND LocationX = ? AND LocationY = ? AND LocationZ = ?";

            try (PreparedStatement preparedStatement = dbContext.getConnection().prepareStatement(sql)) {
                preparedStatement.setString(1, material);
                preparedStatement.setString(2, world.toString());
                preparedStatement.setInt(3, blockX);
                preparedStatement.setInt(4, blockY);
                preparedStatement.setInt(5, blockZ);

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public int asyncCalculateWorth(OnCalculateWorth onCalculateWorth) {
        BukkitTask bukkitTask = dbContext.getInstance().getServer().getScheduler().runTaskAsynchronously(dbContext.getInstance(), () -> {
            HashMap<String, FTopStats> fTopStatsHashMap = new HashMap<>();

            for (Faction faction : Factions.getInstance().getAllFactions()) {
                if (!dbContext.getInstance().fTopUtils.isValidFaction(faction))
                    continue;

                if (!fTopStatsHashMap.containsKey(faction.getId())) {
                    fTopStatsHashMap.put(faction.getId(), new FTopStats(faction.getId()));
                }

                FTopStats fTopStats = fTopStatsHashMap.get(faction.getId());

                String sql = "SELECT * FROM blocks WHERE FactionId = ?";
                List<BlockTableData> blockTableDataList = new ArrayList<>();

                try (PreparedStatement preparedStatement = dbContext.getConnection().prepareStatement(sql)) {
                    preparedStatement.setString(1, faction.getId());

                    ResultSet resultSet = preparedStatement.executeQuery();

                    while (resultSet.next()) {
                        UUID player = UUID.fromString(resultSet.getString(2));
                        String material = resultSet.getString(3);
                        String factionId = resultSet.getString(4);
                        UUID world = (resultSet.getString(5) == null) ? null : UUID.fromString(resultSet.getString(5));
                        int blockX = resultSet.getInt(6);
                        int blockY = resultSet.getInt(7);
                        int blockZ = resultSet.getInt(8);
                        Date placedDate = resultSet.getDate(9);

                        blockTableDataList.add(new BlockTableData(player, material, factionId, world, blockX, blockY, blockZ, placedDate));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                double points = 0;
                double blockPoints = 0;
                int dailyFinished = 0;
                int weeklyFinished = 0;
                double ecoPoints = 0;
                double invPoints = 0;

                for (BlockTableData blockTableData : blockTableDataList) {
                    if (blockTableData.material.equals("WEEKFIN")) {// A single record = 1 completed weekly challenge.
                        weeklyFinished++;
                    } else if (blockTableData.material.equals("DAILYFIN")) { // A single record = 1 completed daily challenge.
                        dailyFinished++;
                    } else if (blockTableData.material.startsWith("CUSTOMFTOP")) { // Manually added points.
                        double amount = Double.parseDouble(blockTableData.material.replace("CUSTOMFTOP", ""));
                        points = points + amount;
                    } else {
                        // Added this to not create sync methods on non-chest blocks. Without large data takes long to calculate.
                        if (blockTableData.material.contains("CHEST")) {
                            BlockData blockData = null;
                            try {
                                blockData = Bukkit.getScheduler().callSyncMethod(dbContext.getInstance(), new Callable<BlockData>() {
                                    public BlockData call() {
                                        return new BlockTableCal(dbContext.getInstance(), blockTableData.world, blockTableData.blockX, blockTableData.blockY, blockTableData.blockZ).getBlockData();
                                    }
                                }).get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }

                            if (blockData == null) {
                                System.err.println("NOT FOUND: " + blockTableData.blockX + " " + blockTableData.blockY + " " + blockTableData.blockZ);
                                continue;
                            }

                            if (blockData.material == Material.AIR) {
                                removeBlock(blockTableData.world, blockTableData.blockX, blockTableData.blockY, blockTableData.blockZ);
                            } else {
                                if (!blockData.itemDataList.isEmpty()) {
                                    for (ItemData itemData : blockData.itemDataList) {
                                        String material = itemData.materialType + ((itemData.data > 0) ? "-" + itemData.data : "");
                                        WorthData worthData = dbContext.getInstance().configStore.inventories.get(material);
                                        if (worthData == null) {
                                            worthData = dbContext.getInstance().configStore.inventories.get("DEFAULT");
                                        }

                                        points = points + (worthData.points * itemData.amount);
                                        invPoints = invPoints + (worthData.points * itemData.amount);
                                        fTopStats.incrementBlocks(material);
                                        fTopStats.addPointsToPlayers(blockTableData.player, worthData.points);
                                        fTopStats.addItems(material, itemData.amount);
                                    }
                                } else {
                                    String material = blockTableData.material + ((blockData.data > 0) ? "-" + blockData.data : "");
                                    WorthData worthData = dbContext.getInstance().configStore.placed.get(material);
                                    if (worthData == null) {
                                        worthData = dbContext.getInstance().configStore.placed.get("DEFAULT");
                                    }

                                    int hours = getHours(blockTableData.placedDate, worthData.graceHours);

                                    // If difference is 0 or does not have grace hours set
                                    if (worthData.graceHours == 0) {
                                        points = points + worthData.points;
                                        blockPoints = blockPoints + worthData.points;
                                        fTopStats.addPointsToPlayers(blockTableData.player, worthData.points);
                                    } else {
                                        double generate = worthData.points / worthData.graceHours;
                                        points = points + (hours * generate);
                                        blockPoints = blockPoints + (worthData.points / hours);
                                        fTopStats.addPointsToPlayers(blockTableData.player, (worthData.points / hours));
                                    }

                                    fTopStats.incrementBlocks(material);
                                }
                            }
                        } else {
                            String material = blockTableData.material;
                            WorthData worthData = dbContext.getInstance().configStore.placed.get(material);
                            if (worthData == null) {
                                worthData = dbContext.getInstance().configStore.placed.get("DEFAULT");
                            }

                            int hours = getHours(blockTableData.placedDate, worthData.graceHours);

                            // If difference is 0 or does not have grace hours set
                            if (worthData.graceHours == 0) {
                                points = points + worthData.points;
                                blockPoints = blockPoints + worthData.points;
                                fTopStats.addPointsToPlayers(blockTableData.player, worthData.points);
                            } else {
                                double generate = worthData.points / worthData.graceHours;
                                points = points + (hours * generate);
                                blockPoints = blockPoints + (worthData.points / hours);
                                fTopStats.addPointsToPlayers(blockTableData.player, (worthData.points / hours));
                            }

                            fTopStats.incrementBlocks(material);
                        }
                    }
                }

                double totalBal = 0;

                for (FPlayer fPlayer : faction.getFPlayers()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(fPlayer.getAccountId()));

                    if (offlinePlayer != null) {
                        double bal = Main.economy.getBalance(offlinePlayer);
                        totalBal = totalBal +  bal;
                        fTopStats.addPointsToPlayers(UUID.fromString(fPlayer.getAccountId()), ((bal / dbContext.getInstance().configStore.ecoAmount) * dbContext.getInstance().configStore.ecoWorth));
                    }
                }

                if (dbContext.getInstance().configStore.ecoAmount != 0) {
                    ecoPoints = ((totalBal / dbContext.getInstance().configStore.ecoAmount) * dbContext.getInstance().configStore.ecoWorth);
                }

                points = points + ecoPoints;
                fTopStats.totalPoints = points;
                fTopStats.dailyFinished = dailyFinished;
                fTopStats.weeklyFinished = weeklyFinished;
                fTopStats.blockTotalPoints = blockPoints;
                fTopStats.invTotalPoints = invPoints;
                fTopStats.ecoTotalPoints = ecoPoints;
                fTopStatsHashMap.put(faction.getId(), fTopStats);
            }

            // Order by highest
            Collection<FTopStats> fTopStats = fTopStatsHashMap.values();
            List<FTopStats> list = new ArrayList<>(fTopStats);
            list.sort(new FTopStatsCompare());
            List<FTopStats> sorted = new ArrayList<>(list);

            onCalculateWorth.onComplete(sorted);
        });

        return bukkitTask.getTaskId();
    }

    private int getHours(Date placedDate, int graceHours) {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime placedDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(placedDate.getTime()), TimeZone.getDefault().toZoneId()).plusHours(graceHours);

        Duration duration = Duration.between(localDateTime, placedDateTime);
        if (duration.isZero() || duration.isNegative()) {
            return 0;
        }

        return graceHours - (int) duration.toHours();
    }
}
