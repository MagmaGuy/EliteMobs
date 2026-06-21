package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.dungeons.DungeonBossLockout;
import com.magmaguy.elitemobs.dungeons.DynamicDungeonPackage;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.WorldInstancedDungeonPackage;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.menus.DynamicDungeonBrowser;
import com.magmaguy.elitemobs.menus.InstancedDungeonBrowser;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreenDialog;
import com.magmaguy.elitemobs.playerdata.statusscreen.TeleportsPage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DungeonCommands {
    public static void teleport(Player player, String minidungeonName) {
        teleport(player, minidungeonName, TeleportMenuSource.NONE);
    }

    public static void teleport(Player player, String minidungeonName, TeleportMenuSource teleportMenuSource) {
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
            new DynamicDungeonBrowser(player, emPackage.getContentPackagesConfigFields().getFilename(), teleportMenuSource);
        else if (emPackage instanceof WorldInstancedDungeonPackage)
            new InstancedDungeonBrowser(player, emPackage.getContentPackagesConfigFields().getFilename(), teleportMenuSource);
        else {
            if (emPackage.getContentPackagesConfigFields().getTeleportLocation() != null) {
                PlayerPreTeleportEvent.teleportPlayer(player, emPackage.getContentPackagesConfigFields().getTeleportLocation());
            }
            else
                player.sendMessage(CommandMessagesConfig.getDungeonTeleportNotSetMessage());
        }
    }

    /**
     * Clears every dungeon boss lockout for a target player. Lockouts are stored as absolute timestamps,
     * so players can otherwise stay locked out after an admin shortens (or removes) a dungeon's lockout
     * duration. This frees them manually.
     */
    public static void resetLockout(CommandSender commandSender, String playerString) {
        Player player = Bukkit.getPlayer(playerString);
        if (player == null) {
            commandSender.sendMessage(CommandMessagesConfig.getDungeonLockoutPlayerNotValidMessage());
            return;
        }
        DungeonBossLockout lockout = PlayerData.getDungeonBossLockout(player.getUniqueId());
        if (lockout == null) lockout = new DungeonBossLockout();
        int cleared = lockout.clearLockouts();
        PlayerData.updateDungeonBossLockout(player.getUniqueId(), lockout);
        commandSender.sendMessage(CommandMessagesConfig.getDungeonLockoutResetSuccessMessage()
                .replace("$player", player.getName())
                .replace("$amount", cleared + ""));
    }

    public static void reopenTeleportBrowser(Player player, TeleportMenuSource teleportMenuSource) {
        switch (teleportMenuSource) {
            case INVENTORY -> TeleportsPage.showTeleportInventory(player);
            case DIALOGUE -> PlayerStatusScreenDialog.showTeleportsDialog(player);
            case NONE -> {
            }
        }
    }

    public enum TeleportMenuSource {
        NONE,
        INVENTORY,
        DIALOGUE
    }
}
