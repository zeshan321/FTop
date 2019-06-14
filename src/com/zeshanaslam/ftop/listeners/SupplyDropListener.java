package com.zeshanaslam.ftop.listeners;

import com.zeshanaslam.ftop.Main;
import com.zeshanaslam.supplydrop.api.SupplyDropFinishedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SupplyDropListener implements Listener {

    private final Main main;

    public SupplyDropListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onWin(SupplyDropFinishedEvent event) {
        Player player = event.getPlayer();

        String key = "SupplyDrop";
        main.fTopUtils.updateFactionStats(player, key, 1);
    }
}
