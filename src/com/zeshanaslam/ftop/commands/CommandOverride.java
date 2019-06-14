package com.zeshanaslam.ftop.commands;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Factions;
import com.zeshanaslam.ftop.FTopStats;
import com.zeshanaslam.ftop.Main;
import com.zeshanaslam.ftop.config.ConfigStore;
import com.zeshanaslam.ftop.config.challenges.FactionData;
import com.zeshanaslam.ftop.config.challenges.Objective;
import com.zeshanaslam.ftop.config.challenges.SectionObjectives;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandOverride implements Listener {

    private final Main main;

    public CommandOverride(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onChallenges(PlayerCommandPreprocessEvent event) {
        Player sender = event.getPlayer();

        if (event.getMessage().equalsIgnoreCase("/f challenges") && sender.hasPermission("ftop.challenges")) {
            event.setCancelled(true);

            FPlayer fPlayer = FPlayers.getInstance().getByPlayer(sender);
            if (!fPlayer.hasFaction()) {
                sender.sendMessage(main.configStore.messages.get(ConfigStore.Messages.NoFaction));
                return;
            }

            FactionData factionData = main.configStore.challengesStore.getFactionData(sender);
            SectionObjectives daily = main.configStore.challengesStore.daily;
            SectionObjectives weekly = main.configStore.challengesStore.weekly;

            for (String text : main.configStore.challengesStore.text) {
                if (text.equalsIgnoreCase("%dailyobjectives%")) {
                    if (!factionData.dailyCompleted) {
                        for (Objective objective : daily.objectives.values()) {
                            float percent = ((float) factionData.getDailyValue(objective.type) / (float) objective.amount) * 100;
                            int roundedPercent = (int) percent;

                            String output = ChatColor.translateAlternateColorCodes('&', objective.text)
                                    .replace("%amount%", String.valueOf(objective.amount))
                                    .replace("%complete%", roundedPercent + "%");

                            if (output.contains("%type%") && objective.type.contains("-")) {
                                output = output.replace("%type%", objective.type.split("-")[1].replace("_", " ").toLowerCase());
                            }

                            sender.sendMessage(output);
                        }
                    } else {
                        for (String completed : main.configStore.challengesStore.completed) {
                            completed = completed.replace("%type%", "daily");
                            sender.sendMessage(completed);
                        }
                    }
                } else if (text.equalsIgnoreCase("%weeklyobjectives%")) {
                    if (!factionData.weeklyCompleted) {
                        for (Objective objective : weekly.objectives.values()) {
                            float percent = ((float) factionData.getWeeklyValue(objective.type) / (float) objective.amount) * 100;
                            int roundedPercent = (int) percent;

                            String output = ChatColor.translateAlternateColorCodes('&', objective.text)
                                    .replace("%amount%", String.valueOf(objective.amount))
                                    .replace("%complete%", roundedPercent + "%");

                            if (output.contains("%type%") && objective.type.contains("-")) {
                                output = output.replace("%type%", objective.type.split("-")[1].replace("_", " ").toLowerCase());
                            }

                            sender.sendMessage(output);
                        }
                    } else {
                        for (String completed : main.configStore.challengesStore.completed) {
                            completed = completed.replace("%type%", "weekly");
                            sender.sendMessage(completed);
                        }
                    }
                } else {
                    sender.sendMessage(text);
                }
            }
        }
    }

    @EventHandler
    public void onFTop(PlayerCommandPreprocessEvent event) {
        Player sender = event.getPlayer();

        if ((event.getMessage().equalsIgnoreCase("/ftop") || event.getMessage().startsWith("/ftop ")
            || event.getMessage().equalsIgnoreCase("/f top") || event.getMessage().startsWith("/f top "))
                && sender.hasPermission("ftop.ftop")) {
            event.setCancelled(true);
            int page = 0;

            String[] args = event.getMessage().split(" ");
            if (!event.getMessage().contains(" ") || args.length < 1 || !main.configStore.isNumeric(args[1]))
                page = 1;
            else
                page = Integer.parseInt(args[1]);

            if (main.fTopUtils.stats == null) {
                sender.sendMessage(ChatColor.RED + "Currently calculating faction worth...");
                return;
            }

            List<FTopStats> fTopStatsList = new ArrayList<>(main.fTopUtils.stats);

            int totalPages = fTopStatsList.size() / 10;
            if (totalPages == 0)
                totalPages = 1;

            if (page > totalPages) {
                sender.sendMessage(main.configStore.messages.get(ConfigStore.Messages.NoMorePages));
                return;
            }

            int load = page * 10;
            for (String text : main.configStore.topText) {
                if (text.contains("%rank%")) {
                    for (int i = (load - 10); i < load; i++) {
                        if (fTopStatsList.size() > i) {
                            FTopStats fTopStats = fTopStatsList.get(i);

                            String output = text.replace("%rank%", String.valueOf(i + 1))
                                    .replace("%factionname%", Factions.getInstance().getFactionById(fTopStats.factionId).getTag())
                                    .replace("%points%", String.valueOf(Math.round(fTopStats.totalPoints * 100.0) / 100.0));

                            String hover = main.fTopUtils.getHoverText(fTopStats);

                            TextComponent textComponent = new TextComponent(output);
                            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));

                            sender.spigot().sendMessage(textComponent);
                        }
                    }
                } else {
                    text = text.replace("%page%", String.valueOf(page))
                            .replace("%totalpages%", String.valueOf(totalPages));

                    if (text.contains("%totalpoints%")) {
                        text = text.replace("%totalpoints%", String.valueOf(Math.round(main.fTopUtils.getServerTotal() * 100.0) / 100.0));
                    }
                    sender.sendMessage(text);
                }
            }
        }
    }
}
