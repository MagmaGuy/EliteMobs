package com.magmaguy.elitemobs.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.commands.admin.CheckTierOthersCommand;
import com.magmaguy.elitemobs.commands.combat.CheckTierCommand;
import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.instanced.dungeons.MatchInstance;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.menus.*;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UserCommands {

    public UserCommands(BukkitCommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        // /em adventurersguild
        manager.command(builder.literal("adventurersguild", "ag")
                .meta(CommandMeta.DESCRIPTION, "Teleports players to the Adventurer's Guild Hub or opens the Adventurer's Guild menu.")
                .senderType(Player.class)
                .permission("elitemobs.adventurersguild.command")
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

        // /em repair
        manager.command(builder.literal("repair")
                .meta(CommandMeta.DESCRIPTION, "Opens the custom item shop or teleports the player to the Adventurer's Guild Hub")
                .senderType(Player.class)
                .permission("elitemobs.repair.command")
                .handler(commandContext -> {
                    if (!AdventurersGuildCommand.adventurersGuildTeleport((Player) commandContext.getSender())) {
                        RepairMenu repairMenu = new RepairMenu();
                        repairMenu.constructRepairMenu((Player) commandContext.getSender());
                    }
                }));

        // /em enhance
        manager.command(builder.literal("enhance")
                .meta(CommandMeta.DESCRIPTION, "Opens the custom item shop or teleports the player to the Adventurer's Guild Hub")
                .senderType(Player.class)
                .permission("elitemobs.enhancer.command")
                .handler(commandContext -> {
                    if (!AdventurersGuildCommand.adventurersGuildTeleport((Player) commandContext.getSender())) {
                        EnhancementMenu enhancementMenu = new EnhancementMenu();
                        enhancementMenu.constructEnhancementMenu((Player) commandContext.getSender());
                    }
                }));

        // /em refine
        manager.command(builder.literal("refine")
                .meta(CommandMeta.DESCRIPTION, "Opens the custom item shop or teleports the player to the Adventurer's Guild Hub")
                .senderType(Player.class)
                .permission("elitemobs.refiner.command")
                .handler(commandContext -> {
                    if (!AdventurersGuildCommand.adventurersGuildTeleport((Player) commandContext.getSender())) {
                        RefinerMenu refinerMenu = new RefinerMenu();
                        refinerMenu.constructRefinerMenu((Player) commandContext.getSender());
                    }
                }));

        // /em smelt
        manager.command(builder.literal("smelt")
                .meta(CommandMeta.DESCRIPTION, "Opens the custom item shop or teleports the player to the Adventurer's Guild Hub")
                .senderType(Player.class)
                .permission("elitemobs.smelt.command")
                .handler(commandContext -> {
                    if (!AdventurersGuildCommand.adventurersGuildTeleport((Player) commandContext.getSender())) {
                        SmeltMenu smeltMenu = new SmeltMenu();
                        smeltMenu.constructSmeltMenu((Player) commandContext.getSender());
                    }
                }));

        // /em scrap
        manager.command(builder.literal("scrap")
                .meta(CommandMeta.DESCRIPTION, "Opens the custom item shop or teleports the player to the Adventurer's Guild Hub")
                .senderType(Player.class)
                .permission("elitemobs.scrap.command")
                .handler(commandContext -> {
                    if (!AdventurersGuildCommand.adventurersGuildTeleport((Player) commandContext.getSender())) {
                        ScrapperMenu scrapperMenu = new ScrapperMenu();
                        scrapperMenu.constructScrapMenu((Player) commandContext.getSender());
                    }
                }));

        // /em unbind
        manager.command(builder.literal("unbind")
                .meta(CommandMeta.DESCRIPTION, "Opens the custom item shop or teleports the player to the Adventurer's Guild Hub")
                .senderType(Player.class)
                .permission("elitemobs.unbind.command")
                .handler(commandContext -> {
                    if (!AdventurersGuildCommand.adventurersGuildTeleport((Player) commandContext.getSender())) {
                        UnbindMenu unbindMenu = new UnbindMenu();
                        unbindMenu.constructUnbinderMenu((Player) commandContext.getSender());
                    }
                }));

        // /em wallet
        manager.command(builder.literal("wallet")
                .meta(CommandMeta.DESCRIPTION, "Checks the EliteMobs currency")
                .senderType(Player.class)
                .permission("elitemobs.currency.check")
                .handler(commandContext -> {
                    if (DefaultConfig.isOtherCommandsLeadToEMStatusMenu())
                        new PlayerStatusScreen((Player) commandContext.getSender());
                    else
                        CurrencyCommandsHandler.walletCommand((Player) commandContext.getSender());
                }));

        // /em quest accept <questID>
        manager.command(builder.literal("quest")
                .literal("accept")
                .argument(StringArgument.newBuilder("questID"), ArgumentDescription.of("Quest ID"))
                .meta(CommandMeta.DESCRIPTION, "Accepts a quest")
                .senderType(Player.class)
                .permission("elitemobs.quest.command")
                .handler(commandContext -> {
                    QuestCommand.joinQuest(commandContext.get("questID"), (Player) commandContext.getSender());
                }));

        // /em quest track <questID>
        manager.command(builder.literal("quest")
                .literal("track")
                .argument(StringArgument.newBuilder("questID"), ArgumentDescription.of("Quest ID"))
                .meta(CommandMeta.DESCRIPTION, "Toggles quest tracking")
                .senderType(Player.class)
                .permission("elitemobs.quest.command")
                .handler(commandContext -> {
                    QuestCommand.trackQuest(commandContext.get("questID"), (Player) commandContext.getSender());
                }));

        // /em quest complete
        manager.command(builder.literal("quest")
                .literal("complete")
                .argument(StringArgument.newBuilder("questID"), ArgumentDescription.of("Quest ID"))
                .meta(CommandMeta.DESCRIPTION, "Completes a quest")
                .senderType(Player.class)
                .permission("elitemobs.quest.command")
                .handler(commandContext -> {
                    QuestCommand.completeQuest(commandContext.get("questID"), (Player) commandContext.getSender());
                }));


        // /em quest leave
        manager.command(builder.literal("quest")
                .literal("leave")
                .argument(StringArgument.newBuilder("questID"), ArgumentDescription.of("Quest ID"))
                .meta(CommandMeta.DESCRIPTION, "Leaves a quest")
                .senderType(Player.class)
                .permission("elitemobs.quest.command")
                .handler(commandContext -> QuestCommand.leaveQuest((Player) commandContext.getSender(), commandContext.get("questID"))));

        CommandArgument<CommandSender, String> onlinePlayers = StringArgument.<CommandSender>newBuilder("onlinePlayer")
                .withSuggestionsProvider(((objectCommandContext, s) -> {
                    ArrayList<String> arrayList = new ArrayList<>();
                    Bukkit.getOnlinePlayers().forEach(player -> arrayList.add(player.getName()));
                    return arrayList;
                })).build();


        // Create the confirmation manager. This allows us to require certain commands to be
        // confirmed before they can be executed
        CommandConfirmationManager<CommandSender> paymentConfirmationManager = new CommandConfirmationManager<>(
                /* Timeout */ 30L,
                /* Timeout unit */ TimeUnit.SECONDS,
                /* Action when confirmation is required */ context -> context.getCommandContext().getSender().sendMessage(
                ChatColorConverter.convert(TranslationConfig.getEconomyTaxMessage()
                        .replace("$command", "/em confirm")
                        .replace("$percentage", (EconomySettingsConfig.getPlayerToPlayerTaxes() * 100) + ""))),
                /* Action when no confirmation is pending */ sender -> sender.sendMessage(
                ChatColorConverter.convert(TranslationConfig.getNoPendingCommands()))
        );

        // Register the confirmation processor. This will enable confirmations for commands that require it
        paymentConfirmationManager.registerConfirmationProcessor(manager);

        // Add a confirmation command
        manager.command(builder.literal("confirm")
                .meta(CommandMeta.DESCRIPTION, "Confirm a pending command")
                .handler(paymentConfirmationManager.createConfirmationExecutionHandler()));

        // /em pay <username> <amount>
        manager.command(builder.literal("pay")
                .argument(onlinePlayers.copy(), ArgumentDescription.of("Player name"))
                .argument(DoubleArgument.newBuilder("amount"), ArgumentDescription.of("Amount to pay"))
                .meta(CommandMeta.DESCRIPTION, "Pays an amount of currency to another player")
                .meta(CommandConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .senderType(Player.class)
                .permission("elitemobs.currency.pay")
                .handler(commandContext -> {
                    paymentConfirmationManager.createConfirmationExecutionHandler();
                    CurrencyCommandsHandler.payCommand(
                            (Player) commandContext.getSender(),
                            commandContext.get("onlinePlayer"),
                            commandContext.get("amount"));
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
                .meta(CommandMeta.DESCRIPTION, "Opens the EliteMobs status screen.")
                .senderType(Player.class)
                .handler(commandContext -> new PlayerStatusScreen((Player) commandContext.getSender())));

        // /em rank
        manager.command(builder.literal("checktier")
                .meta(CommandMeta.DESCRIPTION, "Checks your equipped EliteMobs gear tier.")
                .senderType(Player.class)
                .handler(commandContext -> {
                    if (DefaultConfig.isOtherCommandsLeadToEMStatusMenu())
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
                        for (CustomBossEntity customBossEntity : CustomBossEntity.getTrackableCustomBosses())
                            if (customBossEntity.getEliteUUID().equals(UUID.fromString(commandContext.get("uuid")))) {
                                customBossEntity.getCustomBossBossBar().addTrackingPlayer((Player) commandContext.getSender());
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
                .permission("elitemobs.dungeontp")
                .senderType(Player.class)
                .handler(commandContext -> DungeonCommands.teleport((Player) commandContext.getSender(), commandContext.get("dungeonid"))));

        // /em spawntp
        manager.command(builder.literal("spawntp")
                .meta(CommandMeta.DESCRIPTION, "Teleports players to the server spawn.")
                .senderType(Player.class)
                .permission("elitemobs.spawntp")
                .handler(commandContext -> {
                    if (DefaultConfig.getDefaultSpawnLocation() != null)
                        PlayerPreTeleportEvent.teleportPlayer((Player) commandContext.getSender(), DefaultConfig.getDefaultSpawnLocation());
                }));

        // /em alt
        manager.command(builder.literal("alt")
                .meta(CommandMeta.DESCRIPTION, "Changes the style of the /em menu.")
                .senderType(Player.class)
                .handler(commandContext -> {
                    PlayerData.setUseBookMenus(((Player) commandContext.getSender()), !PlayerData.getUseBookMenus(((Player) commandContext.getSender()).getUniqueId()));
                    commandContext.getSender().sendMessage(TranslationConfig.getSwitchEMStyleMessage());
                }));

        // /em dismiss
        manager.command(builder.literal("dismiss")
                .meta(CommandMeta.DESCRIPTION, "Dismisses /em menu message.")
                .senderType(Player.class)
                .handler(commandContext -> {
                    PlayerData.setDismissEMStatusScreenMessage(((Player) commandContext.getSender()), !PlayerData.getDismissEMStatusScreenMessage(((Player) commandContext.getSender()).getUniqueId()));
                }));

        // /em arena <arenaFilename>
        manager.command(builder.literal("arena")
                .meta(CommandMeta.DESCRIPTION, "Opens the arena menu.")
                .argument(StringArgument.newBuilder("dungeonid"), ArgumentDescription.of("Name of the arena to go to."))
                .senderType(Player.class)
                .handler(commandContext -> {
                    ArenaCommands.openArenaMenu(((Player) commandContext.getSender()), commandContext.get("arena"));
                }));

        // /em start - this is for instanced content
        manager.command(builder.literal("start")
                .meta(CommandMeta.DESCRIPTION, "When in instanced content, starts the countdown to start doing the content.")
                .senderType(Player.class)
                .handler(commandContext -> {
                    MatchInstance matchInstance = MatchInstance.getPlayerInstance(((Player) commandContext.getSender()));
                    if (matchInstance != null)
                        matchInstance.countdownMatch();
                }));

        // /em quit - this is for instanced content
        manager.command(builder.literal("quit")
                .meta(CommandMeta.DESCRIPTION, "When in instanced content, makes the player leave the instance.")
                .senderType(Player.class)
                .handler(commandContext -> {
                    MatchInstance matchInstance = MatchInstance.getAnyPlayerInstance(((Player) commandContext.getSender()));
                    if (matchInstance != null)
                        matchInstance.removeAnyKind((Player) commandContext.getSender());
                }));
    }

}
