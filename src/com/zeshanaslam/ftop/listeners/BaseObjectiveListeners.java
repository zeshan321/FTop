package com.zeshanaslam.ftop.listeners;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.zeshanaslam.ftop.FTopStats;
import com.zeshanaslam.ftop.Main;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;


public class BaseObjectiveListeners implements Listener {

    private final Main main;

    public BaseObjectiveListeners(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onDisband(FactionDisbandEvent event) {
        main.dbContext.getBlockTable().asyncDeleteFactionData(event.getFaction().getId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event){
        if (!event.getFormat().contains("{ftop_rank}"))
            return;

        Player player = event.getPlayer();
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        int rank = -1;
        if (main.fTopUtils.stats != null) {
            List<FTopStats> fTopStatsList = new ArrayList<>(main.fTopUtils.stats);
            for (FTopStats fTopStats : fTopStatsList) {
                rank++;
                if (fTopStats.factionId.equalsIgnoreCase(fPlayer.getFactionId())) {
                    event.setFormat(event.getFormat().replace("{ftop_rank}", String.valueOf(rank + 1)));
                    return;
                }
            }
        }

        event.setFormat(event.getFormat().replace("{ftop_rank}", ""));
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.isCancelled())
            return;

        if (event.getCaught() == null)
            return;

        Player player = event.getPlayer();

        main.fTopUtils.updateFactionStats(player, "FISH", 1);
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.getEntity().getKiller() == null)
            return;

        Player player = entity.getKiller();

        String key = "KILL-" + entity.getType().name();
        main.fTopUtils.updateFactionStats(player, key, 1);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        if (event.getTo().distance(event.getFrom()) >= 1) {
            String type = "FOOT";
            if (player.getVehicle() != null) {
                type = player.getVehicle().getType().name();
            }

            String key = "TRAVEL-" + type;
            main.fTopUtils.updateFactionStats(player, key, 1);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        Block block = event.getBlock();

        String key = "BLOCKBREAK-" + block.getType().name();
        main.fTopUtils.updateFactionStats(player, key, 1);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        Block block = event.getBlock();

        String key = "BLOCKPLACE-" + block.getType().name();
        main.fTopUtils.updateFactionStats(player, key, 1);
    }

    /*@EventHandler
    public void onMake(UserBalanceUpdateEvent event) {
        Player player = event.getPlayer();

        BigDecimal diff = event.getNewBalance().subtract(event.getOldBalance());

        String key = "MAKE";
        main.fTopUtils.updateFactionStats(player, key, diff.doubleValue());
    }*/
}
