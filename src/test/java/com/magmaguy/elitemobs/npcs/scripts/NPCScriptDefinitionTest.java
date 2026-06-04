package com.magmaguy.elitemobs.npcs.scripts;

import com.magmaguy.magmacore.scripting.ScriptDefinition;
import com.magmaguy.magmacore.scripting.ScriptHook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that NPC Lua scripts parse against the shared Magmacore scripting runtime via
 * {@link EliteMobsNPCScriptProvider}. NPC scripts no longer have a bespoke definition/hook
 * type — they reuse Magmacore's {@link ScriptDefinition} and {@link ScriptHook}, which is what
 * lets them inherit the full scripting surface (context.world, zones, scheduler, ...).
 */
class NPCScriptDefinitionTest {

    @TempDir
    Path tempDir;

    private ScriptDefinition validate(String fileName, String source) {
        return ScriptDefinition.validate(fileName, tempDir.resolve(fileName).toFile(), source,
                new EliteMobsNPCScriptProvider(tempDir));
    }

    @Test
    void validatesNpcHooks() {
        ScriptDefinition definition = validate("wave.lua", """
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
        assertTrue(definition.getHooks().contains(ScriptHook.ON_SPAWN));
        assertTrue(definition.getHooks().contains(ScriptableNPC.ON_REMOVE));
        assertTrue(definition.getHooks().contains(ScriptHook.ON_TICK));
        assertTrue(definition.getHooks().contains(ScriptableNPC.ON_INTERACT));
        assertTrue(definition.getHooks().contains(ScriptableNPC.ON_PROXIMITY_ENTER));
        assertTrue(definition.getHooks().contains(ScriptableNPC.ON_PROXIMITY_LEAVE));
    }

    @Test
    void rejectsMissingApiVersion() {
        assertThrows(IllegalArgumentException.class, () -> validate("broken.lua", """
                return {
                  on_npc_proximity_enter = function(context) end
                }
                """));
    }

    @Test
    void rejectsUnsupportedApiVersion() {
        assertThrows(IllegalArgumentException.class, () -> validate("broken.lua", """
                return {
                  api_version = 2,
                  on_npc_proximity_enter = function(context) end
                }
                """));
    }

    @Test
    void rejectsUnknownTopLevelFields() {
        assertThrows(IllegalArgumentException.class, () -> validate("broken.lua", """
                return {
                  api_version = 1,
                  powers = {},
                  on_npc_proximity_enter = function(context) end
                }
                """));
    }
}
