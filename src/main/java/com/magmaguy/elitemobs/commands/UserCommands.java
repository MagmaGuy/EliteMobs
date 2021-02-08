package com.magmaguy.elitemobs.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.commands.admin.CheckTierOthersCommand;
import com.magmaguy.elitemobs.commands.combat.CheckTierCommand;
import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.commands.quest.QuestCommand;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.menus.CustomShopMenu;
import com.magmaguy.elitemobs.menus.ProceduralShopMenu;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import com.magmaguy.elitemobs.config.DefaultConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class UserCommands {

    public UserCommands(PaperCommandManager<CommandSender> manager, Command.Builder<CommandSender> builder, CommandConfirmationManager<CommandSender> commandConfirmationManager) {
        // /em adventurersguild
        manager.command(builder.literal("adventurersguild", "ag")
                .meta(CommandMeta.DESCRIPTION, "Teleports players to the Adventurer's Guild Hub or opens the Adventurer's Guild menu.")
                .senderType(Player.class)
                //permission is dealt inside of the command
                .handler(commandContext -> {
                    AdventurersGuildCommand.adventurersGuildCommand((Player) commandContext.getSender());
                }));

        // /em shareitem
        manager.command(builder.literal("shareitem")
                .meta(CommandMeta.DESCRIPTION, "Shares a held Elite item on chat")
                .senderType(Player.class)
                .permission("elitemobs.shareitem")
                .handler(commandContext -> ShareItem.showOnChat((Player) commandContext.getSender())));

        // /em shop
        manager.command(builder.literal("shop")
                .meta(CommandMeta.DESCRIPTION, "Opens the procedurally generated item shop or teleports the player to the Adventurer's Guild Hub")
                .senderType(Player.class)
                .permission("elitemobs.shop.command")
                .handler(commandContext -> {
                    if (!AdventurersGuildCommand.adventurersGuildTeleport((Player) commandContext.getSender()))
                        ProceduralShopMenu.shopInitializer((Player) commandContext.getSender());
                }));

        // /em customshop
        manager.command(builder.literal("customshop")
                .meta(CommandMeta.DESCRIPTION, "Opens the custom item shop or teleports the player to the Adventurer's Guild Hub")
                .senderType(Player.class)
                .permission("elitemobs.customshop.command")
                .handler(commandContext -> {
                    if (!AdventurersGuildCommand.adventurersGuildTeleport((Player) commandContext.getSender()))
                        CustomShopMenu.customShopInitializer((Player) commandContext.getSender());
                }));

        // /em wallet
        manager.command(builder.literal("wallet")
                .meta(CommandMeta.DESCRIPTION, "Checks the EliteMobs currency")
                .senderType(Player.class)
                .permission("elitemobs.currency.check")
                .handler(commandContext -> {
                    if (DefaultConfig.otherCommandsLeadToEMStatusMenu)
                        new PlayerStatusScreen((Player) commandContext.getSender());
                    else
                        CurrencyCommandsHandler.walletCommand((Player) commandContext.getSender());
                }));

        // /em quest
        manager.command(builder.literal("quest")
                .meta(CommandMeta.DESCRIPTION, "Checks the EliteMobs currency")
                .senderType(Player.class)
                .permission("elitemobs.quest")
                .handler(commandContext -> {
                    if (DefaultConfig.otherCommandsLeadToEMStatusMenu)
                        new PlayerStatusScreen((Player) commandContext.getSender());
                    else
                        new QuestCommand((Player) commandContext.getSender());
                }));

        CommandArgument<CommandSender, String> onlinePlayers = StringArgument.<CommandSender>newBuilder("onlinePlayer")
                .withSuggestionsProvider(((objectCommandContext, s) -> {
                    ArrayList<String> arrayList = new ArrayList<>();
                    Bukkit.getOnlinePlayers().forEach(player -> arrayList.add(player.getName()));
                    return arrayList;
                })).build();

        // /em pay <username> <amount>
        manager.command(builder.literal("pay")
                .argument(onlinePlayers.copy(), ArgumentDescription.of("Player name"))
                .argument(DoubleArgument.newBuilder("amount"), ArgumentDescription.of("Amount to pay"))
                .meta(CommandMeta.DESCRIPTION, "Pays an amount of currency to another player")
                .meta(CommandConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .senderType(Player.class)
                .permission("elitemobs.currency.pay")
                .handler(commandContext -> {
                    CurrencyCommandsHandler.payCommand(
                            (Player) commandContext.getSender(),
                            commandContext.get("onlinePlayer"),
                            commandContext.get("amount"));
                    commandConfirmationManager.createConfirmationExecutionHandler();
                }));


        // /em rank
        manager.command(builder.literal("rank")
                .meta(CommandMeta.DESCRIPTION, "Opens the EliteMobs rank menu.")
                .senderType(Player.class)
                .permission("elitemobs.rank.command")
                .handler(commandContext -> {
                    if (!AdventurersGuildCommand.adventurersGuildTeleport((Player) commandContext.getSender()))
                        AdventurersGuildCommand.adventurersGuildCommand((Player) commandContext.getSender());
                }));

        // /em menu
        manager.command(builder.literal("menu")
                .meta(CommandMeta.DESCRIPTION, "Opens the EliteMobs rank menu.")
                .senderType(Player.class)
                .handler(commandContext -> new PlayerStatusScreen((Player) commandContext.getSender())));

        // /em rank
        manager.command(builder.literal("checktier")
                .meta(CommandMeta.DESCRIPTION, "Checks your equipped EliteMobs gear tier.")
                .senderType(Player.class)
                .handler(commandContext -> {
                    if (DefaultConfig.otherCommandsLeadToEMStatusMenu)
                        new PlayerStatusScreen((Player) commandContext.getSender());
                    else new CheckTierCommand((Player) commandContext.getSender());
                }));

        // /em checktier <username>
        manager.command(builder.literal("checktier")
                .argument(onlinePlayers.copy(), ArgumentDescription.of("Player name"))
                .meta(CommandMeta.DESCRIPTION, "Checks the equipped EliteMobs gear tier of another player.")
                .senderType(CommandSender.class)
                .permission("elitemobs.checktier.others")
                .handler(commandContext -> new CheckTierOthersCommand(commandContext.getSender(), commandContext.get("onlinePlayer"))));

        // /em trackcustomboss uuid
        manager.command(builder.literal("trackcustomboss")
                .argument(StringArgument.newBuilder("uuid"), ArgumentDescription.of("UUID of the custom boss to track."))
                .meta(CommandMeta.DESCRIPTION, "Tracks a Custom Boss.")
                .senderType(Player.class)
                .handler(commandContext -> {
                    try {
                        for (CustomBossEntity customBossEntity : CustomBossEntity.trackableCustomBosses)
                            if (customBossEntity.uuid.equals(UUID.fromString(commandContext.get("uuid")))) {
                                customBossEntity.startBossBarTask((Player) commandContext.getSender(), true);
                                return;
                            }
                        commandContext.getSender().sendMessage("[EliteMobs] Sorry, this boss is already gone!");
                    } catch (Exception ex) {
                        //happens when players try to track an entity that has despawned for any reason
                        commandContext.getSender().sendMessage("[EliteMobs] Sorry, this boss is already gone!");
                    }
                }));

        // /em updateitem
        manager.command(builder.literal("updateitem")
                .meta(CommandMeta.DESCRIPTION, "Manually updates the lore of an item. Used for debugging purposes.")
                .senderType(Player.class)
                .handler(commandContext -> {
                    new EliteItemLore(((Player) commandContext.getSender()).getItemInHand(), false);
                }));

        // /em dungeontp <dungeonid>
        manager.command(builder.literal("dungeontp")
                .argument(StringArgument.newBuilder("dungeonid"), ArgumentDescription.of("ID of the dungeon to teleport to."))
                .meta(CommandMeta.DESCRIPTION, "Teleports players to Lairs, Minidungeons and Dungeons.")
                .senderType(Player.class)
                .handler(commandContext -> DungeonCommands.teleport((Player) commandContext.getSender(), commandContext.get("dungeonid"))));

        // /em spawntp
        manager.command(builder.literal("spawntp")
                .meta(CommandMeta.DESCRIPTION, "Teleports players to the server spawn.")
                .senderType(Player.class)
                .permission("elitemobs.spawntp")
                .handler(commandContext -> {
                    if (DefaultConfig.defaultSpawnLocation != null)
                        PlayerPreTeleportEvent.teleportPlayer((Player) commandContext.getSender(), DefaultConfig.defaultSpawnLocation);
                }));

    }

}
