package com.zeshanaslam.ftop.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.LandClaimEvent;
import com.massivecraft.factions.event.LandUnclaimEvent;
import com.zeshanaslam.ftop.Main;
import com.zeshanaslam.ftop.config.WorthData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.UUID;

public class BlockListeners implements Listener {

    private final Main main;

    public BlockListeners(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        FLocation flocation = new FLocation(location);
        Faction faction = Board.getInstance().getFactionAt(flocation);

        if (!main.fTopUtils.isValidFaction(faction))
            return;

        short entityId = main.silkUtil.getStoredSpawnerItemEntityID(event.getItemInHand());
        String material = (entityId == 0) ? main.fTopUtils.getMaterialName(block) : getMaterialName(block, main.silkUtil.getCreatureName(entityId));
        WorthData worthData = main.configStore.inventories.get(material);
        if (worthData != null) {
            if (worthData.points <= 0) {
                return;
            }
        }

        main.dbContext.getBlockTable().asyncLogBlock(player.getUniqueId(), material, faction.getId(), location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private String getMaterialName(Block block, String name) {
        return (name + "_" + block.getType().name()).toUpperCase();
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        FLocation flocation = new FLocation(location);
        Faction faction = Board.getInstance().getFactionAt(flocation);

        if (!main.fTopUtils.isValidFaction(faction))
            return;

        main.dbContext.getBlockTable().asyncRemoveBlock(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent event) {
        if (event.isCancelled())
            return;

        for (Block block: event.blockList()) {
            Location location = block.getLocation();
            main.dbContext.getBlockTable().asyncRemoveBlock(block.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (event.isCancelled())
            return;

        for (Block block: event.blockList()) {
            Location location = block.getLocation();
            main.dbContext.getBlockTable().asyncRemoveBlock(block.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }
    }

    @EventHandler
    public void onClaim(LandClaimEvent event) {
        if (event.isCancelled())
            return;

        final Chunk syncChunk = event.getLocation().getChunk();
        final String factionId = event.getFaction().getId();
        final UUID player = event.getfPlayer().getPlayer().getUniqueId();
        final UUID world = event.getLocation().getWorld().getUID();

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

            main.dbContext.getBlockTable().asyncUpdateOwnership(player, factionId, world, tile.getX(), tile.getY(), tile.getZ(), material);
        }
    }

    @EventHandler
    public void onUnclaim(LandUnclaimEvent event) {
        if (event.isCancelled())
            return;

        final Chunk syncChunk = event.getLocation().getChunk();

        BlockState[] blockStates = syncChunk.getTileEntities();
        for (BlockState tile : blockStates) {
            main.dbContext.getBlockTable().asyncRemoveBlock(tile.getWorld().getUID(), tile.getX(), tile.getY(), tile.getZ());
        }
    }
}
