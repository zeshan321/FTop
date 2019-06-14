package com.zeshanaslam.ftop;

import com.zeshanaslam.ftop.config.challenges.ChallengesStore;
import com.zeshanaslam.ftop.config.challenges.FactionData;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class ChallengesTask extends BukkitRunnable {

    private final Main main;

    public ChallengesTask(Main main) {
        this.main = main;
    }

    @Override
    public void run() {
        ChallengesStore challengesStore = main.configStore.challengesStore;
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime dailyStart = LocalDateTime.ofInstant(Instant.ofEpochMilli(challengesStore.dailyStart), TimeZone.getDefault().toZoneId());
        if (now.getHour() == 0 && now.getMinute() == 0) {
            for (String factionId : challengesStore.factionData.keySet()) {
                FactionData factionData = challengesStore.factionData.get(factionId);
                factionData.dailyObjectiveValues.clear();
                factionData.setDailyStatus(false);

                challengesStore.factionData.put(factionId, factionData);
            }

            challengesStore.dailyStart = System.currentTimeMillis();
            challengesStore.daily = challengesStore.generateObjectives(challengesStore.dailyObjectives, challengesStore.dailyMin, challengesStore.dailyMax);
        }

        LocalDateTime weeklyStart = LocalDateTime.ofInstant(Instant.ofEpochMilli(challengesStore.weeklyStart), TimeZone.getDefault().toZoneId());
        if (now.getDayOfWeek() == DayOfWeek.FRIDAY && now.getHour() == 0 && now.getMinute() == 0) {
            for (String factionId : challengesStore.factionData.keySet()) {
                FactionData factionData = challengesStore.factionData.get(factionId);
                factionData.weeklyObjectiveValues.clear();
                factionData.setWeeklyStatus(false);

                challengesStore.factionData.put(factionId, factionData);
            }

            challengesStore.weeklyStart = System.currentTimeMillis();
            challengesStore.weekly = challengesStore.generateObjectives(challengesStore.weeklyObjectives, challengesStore.weeklyMin, challengesStore.weeklyMax);
        }
    }

    private Duration getDuration(LocalDateTime current, LocalDateTime later) {
        return Duration.between(current, later);
    }
}
