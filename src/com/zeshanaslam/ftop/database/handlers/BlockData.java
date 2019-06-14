package com.zeshanaslam.ftop.database.handlers;

import org.bukkit.Material;
import org.bukkit.block.BlockState;

import java.util.List;

public class BlockData {

    public BlockState blockState;
    public Material material;
    public String materialName;
    public byte data;
    public List<ItemData> itemDataList;

    public BlockData(BlockState blockState, String materialName, Material material, byte data, List<ItemData> itemDataList) {
        this.blockState = blockState;
        this.material = material;
        this.materialName = materialName;
        this.data = data;
        this.itemDataList = itemDataList;
    }
}
