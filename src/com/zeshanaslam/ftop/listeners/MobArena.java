package com.zeshanaslam.ftop.listeners;

import com.garbagemule.MobArena.events.NewWaveEvent;
import com.zeshanaslam.ftop.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MobArena implements Listener {

    private final Main main;

    public MobArena(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onWave(NewWaveEvent event) {
        for (Player player : event.getArena().getPlayersInArena()) {
            if (player.isDead())
                continue;

            main.fTopUtils.updateFactionStats(player, "MOBARENAWAVES", 1);
        }
    }
}
