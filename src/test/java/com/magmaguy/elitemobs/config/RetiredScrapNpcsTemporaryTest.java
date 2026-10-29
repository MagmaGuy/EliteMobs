package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerDataLoadedEvent;
import com.magmaguy.elitemobs.commands.admin.NPCCommands;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.RetiredNPCObjectives;
import com.magmaguy.elitemobs.quests.objectives.DialogObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.magmacore.MagmaCore;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

/** temp.elitemobs.retired-scrap-npcs: scrap NPC roles stay switched off and nothing about them can block a quest. */
@EnabledIfEnvironmentVariable(named = "NIGHTBREAK_TEMP_TICKET_TESTS", matches = "true")
class RetiredScrapNpcsTemporaryTest {
    private ServerMock server;
    private JavaPlugin plugin;
    private MagmaCore previousCore;
    private HashMap<String, NPCsConfigFields> previousNpcs;

    public static class ResourcePlugin extends JavaPlugin {
        @Override public InputStream getResource(String name) {
            return RetiredScrapNpcsTemporaryTest.class.getClassLoader().getResourceAsStream(name);
        }
    }

    @BeforeEach void open() throws Exception {
        server = MockBukkit.mock();
        plugin = MockBukkit.loadSimple(ResourcePlugin.class);
        MetadataHandler.PLUGIN = plugin;
        var instance = MagmaCore.class.getDeclaredField("instance");
        instance.setAccessible(true);
        previousCore = MagmaCore.getInstance();
        instance.set(null, null);
        MagmaCore.createInstance(plugin);
        previousNpcs = NPCsConfig.npcEntities;
        NPCsConfig.npcEntities = new HashMap<>();
    }

    @AfterEach void close() throws Exception {
        EntityTracker.getNpcEntities().clear();
        NPCsConfig.npcEntities = previousNpcs;
        var instance = MagmaCore.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, previousCore);
        MockBukkit.unmock();
    }

    @Test void retiredScrapRolesLoadSwitchedOffAndKeepTheirAuthoredRole() throws Exception {
        for (String role : List.of("SCRAPPER", "enhancer", "REFINER", "SMELTER")) {
            var yaml = npcYaml(role);
            var fields = load(role.toLowerCase() + "_npc.yml", yaml);
            assertFalse(fields.isEnabled(), role);
            assertTrue(fields.isRetiredRole(), role);
            assertEquals(NPCInteractions.NPCInteractionType.NONE, fields.getInteractionType(), role);
            assertEquals(role, yaml.getString("interactionType"), "The authored role stays in the file: " + role);
            assertTrue(yaml.getBoolean("isEnabled"), "The file is not switched off on disk: " + role);
        }
        var repairman = load("repairman_config.yml", npcYaml("REPAIRMAN"));
        assertTrue(repairman.isEnabled());
        assertFalse(repairman.isRetiredRole());
        assertEquals(NPCInteractions.NPCInteractionType.REPAIRMAN, repairman.getInteractionType());
    }

    @Test void placingARetiredRoleIsRefused() throws Exception {
        var yaml = npcYaml("SCRAPPER");
        var fields = load("primis_pontis_scrapper.yml", yaml);
        var message = CommandMessagesConfig.class.getDeclaredField("invalidNpcFilenameMessage");
        message.setAccessible(true);
        message.set(null, "invalid npc");
        var world = server.addSimpleWorld("placement");
        PlayerMock player = server.addPlayer();
        player.teleport(world.getSpawnLocation());

        NPCCommands.set(player, "primis_pontis_scrapper.yml");
        assertEquals("invalid npc", player.nextMessage());
        assertFalse(fields.isEnabled());
        assertTrue(yaml.getBoolean("isEnabled"), "Refusing must not write the file");
        assertEquals(List.of(), yaml.getStringList("spawnLocations"));
        assertTrue(EntityTracker.getNpcEntities().isEmpty(), "No NPC was spawned");
    }

    @Test void scrapperObjectivesCompleteOnLoadWhileOtherDialogueWaits() throws Exception {
        load("primis_pontis_scrapper.yml", npcYaml("SCRAPPER"));
        PlayerMock player = server.addPlayer();
        // Kelly's file is gone after the archive, so her objective is recognized by its filename.
        var kelly = new DialogObjective("scrapper_config.yml", "Kelly", "under the main building", List.of("Old scrap line"));
        var reggie = new DialogObjective("repairman_config.yml", "Reggie", "under the main building", List.of());
        var dlcScrapper = new DialogObjective("primis_pontis_scrapper.yml", "Scrapper", "Pontis", List.of());
        Quest welcome = quest(player, kelly, reggie);
        Quest dlc = quest(player, dlcScrapper);
        try (var playerData = mockStatic(PlayerData.class)) {
            playerData.when(() -> PlayerData.getQuests(player.getUniqueId())).thenReturn(new ArrayList<>(List.of(welcome, dlc)));
            new RetiredNPCObjectives().onPlayerDataLoaded(new PlayerDataLoadedEvent(player));

            assertTrue(kelly.isObjectiveCompleted());
            assertTrue(dlcScrapper.isObjectiveCompleted());
            assertFalse(reggie.isObjectiveCompleted(), "Objectives for NPCs that still exist are untouched");
            assertFalse(welcome.getQuestObjectives().isOver());
            assertTrue(dlc.getQuestObjectives().isOver(), "A quest left with only a scrapper objective can be turned in");
            assertNull(player.nextMessage(), "Completing a retired objective shows no dialogue");
            playerData.verify(() -> PlayerData.updateQuestStatus(player.getUniqueId()), times(1));

            new RetiredNPCObjectives().onPlayerDataLoaded(new PlayerDataLoadedEvent(player));
            playerData.verify(() -> PlayerData.updateQuestStatus(player.getUniqueId()), times(1));
        }
    }

    @Test void customItemsThatDeclareRepairScrapAreDisabled() throws Exception {
        var scrap = item("my_scrap.yml", "isEnabled: true\nmaterial: WHITE_DYE\nname: My scrap\n"
                + "consumable:\n  type: repair_scrap\n  tier: 3\n");
        assertFalse(scrap.isEnabled());
        assertNull(scrap.getConsumable());
        var ticket = item("my_ticket.yml", "isEnabled: true\nmaterial: PAPER\nname: My ticket\n"
                + "consumable:\n  type: lucky_ticket\n");
        assertTrue(ticket.isEnabled());
        assertEquals(new ItemConsumables.Definition(ItemConsumables.Type.LUCKY_TICKET), ticket.getConsumable());
    }

    private static YamlConfiguration npcYaml(String role) throws Exception {
        var yaml = new YamlConfiguration();
        yaml.loadFromString("isEnabled: true\nname: Shopkeeper\nrole: <Shop>\ninteractionType: " + role
                + "\nspawnLocation: world,0,64,0,0,0\nspawnLocations: []\n");
        return yaml;
    }

    private static NPCsConfigFields load(String filename, YamlConfiguration yaml) {
        var fields = new NPCsConfigFields(filename, true);
        fields.setFileConfiguration(yaml);
        fields.processConfigFields();
        NPCsConfig.npcEntities.put(fields.getFilename(), fields);
        return fields;
    }

    private static CustomItemsConfigFields item(String filename, String contents) throws Exception {
        var yaml = new YamlConfiguration();
        yaml.loadFromString(contents);
        var fields = new CustomItemsConfigFields(filename, true);
        fields.setFileConfiguration(yaml);
        fields.processConfigFields();
        return fields;
    }

    private static Quest quest(Player player, Objective... objectives) {
        var questObjectives = new QuestObjectives(UUID.randomUUID(), new ArrayList<>(List.of(objectives)));
        var quest = new Quest(player, questObjectives, 1);
        questObjectives.setQuest(quest);
        return quest;
    }
}
