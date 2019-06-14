package com.zeshanaslam.ftop.database.handlers;

import java.util.UUID;

public class RemoveBlock {

    public UUID world;
    public int blockX;
    public int blockY;
    public int blockZ;

    public RemoveBlock(UUID world, int blockX, int blockY, int blockZ) {
        this.world = world;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
    }
}
