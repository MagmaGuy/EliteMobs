/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.scoreboard;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by MagmaGuy on 03/05/2017.
 */
public class ScoreboardHandler implements Listener {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    static HashMap playerHasScoreboard = new HashMap<Player, Boolean>();
    int processID;

    public void scanSight () {

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (player.hasPermission("elitemobs.scoreboard") && playerHasScoreboard.containsKey(player) &&
                    (Boolean) playerHasScoreboard.get(player) == false || !playerHasScoreboard.containsKey(player) &&
                    player.hasPermission("elitemobs.scoreboard")) {

                Location playerLocation = player.getLocation();

                Collection<Entity> nearbyEntities = playerLocation.getWorld().getNearbyEntities(playerLocation, 7, 7, 7);

                for (Entity entity : nearbyEntities) {

                    if (entity.hasMetadata(MetadataHandler.ELITE_MOB_MD)) {

                        aggressiveEliteMobScoreboard(entity, player);
                        return;

                    } else if (entity.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD)) {

                        aggressiveEliteMobScoreboard(entity, player);
                        return;

                    }

                }

            }

        }

    }

    public void aggressiveEliteMobScoreboard (Entity entity, Player player) {

        processID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            int iterator = 0;

            public void run() {

                if (!entity.isValid() || player.getWorld() != entity.getWorld() ||
                        player.getLocation().distance(entity.getLocation()) > 7) {

                    player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                    playerHasScoreboard.put(player, false);
                    Bukkit.getScheduler().cancelTask(processID);
                    return;

                }

                MetadataHandler metadataHandler = new MetadataHandler();

                int powerCounter = 0;

                for (String string : MetadataHandler.allPowersList()) {

                    if (entity.hasMetadata(string)) {

                        powerCounter++;

                    }

                }

                ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
                Scoreboard board = scoreboardManager.getNewScoreboard();
                Objective objective = board.registerNewObjective("test", "dummy");

                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                objective.setDisplayName(entity.getName());


                int currentHealth = (int) ((LivingEntity) entity).getHealth();
                int maxHealth = (int) ((LivingEntity) entity).getMaxHealth();

                int counter = 0;

                for (String string : MetadataHandler.allPowersList()) {

                    if (entity.hasMetadata(string)) {

                        String finalString = metadataHandler.machineToHumanTranslator(string);

                        if (powerCounter > 13) {

                            Score score = null;

                            if (MetadataHandler.minorPowerList().contains(string)){

                                score = objective.getScore(ChatColor.AQUA + finalString);

                            } else if (MetadataHandler.majorPowerList().contains(string)) {

                                score = objective.getScore(ChatColor.DARK_AQUA + finalString);

                            }

                            if (counter + iterator < powerCounter) {

                                score.setScore(counter + iterator);

                            } else {

                                int currentValue = (int) ((counter + iterator) - Math.floor(((counter + iterator) / powerCounter) * powerCounter));

                                score.setScore(currentValue);

                            }


                        } else {

                            Score score;

                            if (MetadataHandler.minorPowerList().contains(string)){

                                score = objective.getScore(ChatColor.AQUA + finalString);

                            } else {

                                score = objective.getScore(ChatColor.DARK_AQUA + finalString);

                            }

                            score.setScore(counter);

                        }

                        counter++;

                    }

                }

                player.setScoreboard(board);
                playerHasScoreboard.put(player, true);

                Score healthScore = objective.getScore(String.format("%s%s", ChatColor.DARK_RED, ChatColor.BOLD) +
                        ConfigValues.translationConfig.getString("ScoreBoard.Health") + ChatColor.RED + currentHealth
                        + ChatColor.DARK_RED + "/" + ChatColor.RED + maxHealth);
                healthScore.setScore(powerCounter);

                iterator++;

            }

        }, 4, 4);

    }

}
