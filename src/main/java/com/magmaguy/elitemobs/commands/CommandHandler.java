package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.command.CommandManager;

public class CommandHandler {
    private  static CommandManager emCommand;
    private  static CommandManager adventurersGuildCommand;
    private CommandHandler() {
    }

    public static void registerCommands() {
        emCommand = new CommandManager(MetadataHandler.PLUGIN, "elitemobs");

        //Admin commands
        emCommand.registerCommand(new SetupCommand());
        emCommand.registerCommand(new SetupDoneCommand());
        emCommand.registerCommand(new SetupToggleCommand());
        emCommand.registerCommand(new SpawnBossCommand());
        emCommand.registerCommand(new SpawnBossLevelCommand());
        emCommand.registerCommand(new SpawnBossAtCommand());
        emCommand.registerCommand(new SpawnBossLevelAtCommand());
        emCommand.registerCommand(new PlaceBossCommand());
        emCommand.registerCommand(new PlaceTreasureChestCommand());
        emCommand.registerCommand(new PlaceNPCCommand());
        emCommand.registerCommand(new RemoveCommand());
        emCommand.registerCommand(new EventCommand());
        emCommand.registerCommand(new StatsCommand());
        emCommand.registerCommand(new LootMenuCommand());
        emCommand.registerCommand(new LootGiveCommand());
        emCommand.registerCommand(new LootRandomCommand());
        emCommand.registerCommand(new LootSimulateMultipleCommand());
        emCommand.registerCommand(new LootSimulateCommand());
        emCommand.registerCommand(new VersionCommand());
        emCommand.registerCommand(new ReloadCommand());
        emCommand.registerCommand(new KillCommand());
        emCommand.registerCommand(new KillRadiusCommand());
        emCommand.registerCommand(new KillTypeCommand());
        emCommand.registerCommand(new KillTypeRadiusCommand());
        emCommand.registerCommand(new LootDebugCommand());
        emCommand.registerCommand(new MoneyAddCommand());
        emCommand.registerCommand(new MoneyAddAllCommand());
        emCommand.registerCommand(new MoneySetCommand());
        emCommand.registerCommand(new MoneyCheckPlayerCommand());
        emCommand.registerCommand(new UnbindForceCommand());
        emCommand.registerCommand(new FireballCommand());
        emCommand.registerCommand(new RespawnAllCommand());
        emCommand.registerCommand(new PackageDungeonCommand());
        emCommand.registerCommand(new LanguageCommand());
        emCommand.registerCommand(new PlaceWormholeCommand());
        emCommand.registerCommand(new LootStats());
        emCommand.registerCommand(new ShopProceduralOtherCommand());
        emCommand.registerCommand(new ShopCustomOtherCommand());
        emCommand.registerCommand(new ShopSellOtherCommand());
        emCommand.registerCommand(new QuestBypassCommand());
        emCommand.registerCommand(new QuestCompleteCommand());
        emCommand.registerCommand(new QuestCompleteQuestCommand());
        emCommand.registerCommand(new QuestResetCommand());
        emCommand.registerCommand(new QuestResetAllCommand());
        emCommand.registerCommand(new TransitiveBlocksCancelCommand());
        emCommand.registerCommand(new TransitiveBlocksRegisterCommand());
        emCommand.registerCommand(new TransitiveBlocksEditCommand());
        emCommand.registerCommand(new TransitiveBlocksRegisterAreaCommand());
        emCommand.registerCommand(new TransitiveBlocksEditAreaCommand());
        emCommand.registerCommand(new RankSetCommand());
        emCommand.registerCommand(new SpawnElite());
        emCommand.registerCommand(new SpawnEliteAtCommand());
        emCommand.registerCommand(new DiscordMessageCommand());
        emCommand.registerCommand(new DiscordCommand());
        emCommand.registerCommand(new MoneyRemoveCommand());
        emCommand.registerCommand(new ProtectionBypassCommand());
        emCommand.registerCommand(new FirstTimeSetupCommand());

        //User commands
//        emCommand.registerCommand(new AdventurersGuildCommand());
        emCommand.registerCommand(new ShareItemCommand());
        emCommand.registerCommand(new ShopDynamicCommand());
        emCommand.registerCommand(new ShopCustomCommand());
        emCommand.registerCommand(new RepairCommand());
        emCommand.registerCommand(new EnchantCommand());
        emCommand.registerCommand(new EliteScrollCommand());
        emCommand.registerCommand(new ScrollGetCommand());
        emCommand.registerCommand(new ScrapCommand());
        emCommand.registerCommand(new UnbindCommand());
        emCommand.registerCommand(new MoneyCheckCommand());
        emCommand.registerCommand(new QuestAcceptCommand());
        emCommand.registerCommand(new QuestCheckCommand());
        emCommand.registerCommand(new QuestTrackCommand());
        emCommand.registerCommand(new QuestLeaveCommand());
        emCommand.registerCommand(new RankCommand());
        emCommand.registerCommand(new LootCommand());
        emCommand.registerCommand(new QuitCommand());
        emCommand.registerCommand(new StartCommand());
        emCommand.registerCommand(new ArenaCommand());
        emCommand.registerCommand(new DismissCommand());
        emCommand.registerCommand(new AltCommand());
        emCommand.registerCommand(new SpawnTeleportCommand());
        emCommand.registerCommand(new DungeonTeleportCommand());
        emCommand.registerCommand(new TrackBossCommand());
        emCommand.registerCommand(new PayCommand());
        emCommand.registerCommand(new AdventurersGuildArgCommand());
        emCommand.registerCommand(new NPCQuestList());

        emCommand.registerCommand(new EliteMobsCommand());
        emCommand.registerCommand(new HelpCommand());

        adventurersGuildCommand =new CommandManager(MetadataHandler.PLUGIN, "adventurersguild");
        adventurersGuildCommand.registerCommand(new AdventurersGuildCommand());
    }
}
