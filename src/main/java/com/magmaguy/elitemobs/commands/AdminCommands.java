package com.magmaguy.elitemobs.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.*;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import cloud.commandframework.types.tuples.Triplet;
import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.menus.GetLootMenu;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import com.magmaguy.elitemobs.commands.admin.*;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.config.events.EventsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;

public class AdminCommands {

    public AdminCommands(PaperCommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {

        // /em setup
        manager.command(builder.literal("setup")
                .meta(CommandMeta.DESCRIPTION, "Opens the main setup menu")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SetupHandler.setupMenuCommand((Player) commandContext.getSender())));

        // /em setup done
        manager.command(builder.literal("setup")
                .literal("done")
                .meta(CommandMeta.DESCRIPTION, "Stops showing messages on admin login.")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> {
                    DefaultConfig.toggleSetupDone();
                    if (DefaultConfig.setupDone)
                        commandContext.getSender().sendMessage(ChatColorConverter.convert("&8[EliteMobs] &aEliteMobs" +
                                " will no longer send messages on login. You can do [/em setup done] again to revert this."));
                    else commandContext.getSender().sendMessage(ChatColorConverter.convert("&8[EliteMobs] &aEliteMobs" +
                            " will once again send messages on login. You can do [/em setup done] again to revert this."));
                }));

        // /em setup minidungeon <minidungeonName>
        manager.command(builder.literal("setup")
                .literal("minidungeon")
                .argument(StringArgument.newBuilder("minidungeonName"), ArgumentDescription.of("minidungeon name"))
                .meta(CommandMeta.DESCRIPTION, "Installs a Minidungeon")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SetupHandler.setupMinidungeonCommand((Player) commandContext.getSender(), commandContext.get("minidungeonName"))));

        // /em setup minidungeon <minidungeonName> noPaste
        manager.command(builder.literal("setup")
                .literal("minidungeon")
                .argument(StringArgument.newBuilder("minidungeonName"), ArgumentDescription.of("minidungeon name"))
                .literal("noPaste")
                .meta(CommandMeta.DESCRIPTION, "Installs a Minidungeon without pasting the WorldEdit schematic")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SetupHandler.setupMinidungeonNoPasteCommand((Player) commandContext.getSender(), commandContext.get("minidungeonName"))));

        // /em setup minidungeon <minidungeonName>
        manager.command(builder.literal("setup")
                .literal("unminidungeon")
                .argument(StringArgument.newBuilder("minidungeonName"), ArgumentDescription.of("minidungeon name"))
                .meta(CommandMeta.DESCRIPTION, "Uninstalls a Minidungeon")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SetupHandler.setupUnminidungeonCommand((Player) commandContext.getSender(), commandContext.get("minidungeonName"))));

        // /em setup unminidungeon <minidungeonName> noPaste
        manager.command(builder.literal("setup")
                .literal("unminidungeon")
                .argument(StringArgument.newBuilder("minidungeonName"), ArgumentDescription.of("minidungeon name"))
                .literal("noPaste")
                .meta(CommandMeta.DESCRIPTION, "Uninstalls a Minidungeon without undoing a WorldEdit paste.")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SetupHandler.setupUnminidungeonNoPasteCommand((Player) commandContext.getSender(), commandContext.get("minidungeonName"))));

        // /em setup area <areaName>
        manager.command(builder.literal("setup")
                .literal("area")
                .argument(StringArgument.newBuilder("areaName"), ArgumentDescription.of("WorldGuard region name"))
                .meta(CommandMeta.DESCRIPTION, "Protects an area using WorldGuard, used for Minidungeons and the Adventurer's World Hub")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SetupHandler.setupAreaCommand((Player) commandContext.getSender(), commandContext.get("areaName"))));

        ArrayList<String> powers = new ArrayList<>();
        for (ElitePower elitePower : ElitePower.elitePowers)
            powers.add(elitePower.getFileName());
        powers.add("custom");

        // /em spawnelite <entityType> <level> <power1> <power2> <power3>
        manager.command(builder.literal("spawnelite")
                .argument(EnumArgument.newBuilder(EntityType.class, "entityType"),
                        ArgumentDescription.of("Minecraft Entity Type, must be a valid type for an Elite"))
                .argument(IntegerArgument.newBuilder("eliteLevel"), ArgumentDescription.of("Elite Mob level"))
                .argument(StringArrayArgument.optional("powers", ((commandContext, lastString) -> powers)),
                        ArgumentDescription.of("List of powers"))
                .meta(CommandMeta.DESCRIPTION, "Spawns an Elite based on the entity type.")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SpawnCommand.spawnEliteEntityTypeCommand(
                        (Player) commandContext.getSender(),
                        commandContext.get("entityType"),
                        commandContext.get("eliteLevel"),
                        commandContext.getOrDefault("powers", new String[0]))));

        // /em spawnlocationelite <entityType> <worldName> <x> <y> <z> <level> <power1> <power2> <power3>
        manager.command(builder.literal("spawnlocationelite")
                .argument(EnumArgument.newBuilder(EntityType.class, "entityType"),
                        ArgumentDescription.of("Minecraft Entity Type, must be a valid type for an Elite"))
                .argument(StringArgument.newBuilder("worldName"), ArgumentDescription.of("Name of the world"))
                .argumentTriplet("coords",
                        TypeToken.get(Vector.class),
                        Triplet.of("x", "y", "z"),
                        Triplet.of(Integer.class, Integer.class, Integer.class),
                        (sender, triplet) -> new Vector(triplet.getFirst(), triplet.getSecond(), triplet.getThird()),
                        ArgumentDescription.of("Coordinates"))
                .argument(IntegerArgument.newBuilder("eliteLevel"), ArgumentDescription.of("Elite Mob level"))
                .argument(StringArrayArgument.optional("powers", ((commandContext, lastString) -> powers)),
                        ArgumentDescription.of("List of powers"))
                .meta(CommandMeta.DESCRIPTION, "Spawns an Elite based on the entity type and location.")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SpawnCommand.spawnEliteEntityTypeCommand(
                        commandContext.getSender(),
                        commandContext.get("entityType"),
                        commandContext.get("worldName"),
                        commandContext.get("coords"),
                        commandContext.get("eliteLevel"),
                        commandContext.get("powers"))));

        ArrayList<String> customBosses = new ArrayList<>(CustomBossConfigFields.customBossConfigFields.keySet());
        ArrayList<String> regionalBosses = new ArrayList<>(CustomBossConfigFields.regionalElites.keySet());

        // /em spawncustom <fileName>
        manager.command(builder.literal("spawncustom", "spawncustomboss")
                .argument(StringArgument.<CommandSender>newBuilder("fileName").withSuggestionsProvider(((objectCommandContext, s) -> customBosses)),
                        ArgumentDescription.of("Custom Boss configuration file name"))
                .meta(CommandMeta.DESCRIPTION, "Spawns an Elite based on the entity type.")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SpawnCommand.spawnCustomBossCommand(
                        (Player) commandContext.getSender(),
                        commandContext.get("fileName"))));

        // /em spawncustomlevel <fileName> <level>
        manager.command(builder.literal("spawncustomlevel", "spawncustombosslevel")
                .argument(StringArgument.<CommandSender>newBuilder("fileName").withSuggestionsProvider(((objectCommandContext, s) -> customBosses)),
                        ArgumentDescription.of("Custom Boss configuration file name"))
                .argument(IntegerArgument.newBuilder("eliteLevel"), ArgumentDescription.of("Elite Mob level"))
                .meta(CommandMeta.DESCRIPTION, "Spawns a Custom Boss at a specific level.")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SpawnCommand.spawnCustomBossCommand(
                        (Player) commandContext.getSender(),
                        commandContext.get("fileName"),
                        commandContext.get("eliteLevel"))));

        // /em spawnlocationcustom <filename> <worldName> <x> <y> <z>
        manager.command(builder.literal("spawnlocationcustom", "spawnlocationcustomboss")
                .argument(StringArgument.<CommandSender>newBuilder("fileName").withSuggestionsProvider(((objectCommandContext, s) -> customBosses)),
                        ArgumentDescription.of("Custom Boss configuration file name"))
                .argument(StringArgument.newBuilder("worldName"), ArgumentDescription.of("Name of the world"))
                .argumentTriplet("coords",
                        TypeToken.get(Vector.class),
                        Triplet.of("x", "y", "z"),
                        Triplet.of(Integer.class, Integer.class, Integer.class),
                        (sender, triplet) -> new Vector(triplet.getFirst(), triplet.getSecond(), triplet.getThird()),
                        ArgumentDescription.of("Coordinates"))
                .meta(CommandMeta.DESCRIPTION, "Spawns an Elite based on the entity type and location.")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SpawnCommand.spawnCustomBossCommand(
                        commandContext.getSender(),
                        commandContext.get("fileName"),
                        commandContext.get("worldName"),
                        commandContext.get("coords"))));

        // /em spawnlocationcustomlevel <filename> <level> <worldName> <x> <y> <z>
        manager.command(builder.literal("spawnlocationcustomlevel", "spawnlocationcustombosslevel")
                .argument(StringArgument.<CommandSender>newBuilder("fileName").withSuggestionsProvider(((objectCommandContext, s) -> customBosses)),
                        ArgumentDescription.of("Custom Boss configuration file name"))
                .argument(IntegerArgument.newBuilder("eliteLevel"), ArgumentDescription.of("Elite Mob level"))
                .argument(StringArgument.newBuilder("worldName"), ArgumentDescription.of("Name of the world"))
                .argumentTriplet("coords",
                        TypeToken.get(Vector.class),
                        Triplet.of("x", "y", "z"),
                        Triplet.of(Integer.class, Integer.class, Integer.class),
                        (sender, triplet) -> new Vector(triplet.getFirst(), triplet.getSecond(), triplet.getThird()),
                        ArgumentDescription.of("Coordinates"))
                .meta(CommandMeta.DESCRIPTION, "Spawns an Elite based on the entity type and location.")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SpawnCommand.spawnCustomBossCommand(
                        commandContext.getSender(),
                        commandContext.get("fileName"),
                        commandContext.get("worldName"),
                        commandContext.get("coords"),
                        commandContext.get("eliteLevel"))));

        // /em spawnsuper <EntityType>
        manager.command(builder.literal("spawnsuper", "spawnsupermob")
                .argument(EnumArgument.newBuilder(EntityType.class, "entityType"),
                        ArgumentDescription.of("Minecraft Entity Type, must be a valid type for a Super Mob"))
                .meta(CommandMeta.DESCRIPTION, "Spawns a Super Mob based on the entity type.")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SpawnCommand.spawnSuperMobCommand(
                        (Player) commandContext.getSender(),
                        commandContext.get("entityType"))));

        // /em addSpawnLocation <fileName>
        manager.command(builder.literal("addSpawnLocation", "asp")
                .argument(StringArgument.<CommandSender>newBuilder("fileName").withSuggestionsProvider(((objectCommandContext, s) -> regionalBosses)),
                        ArgumentDescription.of("Custom Boss configuration file name"))
                .meta(CommandMeta.DESCRIPTION, "Adds a spawn location to a Regional Boss.")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> CustomBossCommandHandler.addSpawnLocation(
                        commandContext.get("fileName"), (Player) commandContext.getSender())));

        ArrayList<String> minidungeonFileNames = new ArrayList<>(Minidungeon.minidungeons.keySet());

        // /em addRelativeSpawnLocation <customBossFileName> <minidungeonFileName>
        manager.command(builder.literal("addRelativeSpawnLocation", "arsp")
                .argument(StringArgument.<CommandSender>newBuilder("bossFileName").withSuggestionsProvider(((objectCommandContext, s) -> regionalBosses)),
                        ArgumentDescription.of("Custom Boss configuration file name"))
                .argument(StringArgument.<CommandSender>newBuilder("minidungeonFileName").withSuggestionsProvider(((objectCommandContext, s) -> minidungeonFileNames)),
                        ArgumentDescription.of("Minidungeon configuration file name"))
                .meta(CommandMeta.DESCRIPTION, "Adds a spawn location to a Regional Boss.")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> CustomBossCommandHandler.addRelativeSpawnLocation(
                        (Player) commandContext.getSender(), commandContext.get("bossFileName"), commandContext.get("minidungeonFileName"))));

        // /em setLeashRadius <fileName> <radius>
        manager.command(builder.literal("addSpawnLocation", "asp")
                .argument(StringArgument.<CommandSender>newBuilder("fileName").withSuggestionsProvider(((objectCommandContext, s) -> regionalBosses)),
                        ArgumentDescription.of("Custom Boss configuration file name"))
                .argument(DoubleArgument.newBuilder("radius"), ArgumentDescription.of("Radius of the Regional Boss leash"))
                .meta(CommandMeta.DESCRIPTION, "Adds a spawn location to a Regional Boss.")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> CustomBossCommandHandler.setLeashRadius(
                        commandContext.get("fileName"), commandContext.getSender(), commandContext.get("radius"))));

        // /em remove
        manager.command(builder.literal("remove", "r")
                .meta(CommandMeta.DESCRIPTION, "Permanently removes an Elite Mob entity. Elite/Regional/Super/NPCs all work.")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> RemoveCommand.remove((Player) commandContext.getSender())));


        // /em debug <name>
        manager.command(builder.literal("debug", "d")
                .argument(StringArgument.<CommandSender>newBuilder("argument").withSuggestionsProvider(((objectCommandContext, s) -> {
                            ArrayList<String> arrayList = new ArrayList<String>((Collection<? extends String>) regionalBosses.clone());
                            Bukkit.getOnlinePlayers().forEach(player -> arrayList.add(player.getName()));
                            return arrayList;
                        })),
                        ArgumentDescription.of("Player name or regional boss file name"))
                .meta(CommandMeta.DESCRIPTION, "Opens a debug screen for players or regional bosses.")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> DebugScreen.open((Player) commandContext.getSender(), commandContext.get("argument"))));

        ArrayList<String> events = new ArrayList<>(EventsConfig.eventFields.keySet());

        // /em event <eventName>
        manager.command(builder.literal("event")
                .argument(StringArgument.<CommandSender>newBuilder("events").withSuggestionsProvider(((objectCommandContext, s) -> events)),
                        ArgumentDescription.of("Custom Boss configuration file name"))
                .meta(CommandMeta.DESCRIPTION, "Opens a debug screen for players or regional bosses.")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> EventCommand.trigger(commandContext.getSender(), commandContext.get("events"))));

        ArrayList<String> npcs = new ArrayList<>(NPCsConfig.NPCsList.keySet());

        // /em spawnnpc <npcFileName>
        manager.command(builder.literal("spawnnpc")
                .argument(StringArgument.<CommandSender>newBuilder("npcFileName").withSuggestionsProvider(((objectCommandContext, s) -> npcs)),
                        ArgumentDescription.of("Custom Boss configuration file name"))
                .meta(CommandMeta.DESCRIPTION, "Spawns an NPC")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> NPCCommands.set((Player) commandContext.getSender(), commandContext.get("npcFileName"))));

        // /em stats
        manager.command(builder.literal("stats")
                .meta(CommandMeta.DESCRIPTION, "Gets the stats for the currently active EliteMobs entities and players.")
                .senderType(CommandSender.class)
                .permission("elitemobs.stats")
                .handler(commandContext -> StatsCommand.statsHandler(commandContext.getSender())));

        // /em getloot
        manager.command(builder.literal("getloot")
                .meta(CommandMeta.DESCRIPTION, "Opens a menu where you can get any Custom Loot")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> new GetLootMenu((Player) commandContext.getSender())));

        ArrayList<String> customItems = new ArrayList<>(CustomItem.getCustomItems().keySet());

        // /em getloot <filename>
        manager.command(builder.literal("getloot")
                .argument(StringArgument.<CommandSender>newBuilder("customItem").withSuggestionsProvider(((objectCommandContext, s) -> customItems)),
                        ArgumentDescription.of("File name of the custom item"))
                .meta(CommandMeta.DESCRIPTION, "Gets a specific custom item")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> LootCommand.get((Player) commandContext.getSender(), commandContext.get("customItem"))));

        // /em giveloot <filename> <player>
        manager.command(builder.literal("give")
                .argument(StringArgument.<CommandSender>newBuilder("customItem").withSuggestionsProvider(((objectCommandContext, s) -> customItems)),
                        ArgumentDescription.of("File name of the custom item"))
                .argument(StringArgument.<CommandSender>newBuilder("player").withSuggestionsProvider(((objectCommandContext, s) -> {
                            ArrayList<String> arrayList = new ArrayList<>();
                            Bukkit.getOnlinePlayers().forEach(player -> arrayList.add(player.getName()));
                            return arrayList;
                        })),
                        ArgumentDescription.of("Name of the player that will get the custom item"))
                .meta(CommandMeta.DESCRIPTION, "Gives a specific custom item to a player.")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> LootCommand.give(commandContext.getSender(),
                        commandContext.get("player"),
                        commandContext.get("customItem"))));

        // /em simloot <level>
        manager.command(builder.literal("simloot")
                .argument(IntegerArgument.newBuilder("level"),
                        ArgumentDescription.of("Level of Elite Mob to simulate"))
                .meta(CommandMeta.DESCRIPTION, "Simulates drops from an Elite Mob from the set tier")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SimLootCommand.run((Player) commandContext.getSender(), commandContext.get("level"))));

        // /em simloot <level> <times>
        manager.command(builder.literal("simloot")
                .argument(IntegerArgument.newBuilder("level"),
                        ArgumentDescription.of("Level of Elite Mob to simulate"))
                .argument(IntegerArgument.newBuilder("times"),
                        ArgumentDescription.of("Number of times that the simulation will run"))
                .meta(CommandMeta.DESCRIPTION, "Simulates drops from an Elite Mob from the set tier a set amount of times")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> SimLootCommand.runMultipleTimes(
                        (Player) commandContext.getSender(),
                        commandContext.get("level"),
                        commandContext.get("times"))));

        // /em version
        manager.command(builder.literal("version")
                .meta(CommandMeta.DESCRIPTION, "Gets the version of the plugin")
                .senderType(CommandSender.class)
                .permission("elitemobs.version")
                .handler(commandContext -> VersionCommand.getVersion(commandContext.getSender())));

        // /em reload
        manager.command(builder.literal("reload")
                .meta(CommandMeta.DESCRIPTION, "Reloads the plugin. Works almost every time.")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> ReloadCommand.reload(commandContext.getSender())));

        // /em killaggressive
        manager.command(builder.literal("killaggressive")
                .meta(CommandMeta.DESCRIPTION, "Kills all aggressive Elite Mobs.")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> KillHandler.killAggressiveMobs(commandContext.getSender())));

        // /em killaggressive <radius>
        manager.command(builder.literal("killaggressive")
                .argument(IntegerArgument.newBuilder("radius"),
                        ArgumentDescription.of("Distance to kill aggressive elite mobs in"))
                .meta(CommandMeta.DESCRIPTION, "Kills all aggressive Elite Mobs in a radius.")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> KillHandler.radiusKillAggressiveMobs((Player) commandContext.getSender(), commandContext.get("radius"))));

        // /em killpassive
        manager.command(builder.literal("killpassive")
                .meta(CommandMeta.DESCRIPTION, "Kills all passive Super Mobs")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> KillHandler.killPassiveMobs(commandContext.getSender())));

        // /em killpassive <radius>
        manager.command(builder.literal("killpassive")
                .argument(IntegerArgument.newBuilder("radius"),
                        ArgumentDescription.of("Distance to kill aggressive elite mobs in"))
                .meta(CommandMeta.DESCRIPTION, "Kills all passive Super Mobs in a radius")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> KillHandler.radiusKillPassiveMobs((Player) commandContext.getSender(), commandContext.get("radius"))));


        // /em killtype <entityType>
        manager.command(builder.literal("killtype")
                .argument(EnumArgument.newBuilder(EntityType.class, "entityType"),
                        ArgumentDescription.of("Minecraft Entity Type to kill"))
                .meta(CommandMeta.DESCRIPTION, "Kills all elites of a specific type")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> KillHandler.killEntityType(commandContext.getSender(), commandContext.get("entityType"))));

        // /em killtype <entityType> <radius>
        manager.command(builder.literal("killtype")
                .argument(EnumArgument.newBuilder(EntityType.class, "entityType"),
                        ArgumentDescription.of("Minecraft Entity Type to kill"))
                .argument(IntegerArgument.newBuilder("radius"),
                        ArgumentDescription.of("Distance to kill aggressive elite mobs in"))
                .meta(CommandMeta.DESCRIPTION, "Kills all elites of a specific type in a radius")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> KillHandler.radiusKillSpecificMobs((Player) commandContext.getSender(),
                        commandContext.get("entityType"),
                        commandContext.get("radius"))));

        // /em gettier <tier>
        manager.command(builder.literal("gettier")
                .argument(IntegerArgument.newBuilder("tier"),
                        ArgumentDescription.of("Tier of the item to get"))
                .meta(CommandMeta.DESCRIPTION, "Gets debug items for testing purposes")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .handler(commandContext -> GetTierCommand.get((Player) commandContext.getSender(), commandContext.get("tier"))));


        CommandArgument<CommandSender, String> onlinePlayers = StringArgument.<CommandSender>newBuilder("onlinePlayer")
                .withSuggestionsProvider(((objectCommandContext, s) -> {
                    ArrayList<String> arrayList = new ArrayList<>();
                    Bukkit.getOnlinePlayers().forEach(player -> arrayList.add(player.getName()));
                    return arrayList;
                })).build();

        // /em money add <username> <amount>
        manager.command(builder.literal("money")
                .literal("add")
                .argument(onlinePlayers.copy(), ArgumentDescription.of("Player name"))
                .argument(IntegerArgument.newBuilder("amount"), ArgumentDescription.of("Amount of money to add"))
                .meta(CommandMeta.DESCRIPTION, "Adds a set amount of money to a player")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> CurrencyCommandsHandler.addCommand(commandContext.getSender(),
                        commandContext.get("onlinePlayer"),
                        commandContext.get("amount"))));

        // /em money addall <amount>
        manager.command(builder.literal("money")
                .literal("addall")
                .argument(IntegerArgument.newBuilder("amount"), ArgumentDescription.of("Amount of money to add"))
                .meta(CommandMeta.DESCRIPTION, "Adds a set amount of money to all online players")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> CurrencyCommandsHandler.addAllCommand(commandContext.getSender(),
                        commandContext.get("amount"))));

        // /em money remove <username> <amount>
        manager.command(builder.literal("money")
                .literal("remove")
                .argument(onlinePlayers.copy(), ArgumentDescription.of("Player name"))
                .argument(IntegerArgument.newBuilder("amount"), ArgumentDescription.of("Amount of money to add"))
                .meta(CommandMeta.DESCRIPTION, "Removes a set amount of money from a player")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> CurrencyCommandsHandler.subtractCommand(commandContext.getSender(),
                        commandContext.get("onlinePlayer"),
                        commandContext.get("amount"))));

        // /em money set <username> <amount>
        manager.command(builder.literal("money")
                .literal("set")
                .argument(onlinePlayers.copy(), ArgumentDescription.of("Player name"))
                .argument(DoubleArgument.newBuilder("amount"), ArgumentDescription.of("Amount to be set"))
                .meta(CommandMeta.DESCRIPTION, "Sets the total currency amount of a player")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> CurrencyCommandsHandler.setCommand(commandContext.getSender(),
                        commandContext.get("onlinePlayer"),
                        commandContext.get("amount"))));

        // /em setrank <player> <prestigetier> <guildtier>
        manager.command(builder.literal("setrank")
                .argument(onlinePlayers.copy(), ArgumentDescription.of("Player name"))
                .argument(IntegerArgument.newBuilder("prestigeRank"), ArgumentDescription.of("Prestige rank, 0-10"))
                .argument(IntegerArgument.newBuilder("guildRank"), ArgumentDescription.of("Guild rank, 0-20"))
                .meta(CommandMeta.DESCRIPTION, "Sets the guild rank of a player.")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> GuildRankCommands.setGuildRank(commandContext.getSender(),
                        commandContext.get("onlinePlayer"),
                        commandContext.get("prestigeRank"),
                        commandContext.get("guildRank"))));

        // /em discord
        manager.command(builder.literal("discord")
                .meta(CommandMeta.DESCRIPTION, "Gets the link for the support Discord server.")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> commandContext.getSender().sendMessage(
                        ChatColorConverter.convert("&8[EliteMobs] &6Discord room for support & downloads: &9" + DiscordLinks.mainLink))));

        // /em discord <message>
        manager.command(builder.literal("discord")
                .argument(StringArgument.<CommandSender>newBuilder("message").greedy().build(),
                        ArgumentDescription.of("Message to be sent to Discord"))
                .meta(CommandMeta.DESCRIPTION, "Posts a debug message on Discord if DiscordSRV is configured correctly.")
                .senderType(CommandSender.class)
                .permission("elitemobs.*")
                .handler(commandContext -> {
                    new DiscordSRVAnnouncement(commandContext.get("message"));
                    commandContext.getSender().sendMessage(ChatColorConverter.convert("&8[EliteMobs] &aAttempted to send a message to Discord!"));
                }));

        // /em unbind
        manager.command(builder.literal("unbind")
                .senderType(Player.class)
                .permission("elitemobs.*")
                .meta(CommandMeta.DESCRIPTION, "Unbinds a held soulbound item.")
                .handler(commandContext -> {
                    ItemStack itemStack = ((Player) commandContext.getSender()).getInventory().getItemInMainHand();
                    if (ItemTagger.isEliteItem(itemStack))
                        SoulbindEnchantment.removeEnchantment(itemStack);
                }));

        // /em relativecoords <minidungeon>
        manager.command(builder.literal("relativecoords")
                .argument(StringArgument.<CommandSender>newBuilder("minidungeonFileName").withSuggestionsProvider(((objectCommandContext, s) -> minidungeonFileNames)),
                        ArgumentDescription.of("Minidungeon configuration file name"))
                .senderType(Player.class)
                .permission("elitemobs.*")
                .meta(CommandMeta.DESCRIPTION, "Gets the relative coordinated to an installed dungeon.")
                .handler(commandContext -> RelativeCoordinatesCommand.get((Player) commandContext.getSender(), commandContext.get("minidungeonFileName"))));

        // /em wallet <player>
        manager.command(builder.literal("wallet")
                .argument(onlinePlayers.copy(), ArgumentDescription.of("Player name"))
                .senderType(CommandSender.class)
                .permission("elitemobs.currency.check.others")
                .meta(CommandMeta.DESCRIPTION, "Checks the currency of a specific player.")
                .handler(commandContext -> CurrencyCommandsHandler.checkCommand(commandContext.getSender(), commandContext.get("onlinePlayer"))));

    }

}
