package com.magmaguy.elitemobs.commands.guild;

import com.magmaguy.elitemobs.adventurersguild.GuildRankMenuHandler;
import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class AdventurersGuildCommand {

    public AdventurersGuildCommand(Player player) {

        if (adventurersGuildTeleport(player)) return;

        if (!player.hasPermission("elitemobs.guild.menu")) return;
        GuildRankMenuHandler guildRankMenuHandler = new GuildRankMenuHandler();
        GuildRankMenuHandler.initializeGuildRankMenu(player);

    }

    public static boolean adventurersGuildTeleport(Player player) {

        if (!AdventurersGuildConfig.agTeleport) return false;
        if (!AdventurersGuildConfig.alwaysUseNpcs) return false;
        if (AdventurersGuildConfig.guildWorldLocation == null)
            defineTeleportLocation();
        if (AdventurersGuildConfig.guildWorldLocation == null) return false;

        if (CombatTagConfig.enableCombatTag)
            new EventCaller(new PlayerPreTeleportEvent(player, AdventurersGuildConfig.guildWorldLocation));
        else
            new EventCaller(new PlayerTeleportEvent(player, AdventurersGuildConfig.guildWorldLocation));

        return true;

    }

    public static Location defineTeleportLocation() {

        for (World world : Bukkit.getWorlds())
            if (world.getName().equals(AdventurersGuildConfig.guildWorldName)) {
                double x = 0, y = 0, z = 0;
                float yaw = 0, pitch = 0;
                int counter = 0;

                for (String substring : AdventurersGuildConfig.guildLocationString.split(",")) {
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

                return AdventurersGuildConfig.guildWorldLocation = new Location(world, x, y, z, yaw, pitch);

            }

        return null;

    }

}
