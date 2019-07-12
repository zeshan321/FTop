package com.zeshanaslam.ftop.database.handlers;

import com.zeshanaslam.ftop.config.npc.SafeLocation;
import org.bukkit.Location;

public class LocationData {

    public SafeLocation location;
    public String material;

    public LocationData(SafeLocation location, String material) {
        this.location = location;
        this.material = material;
    }
}
