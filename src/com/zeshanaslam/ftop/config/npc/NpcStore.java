package com.zeshanaslam.ftop.config.npc;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.gson.Gson;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.zeshanaslam.ftop.FTopStats;
import com.zeshanaslam.ftop.Main;
import com.zeshanaslam.ftop.utils.FileHandler;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class NpcStore {

    private final Main main;
    private final Gson gson;
    public String defaultName;
    public HologramData hologramData;
    public HashMap<Npc, Location> npcs;
    public HashMap<Location, Hologram> holograms;
    public HashMap<UUID, String> names;

    public NpcStore(Main main) {
        this.main = main;
        this.gson = new Gson();
        this.npcs = new HashMap<>();
        this.holograms = new HashMap<>();
        this.names = new HashMap<>();

        FileHandler fileHandler = new FileHandler("plugins/FTop/npcs.yml");
        if (fileHandler.contains("Npcs")) {
            List<String> signData = fileHandler.getStringList("Npcs");
            for (String json : signData) {
                Npc npc = gson.fromJson(json, Npc.class);

                npcs.put(npc, npc.safeLocation.getLocation());
            }
        }

        // Hologram
        List<String> hologramDataString = new ArrayList<>();
        for (String hologramString : main.getConfig().getStringList("Npc.Hologram.Data")) {
            hologramDataString.add(ChatColor.translateAlternateColorCodes('&', hologramString));
        }
        double xOffset = main.getConfig().getDouble("Npc.Hologram.XOffset");
        double yOffset = main.getConfig().getDouble("Npc.Hologram.YOffset");
        double zOffset = main.getConfig().getDouble("Npc.Hologram.ZOffset");

        hologramData = new HologramData(hologramDataString, xOffset, yOffset, zOffset);

        defaultName = main.getConfig().getString("Npc.Default");
    }

    public void loadNpcs() {
        List<FTopStats> keys = new ArrayList<>(main.fTopUtils.stats);

        List<Npc> tempNpcs = new ArrayList<>(npcs.keySet());
        for (Npc npc: tempNpcs) {
            if (keys.size() >= npc.rank) {
                FTopStats fTopStats = keys.get(npc.rank - 1);
                Faction faction = Factions.getInstance().getFactionById(fTopStats.factionId);
                Location location = npc.safeLocation.getLocation();
                String adminName = faction.getFPlayerAdmin().getName();

                NPC citizenNpc;
                if (npc.npcUUID == null) {
                    citizenNpc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, adminName);
                } else {
                    citizenNpc = CitizensAPI.getNPCRegistry().getByUniqueId(npc.npcUUID);
                    if (citizenNpc != null) {
                        if (!citizenNpc.getName().equals(adminName))
                            citizenNpc.setName(adminName);
                    } else {
                        citizenNpc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, adminName);
                    }
                }

                location = location.add(hologramData.xOffset, 0, hologramData.zOffset);

                if (!citizenNpc.isSpawned()) {
                    citizenNpc.spawn(location);
                }

                if (citizenNpc.isSpawned()) {
                    names.put(citizenNpc.getEntity().getUniqueId(), "");
                }

                npcs.remove(npc);
                npc.npcUUID = citizenNpc.getUniqueId();
                npcs.put(npc, location);

                location = location.add(0, hologramData.yOffset, 0);

                Hologram hologram;
                if (holograms.containsKey(location)) {
                    hologram = holograms.get(location);
                } else {
                    hologram = HologramsAPI.createHologram(main, location);
                    holograms.put(location, hologram);
                }

                addHologramText(hologram, hologramData.hologramData, String.valueOf(npc.rank), faction.getTag(),
                        faction.getFPlayerAdmin().getName(), String.valueOf(Math.round(fTopStats.totalPoints * 100.0) / 100.0));
            } else {
                Location location = npc.safeLocation.getLocation();

                NPC citizenNpc;
                if (npc.npcUUID == null) {
                    citizenNpc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, defaultName);
                } else {
                    citizenNpc = CitizensAPI.getNPCRegistry().getByUniqueId(npc.npcUUID);
                    if (citizenNpc != null) {
                        if (!citizenNpc.getName().equals(defaultName))
                            citizenNpc.setName(defaultName);
                    } else {
                        citizenNpc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, defaultName);
                    }
                }

                location = location.add(hologramData.xOffset, 0, hologramData.zOffset);

                if (!citizenNpc.isSpawned()) {
                    citizenNpc.spawn(location);
                }

                if (citizenNpc.isSpawned()) {
                    names.put(citizenNpc.getEntity().getUniqueId(), "N/A");
                }

                npcs.remove(npc);
                npc.npcUUID = citizenNpc.getUniqueId();
                npcs.put(npc, location);
            }
        }
    }

    public void save() {
        FileHandler fileHandler = new FileHandler("plugins/FTop/npcs.yml");

        List<String> json = new ArrayList<>();
        for (Npc npc : npcs.keySet()) {
            json.add(gson.toJson(npc));
        }

        fileHandler.createNewStringList("Npcs", json);
        fileHandler.save();
    }

    private void addHologramText(Hologram hologram, List<String> displayText, String rank, String factionTag, String leader, String totalPoints) {
        if (hologram.size() != displayText.size()) {
            hologram.clearLines();

            for (String text: displayText) {
                text = text.replace("%rank%", rank)
                        .replace("%faction%", factionTag)
                        .replace("%leader%", leader)
                        .replace("%totalpoints%", totalPoints);

                hologram.appendTextLine(text);
            }
        } else {
            for (int i = 0; i < hologram.size(); i++) {
                String holoText = hologram.getLine(i).toString();
                String newText = displayText.get(i);

                if (!holoText.equals(newText)) {
                    hologram.getLine(i).removeLine();
                    hologram.insertTextLine(i, newText.replace("%rank%", rank)
                            .replace("%faction%", factionTag)
                            .replace("%leader%", leader)
                            .replace("%totalpoints%", totalPoints));
                }
            }
        }
    }
}
