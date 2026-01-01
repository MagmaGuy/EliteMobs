package com.magmaguy.elitemobs.commands.guild;

import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.menus.SkillBonusMenu;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.entity.Player;

public class AdventurersGuildCommand {

    public static void adventurersGuildCommand(Player player) {
        if (adventurersGuildTeleport(player)) return;
        if (player.hasPermission("elitemobs.adventurersguild.command"))
            SkillBonusMenu.openWeaponSelectMenu(player);
        else
            player.sendMessage("Missing permission: elitemobs.adventurersguild.command / elitemobs.adventurersguild.teleport");
    }

    public static boolean adventurersGuildTeleport(Player player) {
        if (!player.hasPermission("elitemobs.adventurersguild.teleport")) return false;
        if (!AdventurersGuildConfig.isAgTeleport()) return false;

        EMPackage emPackage = EMPackage.getEmPackages().get("adventurers_guild_hub.yml");
        if (!emPackage.isInstalled()) return false;

        if (CombatTagConfig.isEnableCombatTag())
            new EventCaller(new PlayerPreTeleportEvent(player, emPackage.getContentPackagesConfigFields().getTeleportLocation()));
        else
            new EventCaller(new PlayerTeleportEvent(player, emPackage.getContentPackagesConfigFields().getTeleportLocation()));

        return true;
    }

}
