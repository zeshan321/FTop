package com.zeshanaslam.ftop.commands;

import app.ashcon.intake.Command;
import app.ashcon.intake.bukkit.parametric.annotation.Sender;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.zeshanaslam.ftop.FTopTask;
import com.zeshanaslam.ftop.Main;
import com.zeshanaslam.ftop.config.ConfigStore;
import com.zeshanaslam.ftop.config.npc.Npc;
import com.zeshanaslam.ftop.config.npc.SafeLocation;
import com.zeshanaslam.ftop.database.handlers.LocationData;
import com.zeshanaslam.ftop.utils.TargetHelper;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FTopCommands {

    private final Main main;

    public FTopCommands(Main main) {
        this.main = main;
    }

    @Command(
            aliases = "createNpc",
            desc = "Create npcs",
            perms = "ftop.createnpc"
    )
    public void createNpc(@Sender Player sender, int rank) {
        Location loc = sender.getLocation();
        SafeLocation safeLocation = new SafeLocation(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getPitch(), loc.getYaw());
        Npc npc = new Npc(rank, safeLocation, null);

        main.configStore.npcStore.npcs.put(npc, loc);
        sender.sendMessage(main.configStore.messages.get(ConfigStore.Messages.CreatedNpcs));
    }

    @Command(
            aliases = "deleteNpc",
            desc = "Delete npcs",
            perms = "ftop.deletenpc"
    )
    public void deleteNpc(@Sender Player sender) {
        LivingEntity livingEntity = TargetHelper.getLivingTarget(sender, 5);
        if (livingEntity == null) {
            sender.sendMessage(main.configStore.messages.get(ConfigStore.Messages.LookAtNpc));
            return;
        }

        Npc npc = null;
        List<Npc> tempNpcs = new ArrayList<>(main.configStore.npcStore.npcs.keySet());
        for (Npc tempNpc : tempNpcs) {
            if (tempNpc.safeLocation.x == livingEntity.getLocation().getBlockX() && tempNpc.safeLocation.y == livingEntity.getLocation().getBlockY() && tempNpc.safeLocation.z == livingEntity.getLocation().getBlockZ()) {
                npc = tempNpc;
                break;
            }
        }

        if (npc == null) {
            sender.sendMessage(main.configStore.messages.get(ConfigStore.Messages.LookAtNpc));
            return;
        }

        sender.sendMessage(livingEntity.getName());

        net.citizensnpcs.api.npc.NPC citizenNpc = CitizensAPI.getNPCRegistry().getByUniqueId(npc.npcUUID);
        if (citizenNpc == null) {
            sender.sendMessage(main.configStore.messages.get(ConfigStore.Messages.LookAtNpc));
            return;
        }

        for (Hologram hologram : main.configStore.npcStore.holograms.values()) {
            hologram.delete();
        }

        main.configStore.npcStore.holograms.clear();
        main.configStore.npcStore.npcs.remove(npc);
        citizenNpc.destroy();
        main.configStore.npcStore.loadNpcs();
        sender.sendMessage(main.configStore.messages.get(ConfigStore.Messages.DeletedNpc));
    }

    @Command(
            aliases = "update",
            desc = "Update faction stat for objective",
            perms = "ftop.update"
    )
    public void update(CommandSender sender, String objective, String playerName, double amount) {
        Player player = Bukkit.getPlayer(playerName);

        if (player != null) {
            main.fTopUtils.updateFactionStats(player, objective.toUpperCase(), amount);
            sender.sendMessage(ChatColor.GREEN + "Updated " + objective.toUpperCase());
        } else {
            sender.sendMessage(ChatColor.RED + "Player not found!");
        }
    }

    @Command(
            aliases = "points",
            desc = "Adds points to faction by player",
            perms = "ftop.points"
    )
    public void points(CommandSender sender, String uuidString, String factionId, double amount) {
        Faction faction = Factions.getInstance().getFactionById(factionId);
        if (faction == null)
            return;

        UUID uuid = UUID.fromString(uuidString);
        main.dbContext.getBlockTable().asyncLogBlock(uuid, "CUSTOMFTOP" + amount, factionId, null, -9999, -9999, -9999);
        sender.sendMessage(ChatColor.GREEN + "Added " + amount + " points!");
    }

    @Command(
            aliases = "ver",
            desc = "Gets version",
            perms = "ftop.ver"
    )
    public void ver(CommandSender sender) {
        sender.sendMessage(main.getDescription().getVersion());
    }

    @Command(
            aliases = "cancel",
            desc = "Cancels calculation",
            perms = "ftop.cancel"
    )
    public void cancel(CommandSender sender) {
        if (FTopTask.state == -1) {
            sender.sendMessage("Currently not calculating.");
            return;
        }

        if (FTopTask.state == 0) {
            sender.sendMessage("Currently calculating.");
        }

        if (FTopTask.state == 1) {
            sender.sendMessage("Done calculating.");
        }

        if (Bukkit.getServer().getScheduler().isQueued(FTopTask.asyncId) || Bukkit.getServer().getScheduler().isCurrentlyRunning(FTopTask.asyncId)) {
            sender.sendMessage("Doing async calculating. Canceling.");
            Bukkit.getServer().getScheduler().cancelTask(FTopTask.asyncId);
        }

        sender.sendMessage("Doing sync calculating. Canceling.");
        main.fTopTask.cancel();

        sender.sendMessage("Cancelling all tasks except for challenges...");
        for (BukkitTask bukkitTask: Bukkit.getServer().getScheduler().getPendingTasks()) {
            if (bukkitTask.getOwner().getName().equals(main.getDescription().getName())) {
                if (bukkitTask.getTaskId() != main.challengesTask.getTaskId()) {
                    bukkitTask.cancel();
                }
            }
        }

        sender.sendMessage("Starting calculating again...");
        main.fTopTask = new FTopTask(main).runTaskTimer(main, 0, main.configStore.refresh * 20);
        FTopTask.state = -1;
    }

    @Command(
            aliases = "tasks",
            desc = "Gets all tasks",
            perms = "ftop.tasks"
    )
    public void tasks(CommandSender sender) {
        for (BukkitTask bukkitTask: Bukkit.getServer().getScheduler().getPendingTasks()) {
            if (bukkitTask.getOwner().getName().equals(main.getDescription().getName())) {
                sender.sendMessage("" + bukkitTask.getTaskId());
            }
        }
    }

    @Command(
            aliases = "purge",
            desc = "Purge old data",
            perms = "ftop.purge"
    )
    public void purge(CommandSender sender) {
        String materials = "MATERIAL != 'CHEST' AND Material NOT LIKE 'CHEST-%' AND ";
        for (String type: main.configStore.placed.keySet()) {
            materials += "Material != '" + type + "' AND ";
        }

        materials = materials.substring(0, materials.length() - 4);
        main.dbContext.getBlockTable().removeBlockByMaterial(materials);
        sender.sendMessage("Done.");
    }

    @Command(
            aliases = "recalc",
            desc = "Recalc data",
            perms = "ftop.recalc"
    )
    public void recalc (CommandSender sender) {
        int removed = 0;

        for (LocationData locationData: main.dbContext.getBlockTable().getAll()) {
            String material = main.fTopUtils.getMaterialName(locationData.location.getBlock());

            if (!material.equals(locationData.material)) {
                removed++;

                Location location = locationData.location;
                main.dbContext.getBlockTable().asyncRemoveBlock(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Removed " + removed + " blocks during recalculation.");
    }
}
