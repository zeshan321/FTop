package com.zeshanaslam.ftop.config.npc;

import java.util.List;

public class HologramData {
    public List<String> hologramData;
    public double xOffset;
    public double yOffset;
    public double zOffset;

    public HologramData(List<String> hologramData, double xOffset, double yOffset, double zOffset) {
        this.hologramData = hologramData;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }
}
