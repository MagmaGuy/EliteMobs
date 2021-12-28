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

    public static void adventurersGuildCommand(Player player) {
        if (adventurersGuildTeleport(player)) return;
        if (player.hasPermission("elitemobs.adventurersguild.command"))
            GuildRankMenuHandler.initializeGuildRankMenu(player);
        else
            player.sendMessage("Missing permission: elitemobs.adventurersguild.command / elitemobs.adventurersguild.teleport");
    }

    public static boolean adventurersGuildTeleport(Player player) {
        if (!player.hasPermission("elitemobs.adventurersguild.teleport")) return false;
        if (!AdventurersGuildConfig.isAgTeleport()) return false;
        if (AdventurersGuildConfig.getGuildWorldLocation() == null)
            defineTeleportLocation();
        if (AdventurersGuildConfig.getGuildWorldLocation() == null) return false;

        if (CombatTagConfig.isEnableCombatTag())
            new EventCaller(new PlayerPreTeleportEvent(player, AdventurersGuildConfig.getGuildWorldLocation()));
        else
            new EventCaller(new PlayerTeleportEvent(player, AdventurersGuildConfig.getGuildWorldLocation()));

        return true;

    }

    public static Location defineTeleportLocation() {

        for (World world : Bukkit.getWorlds())
            if (world.getName().equals(AdventurersGuildConfig.getGuildWorldName())) {
                double x = 0, y = 0, z = 0;
                float yaw = 0, pitch = 0;
                int counter = 0;

                for (String substring : AdventurersGuildConfig.getGuildLocationString().split(",")) {
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

                Location location = new Location(world, x, y, z, yaw, pitch);
                AdventurersGuildConfig.setGuildWorldLocation(location);
                return location;

            }

        return null;

    }

}
