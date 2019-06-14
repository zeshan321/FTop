package com.zeshanaslam.ftop.database.handlers;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.UUID;

public class BlockTableData {

    public UUID player;
    public String material;
    public String factionId;
    public UUID world;
    public int blockX;
    public int blockY;
    public int blockZ;
    public Date placedDate;

    public BlockTableData(UUID player, String material, String factionId, UUID world, int blockX, int blockY, int blockZ, Date placedDate) {
        this.player = player;
        this.material = material;
        this.factionId = factionId;
        this.world = world;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.placedDate = placedDate;
    }
}
