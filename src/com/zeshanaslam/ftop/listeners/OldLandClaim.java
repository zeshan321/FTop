package com.zeshanaslam.ftop.listeners;

import com.massivecraft.factions.event.LandClaimEvent;
import com.massivecraft.factions.event.LandUnclaimEvent;
import com.zeshanaslam.ftop.Main;
import com.zeshanaslam.ftop.config.WorthData;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public class OldLandClaim {

    private final Main main;

    public OldLandClaim(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onClaim(LandClaimEvent event) {
        if (event.isCancelled())
            return;

        final Chunk syncChunk = event.getLocation().getChunk();
        final String factionId = event.getFaction().getId();
        final UUID player = event.getfPlayer().getPlayer().getUniqueId();
        final UUID world = event.getLocation().getWorld().getUID();
        /*final ChunkSnapshot chunk = syncChunk.getChunkSnapshot();
        final int currentX = (int) (event.getLocation().getX() << 4);
        final int currentZ = (int) (event.getLocation().getZ() << 4);*/

        for (BlockState tile : syncChunk.getTileEntities()) {
            Block block = tile.getBlock();
            short entityId = block.getType() == Material.MOB_SPAWNER ? main.silkUtil.getSpawnerEntityID(block) : 0;
            String material = (entityId == 0) ? main.fTopUtils.getMaterialName(block) : getMaterialName(block, main.silkUtil.getCreatureName(entityId));
            WorthData worthData = main.configStore.inventories.get(material);
            if (worthData != null) {
                if (worthData.points <= 0) {
                    return;
                }
            }

            main.dbContext.getBlockTable().updateOwnership(player, factionId, world, tile.getX(), tile.getY(), tile.getZ(), material);
        }

        /*main.getServer().getScheduler().runTaskAsynchronously(main, () -> {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 256; y++) {
                    for (int z = 0; z < 16; z++) {
                        int materialId = chunk.getBlockTypeId(x, y, z);
                        int data = chunk.getBlockData(x, y, z);
                        Material material = Material.getMaterial(materialId);

                        if (material != Material.AIR) {
                            int realX = currentX + x;
                            int realY = y;
                            int realZ = currentZ + z;

                            if (material == Material.MOB_SPAWNER) {
                                BlockData blockData = null;
                                try {
                                    blockData = Bukkit.getServer().getScheduler().callSyncMethod(main, () -> new BlockTableCal(main, world, realX, realY, realZ).getBlockData()).get();
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }

                                assert blockData != null;

                                if (main.configStore.placed.containsKey(blockData.materialName + ((blockData.data > 0) ? "-" + blockData.data : "")) || main.configStore.placed.containsKey(blockData.materialName + "-*")) {
                                    main.dbContext.getBlockTable().updateOwnership(player, factionId, world, realX, realY, realZ, blockData.materialName);
                                }
                            } else {
                                if (main.configStore.placed.containsKey(material.name() + ((data > 0) ? "-" + data : "")) || main.configStore.placed.containsKey(material.name() + "-*")) {
                                    main.dbContext.getBlockTable().updateOwnership(player, factionId, world, realX, realY, realZ, material.name() + ((data > 0) ? "-" + data : ""));
                                }
                            }
                        }
                    }
                }
            }
        });*/
    }

    @EventHandler
    public void onUnclaim(LandUnclaimEvent event) {
        if (event.isCancelled())
            return;

        final Chunk syncChunk = event.getLocation().getChunk();
       /* final ChunkSnapshot chunk = syncChunk.getChunkSnapshot();
        final UUID world = event.getLocation().getWorld().getUID();
        final int currentX = (int) (event.getLocation().getX() << 4);
        final int currentZ = (int) (event.getLocation().getZ() << 4);*/

        BlockState[] blockStates = syncChunk.getTileEntities();
        for (BlockState tile : blockStates) {
            main.dbContext.getBlockTable().removeBlock(tile.getWorld().getUID(), tile.getX(), tile.getY(), tile.getZ());
        }

        /*main.getServer().getScheduler().runTaskAsynchronously(main, () -> {
            for (int y = 0; y < 256; y++) {
                for (int x = 0; x < 18; x++) {
                    for (int z = 0; z < 18; z++) {
                        int materialId = chunk.getBlockTypeId(x, y, z);
                        Material material = Material.getMaterial(materialId);

                        if (material != Material.AIR) {
                            int realX = currentX + x;
                            int realZ = currentZ + z;

                            System.out.println(realX + " " + y + " " + realZ);
                            main.dbContext.getBlockTable().removeBlock(world, realX, y, realZ);
                        }
                    }
                }
            }
        });*/
    }

    private String getMaterialName(Block block, String name) {
        return (name + "_" + block.getType().name()).toUpperCase();
    }

}
