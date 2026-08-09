package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.dungeons.DungeonBossLockout;
import com.magmaguy.elitemobs.dungeons.DynamicDungeonPackage;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.WorldDungeonPackage;
import com.magmaguy.elitemobs.dungeons.WorldInstancedDungeonPackage;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.menus.DynamicDungeonBrowser;
import com.magmaguy.elitemobs.menus.InstancedDungeonBrowser;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreenDialog;
import com.magmaguy.elitemobs.playerdata.statusscreen.TeleportsPage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                List<Player> enteringPlayers = emPackage instanceof WorldDungeonPackage
                        ? resolveOpenDungeonParty(player, emPackage)
                        : List.of(player);
                if (!enteringPlayers.isEmpty()
                        && !PlayerPreTeleportEvent.teleportPlayers(
                                enteringPlayers,
                                emPackage.getContentPackagesConfigFields().getTeleportLocation())
                        && enteringPlayers.size() > 1)
                    PartyManager.sendConfiguredMessage(player, PartyConfig.getDungeonPartyJoinFailedMessage());
            }
            else
                player.sendMessage(CommandMessagesConfig.getDungeonTeleportNotSetMessage());
        }
    }

    /**
     * Open-world dungeons do not have a match object to admit players into. Resolve the same
     * current, online party snapshot before starting each member's ordinary safe-teleport timer.
     */
    private static List<Player> resolveOpenDungeonParty(Player initiator, EMPackage emPackage) {
        List<UUID> memberIds = PartyManager.getDungeonEntryMemberIds(initiator);
        if (!PartyManager.isDungeonEntryRosterCurrent(initiator, memberIds)) {
            PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyChangedMessage());
            return List.of();
        }

        List<Player> members = new ArrayList<>();
        for (UUID memberId : memberIds) {
            Player member = Bukkit.getPlayer(memberId);
            String memberName = member == null ? Bukkit.getOfflinePlayer(memberId).getName() : member.getName();
            if (member == null || !member.isOnline() || !member.isValid() || !PlayerData.isInMemory(memberId)) {
                PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyUnavailableMessage()
                        .replace("$player", memberName == null ? "Unknown" : memberName));
                return List.of();
            }
            if (PlayerData.getMatchInstance(member) != null || MatchInstance.getAnyPlayerInstance(member) != null) {
                PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyInInstanceMessage()
                        .replace("$player", member.getName()));
                return List.of();
            }
            String permission = emPackage.getContentPackagesConfigFields().getPermission();
            if (permission != null && !permission.isEmpty() && !member.hasPermission(permission)) {
                PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyNoPermissionMessage()
                        .replace("$player", member.getName()));
                return List.of();
            }
            members.add(member);
        }
        return List.copyOf(members);
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
