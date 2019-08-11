package com.magmaguy.elitemobs.commands.guild;

import com.magmaguy.elitemobs.adventurersguild.GuildRankMenuHandler;
import com.magmaguy.elitemobs.combattag.TeleportTag;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import org.bukkit.entity.Player;

public class AdventurersGuildCommand {

    public AdventurersGuildCommand(Player player) {

        if (adventurersGuildTeleport(player)) return;

        if (!player.hasPermission("elitemobs.guild.menu")) return;
        GuildRankMenuHandler.initializeGuildRankMenu(player);

    }

    public static boolean adventurersGuildTeleport(Player player) {

        if (!AdventurersGuildConfig.agTeleport) return false;
        if (!AdventurersGuildConfig.alwaysUseNpcs) return false;
        if (AdventurersGuildConfig.guildWorldLocation == null) return false;

        if (CombatTagConfig.enableCombatTag)
            TeleportTag.initializePlayerTeleport(player, CombatTagConfig.teleportTimerDuration, AdventurersGuildConfig.guildWorldLocation);
        else
            player.teleport(AdventurersGuildConfig.guildWorldLocation);

        return true;

    }

}
