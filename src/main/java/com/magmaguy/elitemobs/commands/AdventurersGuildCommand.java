package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.adventurersguild.AdventurersGuildGUI;
import com.magmaguy.elitemobs.combattag.TeleportTag;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.config.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class AdventurersGuildCommand {

    public AdventurersGuildCommand(Player player) {

        if (ConfigValues.adventurersGuildConfig.getBoolean(AdventurersGuildConfig.AG_TELEPORT))
            for (World world : Bukkit.getWorlds())
                if (world.getName().equals(ConfigValues.adventurersGuildConfig.getString(AdventurersGuildConfig.GUILD_WORLD_NAME))) {

                    if (!player.hasPermission("elitemobs.adventurersguild.teleport")) return;

                    Location location;
                    double x = 0, y = 0, z = 0;
                    float yaw = 0, pitch = 0;
                    int counter = 0;
                    for (String substring : ConfigValues.adventurersGuildConfig.getString(AdventurersGuildConfig.GUILD_WORLD_LOCATION).split(",")) {
                        switch (counter) {
                            case 0:
                                x = Double.parseDouble(substring);
                                break;
                            case 1:
                                y = Double.parseDouble(substring);
                                break;
                            case 2:
                                z = Double.parseDouble(substring);
                                break;
                            case 3:
                                yaw = Float.parseFloat(substring);
                                break;
                            case 4:
                                pitch = Float.parseFloat(substring);
                                break;
                        }
                        counter++;
                    }
                    location = new Location(world, x, y, z, yaw, pitch);
                    if (ConfigValues.combatTagConfig.getBoolean(CombatTagConfig.ENABLE_COMBAT_TAG))
                        TeleportTag.intializePlayerTeleport(player, ConfigValues.combatTagConfig.getInt(CombatTagConfig.TELEPORT_TIMER_DURATION), location);
                    else
                        player.teleport(location);
                    return;
                }

        if (!player.hasPermission("elitemobs.adventurersguild.menu")) return;
        AdventurersGuildGUI.mainMenu(player);

    }

}
