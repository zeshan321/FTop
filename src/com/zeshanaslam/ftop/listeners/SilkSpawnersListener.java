package com.zeshanaslam.ftop.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.zeshanaslam.ftop.Main;
import com.zeshanaslam.ftop.config.WorthData;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerChangeEvent;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerPlaceEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SilkSpawnersListener implements Listener {

    private final Main main;

    public SilkSpawnersListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onSpawnerChange(SilkSpawnersSpawnerChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        FLocation flocation = new FLocation(location);
        Faction faction = Board.getInstance().getFactionAt(flocation);

        if (!main.fTopUtils.isValidFaction(faction))
            return;

        String material = getMaterialName(block, event.getSpawner().getCreatureTypeName());
        WorthData worthData = main.configStore.inventories.get(material);
        if (worthData != null) {
            if (worthData.points <= 0) {
                return;
            }
        }

        main.dbContext.getBlockTable().updateMaterial(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), material);
    }

    @EventHandler
    public void onSpawnerPlace(SilkSpawnersSpawnerPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        FLocation flocation = new FLocation(location);
        Faction faction = Board.getInstance().getFactionAt(flocation);

        if (!main.fTopUtils.isValidFaction(faction))
            return;

        String material = getMaterialName(block, main.silkUtil.getCreatureName(event.getEntityID()));
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
}
