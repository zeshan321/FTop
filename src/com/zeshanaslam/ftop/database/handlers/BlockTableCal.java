package com.zeshanaslam.ftop.database.handlers;

import com.zeshanaslam.ftop.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlockTableCal {

    private final Main main;
    private final UUID world;
    private final int x;
    private final int y;
    private final int z;

    public BlockTableCal(Main main, UUID world, int x, int y, int z) {
        this.main = main;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockData getBlockData() {
        Location location = new Location(Bukkit.getWorld(world), x, y, z);
        Block block = location.getBlock();
        BlockState blockState = block.getState();
        Material material = block.getType();
        byte data = block.getData();

        List<ItemData> itemData = new ArrayList<>();
        if (blockState instanceof Chest) {
            Chest chest = (Chest) blockState;
            for (int i = 0; i < 27; i++) {
                ItemStack itemStack = chest.getBlockInventory().getItem(i);
                if (itemStack == null)
                    continue;

                itemData.add(new ItemData(itemStack.getType().name(), itemStack.getAmount(), itemStack.getData().getData()));
            }
        } else if (blockState instanceof InventoryHolder) {
            InventoryHolder inventoryHolder = (InventoryHolder) blockState;
            for (ItemStack itemStack : inventoryHolder.getInventory().getContents()) {
                if (itemStack == null)
                    continue;

                itemData.add(new ItemData(itemStack.getType().name(), itemStack.getAmount(), itemStack.getData().getData()));
            }
        }

        return new BlockData(blockState, main.fTopUtils.getMaterialName(block), material, data, itemData);
    }
}
