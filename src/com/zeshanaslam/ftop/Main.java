package com.zeshanaslam.ftop;

import app.ashcon.intake.bukkit.BukkitIntake;
import app.ashcon.intake.bukkit.graph.BasicBukkitCommandGraph;
import app.ashcon.intake.fluent.DispatcherNode;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.collect.Lists;
import com.zeshanaslam.ftop.commands.CommandOverride;
import com.zeshanaslam.ftop.commands.FTopCommands;
import com.zeshanaslam.ftop.config.ConfigStore;
import com.zeshanaslam.ftop.database.DBContext;
import com.zeshanaslam.ftop.database.handlers.LocationData;
import com.zeshanaslam.ftop.listeners.*;
import com.zeshanaslam.ftop.utils.FTopExpansion;
import com.zeshanaslam.ftop.utils.FTopUtils;
import com.zeshanaslam.ftop.utils.WrapperPlayServerPlayerInfo;
import de.dustplanet.util.SilkUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main extends JavaPlugin {

    public static Economy economy;
    public ConfigStore configStore;
    public DBContext dbContext;
    public FTopUtils fTopUtils;
    public BukkitTask fTopTask;
    public BukkitTask challengesTask;
    public boolean hookedIntoHealthBar;
    public SilkUtil silkUtil;
    public ThreadPoolExecutor executor;

    @Override
    public void onEnable() {
        super.onEnable();

        // Utils
        fTopUtils = new FTopUtils(this);

        // Config
        saveDefaultConfig();
        configStore = new ConfigStore(this);

        // Thread executor
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(configStore.maxThreads);

        // Database
        try {
            dbContext = new DBContext(this);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        // Register intake commands
        BasicBukkitCommandGraph basicBukkitCommandGraph = new BasicBukkitCommandGraph();
        DispatcherNode dispatcherNode = basicBukkitCommandGraph.getRootDispatcherNode().registerNode("ftopadmin");
        dispatcherNode.registerCommands(new FTopCommands(this));

        BukkitIntake bukkitIntake = new BukkitIntake(this, basicBukkitCommandGraph);
        bukkitIntake.register();

        // Register listeners
        getServer().getPluginManager().registerEvents(new BlockListeners(this), this);
        getServer().getPluginManager().registerEvents(new BaseObjectiveListeners(this), this);
        getServer().getPluginManager().registerEvents(new CommandOverride(this), this);

        // Hook into vault
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }

        // Hook into mob arena
        setupMobArena();

        // Hook into health bar
        setupHealthBar();

        // Hook into silk spawners
        setupSilkSpawners();

        // Hook into supply drops
        setupSupplyDrop();

        // PlaceholderAPI
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new FTopExpansion(this).register();
        }

        // Stats refreshing
        fTopTask = new FTopTask(this).runTaskTimer(this, 0, configStore.refresh * 20);

        // Start daily/weekly challenges rotation
        challengesTask = new ChallengesTask(this).runTaskTimer(this, 0, 20);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.PLAYER_INFO) {

            @Override
            public void onPacketSending(PacketEvent event) {
                WrapperPlayServerPlayerInfo wrapper = new WrapperPlayServerPlayerInfo(event.getPacket());

                List<PlayerInfoData> playerInfoDataList = wrapper.getData();

                if (wrapper.getAction() != EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
                    return;
                }

                List<PlayerInfoData> newPlayerInfoDataList = Lists.newArrayList();

                for (PlayerInfoData playerInfoData : playerInfoDataList) {
                    if (playerInfoData == null || playerInfoData.getProfile() == null) {
                        newPlayerInfoDataList.add(playerInfoData);
                        continue;
                    }

                    UUID uuid = playerInfoData.getProfile().getUUID();
                    if (!configStore.npcStore.names.containsKey(uuid)) {
                        newPlayerInfoDataList.add(playerInfoData);
                        continue;
                    }

                    WrappedGameProfile profile = playerInfoData.getProfile();
                    WrappedGameProfile newProfile = profile.withName(configStore.npcStore.names.get(uuid));
                    newProfile.getProperties().putAll(profile.getProperties());

                    PlayerInfoData newPlayerInfoData = new PlayerInfoData(newProfile, playerInfoData.getPing(), playerInfoData.getGameMode(), playerInfoData.getDisplayName());
                    newPlayerInfoDataList.add(newPlayerInfoData);
                }

                wrapper.setData(newPlayerInfoDataList);
            }
        });

        //test();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        executor.shutdown();
        configStore.save();
        fTopTask.cancel();
        challengesTask.cancel();
    }

    private void setupMobArena() {
        Plugin plugin = getServer().getPluginManager().getPlugin("MobArena");
        if (plugin == null) {
            return;
        }

        getServer().getPluginManager().registerEvents(new MobArena(this), this);
    }

    private void setupSilkSpawners() {
        Plugin plugin = getServer().getPluginManager().getPlugin("SilkSpawners");
        if (plugin == null) {
            return;
        }

        silkUtil = SilkUtil.hookIntoSilkSpanwers();
        //getServer().getPluginManager().registerEvents(new SilkSpawnersListener(this), this);
    }

    private void setupSupplyDrop() {
        Plugin plugin = getServer().getPluginManager().getPlugin("SupplyDrop");
        if (plugin == null) {
            return;
        }

        getServer().getPluginManager().registerEvents(new SupplyDropListener(this), this);
    }

    private void setupHealthBar() {
        Plugin plugin = getServer().getPluginManager().getPlugin("HealthBar");

        hookedIntoHealthBar = plugin != null;
    }

    private void test() {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "ftopadmin purge");
        List<LocationData> locationDataList = dbContext.getBlockTable().getAll();

        int removed = 0;
        for (LocationData locationData: locationDataList) {
            String found = fTopUtils.getMaterialName(locationData.location.getLocation().getBlock());

            if (!found.equals(locationData.material)) {
                removed++;
                dbContext.getBlockTable().removeBlock(locationData.location.world, locationData.location.x, locationData.location.y, locationData.location.z);
                System.out.println(ChatColor.GREEN + "Expected " + locationData.material + " found " + found +  " removed @ " + locationData.location.x + " " + locationData.location.y + " " + locationData.location.z + ". Current amount removed: " + removed);
            }
        }
    }
}
