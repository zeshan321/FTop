package com.zeshanaslam.ftop;

import com.zeshanaslam.ftop.database.handlers.OnCalculateWorth;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class FTopTask extends BukkitRunnable {

    private final Main main;
    public static int state = -1;
    public static int asyncId = -1;

    public FTopTask(Main main) {
        this.main = main;
    }

    @Override
    public void run() {
        if (state == 0)
            return;

        state = 0;
        asyncId = main.dbContext.getBlockTable().asyncCalculateWorth(new OnCalculateWorth() {
            @Override
            public void onComplete(List<FTopStats> factionStats) {
                main.getServer().getScheduler().runTask(main, new Runnable() {
                    @Override
                    public void run() {
                        state = 1;
                        main.fTopUtils.stats = factionStats;
                        main.configStore.npcStore.loadNpcs();
                    }
                });
            }
        });
    }
}
