package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.dungeons.DynamicDungeonPackage;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.WorldInstancedDungeonPackage;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.menus.DynamicDungeonBrowser;
import com.magmaguy.elitemobs.menus.InstancedDungeonBrowser;
import org.bukkit.entity.Player;

public class DungeonCommands {
    public static void teleport(Player player, String minidungeonName) {
        EMPackage emPackage = EMPackage.getEmPackages().get(minidungeonName);
        if (emPackage == null) {
            player.sendMessage(CommandMessagesConfig.getDungeonNotValidMessage());
            return;
        } else if (!emPackage.isInstalled()) {
            player.sendMessage(CommandMessagesConfig.getDungeonNotInstalledMessage());
            return;
        }
        if (MatchInstance.getAnyPlayerInstance(player) != null) {
            player.sendMessage(CommandMessagesConfig.getAlreadyInInstanceMessage());
            return;
        }
        if (emPackage instanceof DynamicDungeonPackage)
            new DynamicDungeonBrowser(player, emPackage.getContentPackagesConfigFields().getFilename());
        else if (emPackage instanceof WorldInstancedDungeonPackage)
            new InstancedDungeonBrowser(player, emPackage.getContentPackagesConfigFields().getFilename());
        else {
            if (emPackage.getContentPackagesConfigFields().getTeleportLocation() != null) {
                PlayerPreTeleportEvent.teleportPlayer(player, emPackage.getContentPackagesConfigFields().getTeleportLocation());
            }
            else
                player.sendMessage(CommandMessagesConfig.getDungeonTeleportNotSetMessage());
        }
    }
}
