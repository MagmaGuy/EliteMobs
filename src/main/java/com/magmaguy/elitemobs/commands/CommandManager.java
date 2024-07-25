package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;

public class CommandManager extends com.magmaguy.magmacore.command.CommandManager {
    public CommandManager() {
        super(MetadataHandler.PLUGIN, "elitemobs");
    }

    @Override
    public void registerCommands() {
        //Admin commands
        registerCommand(new SetupCommand());
        registerCommand(new SetupDoneCommand());
        registerCommand(new SetupToggleCommand());
        registerCommand(new SpawnBossCommand());
        registerCommand(new SpawnBossLevelCommand());
        registerCommand(new SpawnBossAtCommand());
        registerCommand(new SpawnBossLevelAtCommand());
        registerCommand(new PlaceBossCommand());
        registerCommand(new PlaceTreasureChestCommand());
        registerCommand(new PlaceNPCCommand());
        registerCommand(new RemoveCommand());
        registerCommand(new DebugCommand());
        registerCommand(new EventCommand());
        registerCommand(new StatsCommand());
        registerCommand(new LootMenuCommand());
        registerCommand(new LootGiveCommand());
        registerCommand(new LootRandomCommand());
        registerCommand(new LootSimulateMultipleCommand());
        registerCommand(new LootSimulateCommand());
        registerCommand(new VersionCommand());
        registerCommand(new ReloadCommand());
        registerCommand(new KillCommand());
        registerCommand(new KillRadiusCommand());
        registerCommand(new KillTypeCommand());
        registerCommand(new KillTypeRadiusCommand());
        registerCommand(new LootDebugCommand());
        registerCommand(new MoneyAddCommand());
        registerCommand(new MoneyAddAllCommand());
        registerCommand(new MoneySetCommand());
        registerCommand(new MoneyCheckPlayerCommand());
        registerCommand(new UnbindForceCommand());
        registerCommand(new FireballCommand());
        registerCommand(new RespawnAllCommand());
        registerCommand(new PackageDungeonCommand());
        registerCommand(new LanguageCommand());
        registerCommand(new PlaceWormholeCommand());
        registerCommand(new LootStats());
        registerCommand(new ShopProceduralOtherCommand());
        registerCommand(new ShopCustomOtherCommand());
        registerCommand(new ShopSellOtherCommand());
        registerCommand(new QuestBypassCommand());
        registerCommand(new QuestCompleteCommand());
        registerCommand(new QuestResetCommand());
        registerCommand(new QuestResetAllCommand());
        registerCommand(new TransitiveBlocksCancelCommand());
        registerCommand(new TransitiveBlocksRegisterCommand());
        registerCommand(new TransitiveBlocksEditCommand());
        registerCommand(new TransitiveBlocksRegisterAreaCommand());
        registerCommand(new TransitiveBlocksEditAreaCommand());
        registerCommand(new RankSetCommand());
        registerCommand(new SpawnElite());
        registerCommand(new SpawnEliteAtCommand());
        registerCommand(new DiscordMessageCommand());
        registerCommand(new DiscordCommand());

        //User commands
        registerCommand(new AdventurersGuildCommand());
        registerCommand(new ShareItemCommand());
        registerCommand(new ShopDynamicCommand());
        registerCommand(new ShopCustomCommand());
        registerCommand(new RepairCommand());
        registerCommand(new EnchantCommand());
        registerCommand(new ScrapCommand());
        registerCommand(new UnbindCommand());
        registerCommand(new MoneyCheckCommand());
        registerCommand(new QuestAcceptCommand());
        registerCommand(new QuestTrackCommand());
        registerCommand(new QuestLeaveCommand());
        registerCommand(new RankCommand());
        registerCommand(new LootCommand());
        registerCommand(new QuitCommand());
        registerCommand(new StartCommand());
        registerCommand(new ArenaCommand());
        registerCommand(new DismissCommand());
        registerCommand(new AltCommand());
        registerCommand(new SpawnTeleportCommand());
        registerCommand(new DungeonTeleportCommand());
        registerCommand(new TrackBossCommand());
        registerCommand(new PayCommand());

        registerCommand(new EliteMobsCommand());
        registerCommand(new HelpCommand());
    }
}
