package com.zeshanaslam.ftop.config.npc;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public class SafeLocation {
    public UUID world;
    public int x;
    public int y;
    public int z;
    public float pitch;
    public float yaw;

    public SafeLocation() {
    }

    public SafeLocation(UUID world, int x, int y, int z, float pitch, float yaw) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public Location getLocation() {
        Location location = new Location(Bukkit.getWorld(world), x, y, z);
        location.setPitch(pitch);
        location.setYaw(yaw);

        return location;
    }

    public SafeLocation fromLocation(Location location) {
        world = location.getWorld().getUID();
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
        pitch = location.getPitch();
        yaw = location.getYaw();

        return this;
    }
}
