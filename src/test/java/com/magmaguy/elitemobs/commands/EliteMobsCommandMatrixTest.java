package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.magmacore.MagmaCore;
import com.magmaguy.magmacore.command.CommandManager;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EliteMobsCommandMatrixTest {
    private ServerMock server;
    private PluginMock plugin;
    private PluginCommand eliteMobsCommand;

    @BeforeEach
    void setUp() throws Exception {
        server = MockBukkit.mock();
        plugin = loadPluginWithCommandMetadata();
        resetMagmaCore();
        MagmaCore.createInstance(plugin);
        setStaticField(CommandMessagesConfig.class, "discordMessage", "&6Discord room for support & downloads: &9");
        setStaticField(CommandMessagesConfig.class, "helpHeaderMessage", "Commands:");

        eliteMobsCommand = plugin.getCommand("elitemobs");
        assertNotNull(eliteMobsCommand);
    }

    @AfterEach
    void tearDown() throws Exception {
        CommandManager.shutdown();
        MagmaCore.shutdown(plugin);
        resetMagmaCore();
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    @Test
    void pluginYmlDeclaresRootCommandsAliasesAndRepresentativePermissions() {
        YamlConfiguration pluginYml = loadRealPluginYml();

        assertTrue(pluginYml.isConfigurationSection("commands.elitemobs"));
        assertEquals(List.of("em"), pluginYml.getStringList("commands.elitemobs.aliases"));
        assertTrue(pluginYml.isConfigurationSection("commands.adventurersguild"));
        assertEquals(List.of("ag"), pluginYml.getStringList("commands.adventurersguild.aliases"));

        assertEquals("true", pluginYml.getString("permissions.elitemobs.command.default"));
        assertEquals("true", pluginYml.getString("permissions.elitemobs.discord.link.default"));
        assertEquals("op", pluginYml.getString("permissions.elitemobs.setup.default"));
        assertEquals("op", pluginYml.getString("permissions.elitemobs.reload.default"));
    }

    @Test
    void playerFacingDiscordCommandRequiresPermissionThenSendsConfiguredLink() {
        CommandManager commandManager = new CommandManager(plugin, "elitemobs");
        commandManager.registerCommand(new DiscordCommand());

        PlayerMock deniedPlayer = server.addPlayer("DeniedDiscord");
        deniedPlayer.addAttachment(plugin, "elitemobs.discord.link", false);

        assertFalse(eliteMobsCommand.execute(deniedPlayer, "elitemobs", new String[]{"discord"}));
        assertMessageContains(deniedPlayer.nextMessage(), "You do not have permission to run this command!");

        PlayerMock allowedPlayer = server.addPlayer("AllowedDiscord");
        allowedPlayer.addAttachment(plugin, "elitemobs.discord.link", true);

        assertTrue(eliteMobsCommand.execute(allowedPlayer, "elitemobs", new String[]{"discord"}));
        String message = stripped(allowedPlayer.nextMessage());
        assertTrue(message.contains("Discord room for support & downloads:"));
        assertTrue(message.contains("https://discord.gg/eSxvPbWYy4"));
    }

    @Test
    void adminSetupCommandStopsAtSenderAndPermissionGatesBeforeOpeningMenus() {
        CommandManager commandManager = new CommandManager(plugin, "elitemobs");
        commandManager.registerCommand(new SetupCommand());

        assertFalse(eliteMobsCommand.execute(server.getConsoleSender(), "elitemobs", new String[]{"setup"}));
        assertMessageContains(server.getConsoleSender().nextMessage(), "This command must be run as a player!");

        PlayerMock deniedPlayer = server.addPlayer("DeniedSetup");
        deniedPlayer.addAttachment(plugin, "elitemobs.setup", false);

        assertFalse(eliteMobsCommand.execute(deniedPlayer, "elitemobs", new String[]{"setup"}));
        assertMessageContains(deniedPlayer.nextMessage(), "You do not have permission to run this command!");
    }

    @Test
    void commandRoutingReportsUnknownCommandsAndOffersPrefixSuggestions() {
        CommandManager commandManager = new CommandManager(plugin, "elitemobs");
        commandManager.registerCommand(new SetupCommand());
        commandManager.registerCommand(new DiscordCommand());

        PlayerMock player = server.addPlayer("Typo");
        player.addAttachment(plugin, "elitemobs.setup", true);
        player.addAttachment(plugin, "elitemobs.discord.link", true);

        assertFalse(eliteMobsCommand.execute(player, "elitemobs", new String[]{"disc"}));
        assertMessageContains(player.nextMessage(), "Unknown command! Did you mean one of the following?");
        assertMessageContains(player.nextMessage(), "/em discord");
    }

    @Test
    void versionCommandRequiresPermissionThenReportsPluginVersion() {
        CommandManager commandManager = new CommandManager(plugin, "elitemobs");
        commandManager.registerCommand(new VersionCommand());

        PlayerMock deniedPlayer = server.addPlayer("DeniedVersion");
        deniedPlayer.addAttachment(plugin, "elitemobs.version", false);

        assertFalse(eliteMobsCommand.execute(deniedPlayer, "elitemobs", new String[]{"version"}));
        assertMessageContains(deniedPlayer.nextMessage(), "You do not have permission to run this command!");

        PlayerMock allowedPlayer = server.addPlayer("AllowedVersion");
        allowedPlayer.addAttachment(plugin, "elitemobs.version", true);

        assertTrue(eliteMobsCommand.execute(allowedPlayer, "elitemobs", new String[]{"version"}));
        String message = stripped(allowedPlayer.nextMessage());
        assertTrue(message.contains("version"));
        assertTrue(message.contains(plugin.getDescription().getVersion()));
    }

    @Test
    void helpCommandListsRegisteredCommandUsagesForConsole() {
        CommandManager commandManager = new CommandManager(plugin, "elitemobs");
        commandManager.registerCommand(new DiscordCommand());
        commandManager.registerCommand(new VersionCommand());
        commandManager.registerCommand(new HelpCommand());

        assertTrue(eliteMobsCommand.execute(server.getConsoleSender(), "elitemobs", new String[]{"help"}));

        List<String> messages = stripAll(nextConsoleMessages(7));
        assertContainsMessage(messages, "Commands:");
        assertContainsMessage(messages, "/em discord");
        assertContainsMessage(messages, "Checks the server's EliteMobs plugin version.");
        assertContainsMessage(messages, "/em help");
    }

    @Test
    void playerOnlyCommandsRejectConsoleBeforeHeavyHandlers() {
        CommandManager commandManager = new CommandManager(plugin, "elitemobs");
        commandManager.registerCommand(new SpawnBossCommand());
        commandManager.registerCommand(new PayCommand());
        commandManager.registerCommand(new ProtectionBypassCommand());
        commandManager.registerCommand(new TransitiveBlocksRegisterCommand());

        assertRequiresPlayer("spawn", "boss", "missing_boss.yml");
        assertRequiresPlayer("pay", "Target", "10");
        assertRequiresPlayer("protection", "bypass");
        assertRequiresPlayer("transitiveBlocks", "register", "missing_boss.yml", "ON_REMOVE");
    }

    private static PluginMock loadPluginWithCommandMetadata() {
        String yaml = """
                name: EliteMobs
                version: 10.7.1
                main: org.mockbukkit.mockbukkit.plugin.PluginMock
                commands:
                  elitemobs:
                    aliases:
                      - em
                  adventurersguild:
                    aliases:
                      - ag
                permissions:
                  elitemobs.command:
                    default: true
                  elitemobs.discord.link:
                    default: true
                  elitemobs.setup:
                    default: op
                """;
        return MockBukkit.loadWith(PluginMock.class, new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }

    private static YamlConfiguration loadRealPluginYml() {
        YamlConfiguration configuration = new YamlConfiguration();
        try (InputStreamReader reader = new InputStreamReader(
                EliteMobsCommandMatrixTest.class.getResourceAsStream("/plugin.yml"),
                StandardCharsets.UTF_8)) {
            configuration.load(reader);
        } catch (Exception exception) {
            throw new AssertionError("Failed to load EliteMobs plugin.yml", exception);
        }
        return configuration;
    }

    private static void assertMessageContains(String message, String expected) {
        assertTrue(stripped(message).contains(expected), () -> "Expected message to contain: " + expected + ", got: " + message);
    }

    private static void assertContainsMessage(List<String> messages, String expected) {
        assertTrue(messages.stream().anyMatch(message -> message.contains(expected)),
                () -> "Expected messages to contain: " + expected + ", got: " + messages);
    }

    private void assertRequiresPlayer(String... args) {
        assertFalse(eliteMobsCommand.execute(server.getConsoleSender(), "elitemobs", args),
                () -> "Expected /em " + String.join(" ", args) + " to reject console senders");
        assertMessageContains(server.getConsoleSender().nextMessage(), "This command must be run as a player!");
    }

    private List<String> nextConsoleMessages(int count) {
        List<String> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(server.getConsoleSender().nextMessage());
        }
        return messages;
    }

    private static List<String> stripAll(List<String> messages) {
        return messages.stream()
                .map(EliteMobsCommandMatrixTest::stripped)
                .toList();
    }

    private static String stripped(String message) {
        return ChatColor.stripColor(message);
    }

    private static void setStaticField(Class<?> owner, String fieldName, Object value) throws Exception {
        Field field = owner.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    @SuppressWarnings("unchecked")
    private static void resetMagmaCore() throws Exception {
        Field instanceField = MagmaCore.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        Field registeredPluginsField = MagmaCore.class.getDeclaredField("registeredPlugins");
        registeredPluginsField.setAccessible(true);
        ((Map<String, ?>) registeredPluginsField.get(null)).clear();

        Field listenerRegistrationsField = MagmaCore.class.getDeclaredField("listenerRegistrations");
        listenerRegistrationsField.setAccessible(true);
        ((java.util.Set<String>) listenerRegistrationsField.get(null)).clear();
    }
}
