package com.magmaguy.elitemobs.npcs.scripts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class NPCScriptDefinitionTest {

    @TempDir
    Path tempDir;

    @Test
    void validatesNpcHooks() {
        NPCScriptDefinition definition = NPCScriptDefinition.validate("wave.lua", tempDir.resolve("wave.lua").toFile(), """
                return {
                  api_version = 1,
                  priority = 4,
                  on_spawn = function(context) end,
                  on_remove = function(context) end,
                  on_game_tick = function(context) end,
                  on_npc_interact = function(context) end,
                  on_npc_proximity_enter = function(context) end,
                  on_npc_proximity_leave = function(context) end
                }
                """);

        assertEquals("wave.lua", definition.getFileName());
        assertEquals(4, definition.getPriority());
        assertTrue(definition.getHooks().contains(NPCScriptHook.ON_SPAWN));
        assertTrue(definition.getHooks().contains(NPCScriptHook.ON_REMOVE));
        assertTrue(definition.getHooks().contains(NPCScriptHook.ON_TICK));
        assertTrue(definition.getHooks().contains(NPCScriptHook.ON_INTERACT));
        assertTrue(definition.getHooks().contains(NPCScriptHook.ON_PROXIMITY_ENTER));
        assertTrue(definition.getHooks().contains(NPCScriptHook.ON_PROXIMITY_LEAVE));
    }

    @Test
    void rejectsMissingApiVersion() {
        assertThrows(IllegalArgumentException.class, () -> NPCScriptDefinition.validate("broken.lua", tempDir.resolve("broken.lua").toFile(), """
                return {
                  on_npc_proximity_enter = function(context) end
                }
                """));
    }

    @Test
    void rejectsUnsupportedApiVersion() {
        assertThrows(IllegalArgumentException.class, () -> NPCScriptDefinition.validate("broken.lua", tempDir.resolve("broken.lua").toFile(), """
                return {
                  api_version = 2,
                  on_npc_proximity_enter = function(context) end
                }
                """));
    }

    @Test
    void rejectsUnknownTopLevelFields() {
        assertThrows(IllegalArgumentException.class, () -> NPCScriptDefinition.validate("broken.lua", tempDir.resolve("broken.lua").toFile(), """
                return {
                  api_version = 1,
                  powers = {},
                  on_npc_proximity_enter = function(context) end
                }
                """));
    }
}
