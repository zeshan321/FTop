package com.zeshanaslam.ftop.database.handlers;

import org.bukkit.Location;

public class LocationData {

    public Location location;
    public String material;

    public LocationData(Location location, String material) {
        this.location = location;
        this.material = material;
    }
}
