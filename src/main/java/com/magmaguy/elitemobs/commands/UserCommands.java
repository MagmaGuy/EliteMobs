package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.adventurersguild.GuildRankMenuHandler;
import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.commands.admin.CheckTierOthersCommand;
import com.magmaguy.elitemobs.commands.combat.CheckTierCommand;
import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.commands.quest.QuestCommand;
import com.magmaguy.elitemobs.commands.quest.QuestStatusCommand;
import com.magmaguy.elitemobs.commands.shops.CustomShopMenu;
import com.magmaguy.elitemobs.commands.shops.ProceduralShopMenu;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UserCommands {

    public static boolean parseUserCommand(Player player, String[] args) {

        switch (args[0]) {

            case "help":
                CommandHandler.validCommands(player);
                return true;

            case "ag":
            case "adventurersguild":
            case "adventurerguild":
                new AdventurersGuildCommand(player);
                return true;

            case "showitem":
            case "itemshow":
            case "shareitem":
            case "itemshare":
            case "share":
                ShareItem.showOnChat(player);
                return true;

            //shops
            case "shop":
            case "store":
                if (CommandHandler.userPermCheck("elitemobs.shop.command", player))
                    if (!AdventurersGuildCommand.adventurersGuildTeleport(player))
                        ProceduralShopMenu.shopInitializer(player);
                return true;

            case "customshop":
            case "cshop":
            case "customstore":
            case "cstore":
                if (CommandHandler.userPermCheck("elitemobs.customshop.command", player))
                    if (!AdventurersGuildCommand.adventurersGuildTeleport(player))
                        CustomShopMenu.customShopInitializer(player);
                return true;

            //economy
            case "wallet":
            case "bal":
            case "balance":
            case "currency":
            case "money":
            case "$":
                if (CommandHandler.userPermCheck(CommandHandler.CURRENCY_WALLET, player))
                    if (DefaultConfig.otherCommandsLeadToEMStatusMenu)
                        new PlayerStatusScreen(player);
                    else
                        CurrencyCommandsHandler.walletCommand(player, args);
                return true;

            case "quest":
                if (CommandHandler.userPermCheck(CommandHandler.QUEST, player)) {
                    if (args.length == 1) {
                        if (!AdventurersGuildCommand.adventurersGuildTeleport(player))
                            new QuestCommand(player);
                    } else new QuestStatusCommand(player, args);
                }
                return true;

            case "check":
            case "checkcurrency":
            case "checkbal":
            case "checkbalance":
            case "check$":
                if (CommandHandler.permCheck(CommandHandler.CURRENCY_CHECK, player))
                    if (DefaultConfig.otherCommandsLeadToEMStatusMenu)
                        new PlayerStatusScreen(player);
                    else
                        CurrencyCommandsHandler.checkCommand(player, args);
                return true;

            case "pay":
                if (CommandHandler.userPermCheck(CommandHandler.CURRENCY_PAY, player))
                    CurrencyCommandsHandler.payCommand(player, args);
                return true;

            //for servers who want to bypass the /ag teleportation restriction or menu restriction
            case "guild":
            case "rank":
            case "guildrank":
                if (!CommandHandler.userPermCheck("elitemobs.guild.menu", player)) return true;
                if (AdventurersGuildCommand.adventurersGuildTeleport(player)) {
                    new AdventurersGuildCommand(player);
                    return true;
                }
                GuildRankMenuHandler guildRankMenuHandler = new GuildRankMenuHandler();
                GuildRankMenuHandler.initializeGuildRankMenu(player);
                return true;

            //bypass for lack of /em, may be useless
            case "status":
                new PlayerStatusScreen(player);
                return true;

            //tier check
            case "checktier":
            case "tiercheck":
            case "tier":
                if (args.length == 1) {
                    if (CommandHandler.userPermCheck(CommandHandler.CHECK_TIER, player))
                        new CheckTierCommand(player);
                } else if (CommandHandler.permCheck(CommandHandler.CHECK_TIER_OTHERS, player))
                    new CheckTierOthersCommand(player, args);
                return true;

            //track bosses
            case "trackcustomboss":
                try {
                    CustomBossEntity.getCustomBoss(UUID.fromString(args[2])).realTimeTracking(player);
                } catch (Exception ex) {
                    //happens when players try to track an entity that has despawned for any reason
                    player.sendMessage("[EliteMobs] Sorry, this boss is already gone!");
                }
                return true;

            case "updateitem":
                new EliteItemLore(player.getItemInHand(), false);
                new WarningMessage("Updated item!");
                return true;
            case "dungeontp":
                if (CommandHandler.userPermCheck("elitemobs.dungeontp", player)) {
                    Minidungeon minidungeon = Minidungeon.minidungeons.get(args[1]);
                    if (minidungeon != null)
                        if (minidungeon.dungeonPackagerConfigFields.getDungeonLocationType().equals(DungeonPackagerConfigFields.DungeonLocationType.SCHEMATIC))
                            PlayerPreTeleportEvent.teleportPlayer(player, minidungeon.teleportLocation);
                        else
                            PlayerPreTeleportEvent.teleportPlayer(player,
                                    new Location(minidungeon.teleportLocation.getWorld(),
                                            minidungeon.teleportLocation.getX(),
                                            minidungeon.teleportLocation.getY(),
                                            minidungeon.teleportLocation.getZ(),
                                            Float.parseFloat("" + minidungeon.dungeonPackagerConfigFields.getTeleportPointPitch()),
                                            Float.parseFloat("" + minidungeon.dungeonPackagerConfigFields.getTeleportPointYaw())));
                    else
                        player.sendMessage("[EliteMobs] That dungeon isn't valid!");
                }
                return true;
            case "spawntp":
                if (CommandHandler.userPermCheck("elitemobs.spawntp", player))
                    if (DefaultConfig.defaultSpawnLocation != null)
                        PlayerPreTeleportEvent.teleportPlayer(player, DefaultConfig.defaultSpawnLocation);
                return true;
            default:
                return false;
        }

    }

}
