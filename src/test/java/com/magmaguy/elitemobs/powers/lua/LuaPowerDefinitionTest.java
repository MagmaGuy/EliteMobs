package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LuaPowerDefinitionTest {

    @TempDir
    Path tempDir;

    @Test
    void validatesSimpleLuaPower() {
        LuaPowerDefinition definition = LuaPowerDefinition.validate("mycoolpower.lua", tempDir.resolve("mycoolpower.lua").toFile(), """
                return {
                  api_version = 1,
                  priority = 5,
                  on_spawn = function(context) end,
                  on_boss_damaged_by_player = function(context) end
                }
                """);

        assertEquals("mycoolpower.lua", definition.getFileName());
        assertEquals(5, definition.getPriority());
        assertTrue(definition.getHooks().contains(LuaPowerHook.ON_SPAWN));
        assertTrue(definition.getHooks().contains(LuaPowerHook.ON_DAMAGED_BY_PLAYER));
    }

    @Test
    void rejectsMissingApiVersion() {
        assertThrows(IllegalArgumentException.class, () -> LuaPowerDefinition.validate("broken.lua", tempDir.resolve("broken.lua").toFile(), """
                return {
                  on_spawn = function(context) end
                }
                """));
    }

    @Test
    void rejectsUnknownTopLevelFields() {
        assertThrows(IllegalArgumentException.class, () -> LuaPowerDefinition.validate("broken.lua", tempDir.resolve("broken.lua").toFile(), """
                return {
                  api_version = 1,
                  id = "not-allowed",
                  on_spawn = function(context) end
                }
                """));
    }

    @Test
    void sandboxHidesUnsafeGlobals() {
        Globals globals = LuaPowerEnvironmentFactory.createGlobals();
        LuaValue result = globals.load("return os == nil and io == nil and debug == nil and package == nil and require == nil", "sandbox").call();
        assertTrue(result.toboolean());
    }

    @Test
    void discoversLuaPowersFromPowerFolder() throws IOException {
        Path powersDir = Files.createDirectory(tempDir.resolve("powers"));
        Path yamlFile = Files.createFile(powersDir.resolve("dummy.yml"));
        Path luaFile = powersDir.resolve("mycoolpower.lua");
        Files.writeString(luaFile, """
                return {
                  api_version = 1,
                  on_spawn = function(context) end
                }
                """, StandardCharsets.UTF_8);

        PowersConfigFields yamlPower = new PowersConfigFields("dummy.yml", true);
        yamlPower.setFile(yamlFile.toFile());

        Map<String, PowersConfigFields> discovered = LuaPowerManager.discoverLuaPowers(java.util.List.of(yamlPower));
        assertTrue(discovered.containsKey("mycoolpower.lua"));
        assertEquals("mycoolpower.lua", discovered.get("mycoolpower.lua").getFilename());
    }

    @Test
    void validatesComplexParityFixture() throws IOException {
        Path fixture = Path.of("src", "test", "resources", "script-fixtures", "master_blacksmith_goblin.lua");
        String source = Files.readString(fixture, StandardCharsets.UTF_8);
        assertFalse(source.contains("context.script.compile"));

        LuaPowerDefinition definition = LuaPowerDefinition.validate("master_blacksmith_goblin.lua",
                tempDir.resolve("master_blacksmith_goblin.lua").toFile(),
                source);

        assertTrue(definition.getHooks().contains(LuaPowerHook.ON_SPAWN));
        assertTrue(definition.getHooks().contains(LuaPowerHook.ON_DAMAGED_BY_PLAYER));
        assertTrue(definition.getHooks().contains(LuaPowerHook.ON_EXIT_COMBAT));
    }

    @Test
    void validatesTreasureHunterTestbedPower() throws IOException {
        Path fixture = Path.of("testbed", "plugins", "EliteMobs", "powers", "treasure_hunter_goblin_test.lua");
        String source = Files.readString(fixture, StandardCharsets.UTF_8);
        assertFalse(source.contains("context.script.compile"));

        LuaPowerDefinition definition = LuaPowerDefinition.validate("treasure_hunter_goblin_test.lua",
                tempDir.resolve("treasure_hunter_goblin_test.lua").toFile(),
                source);

        assertTrue(definition.getHooks().contains(LuaPowerHook.ON_SPAWN));
        assertTrue(definition.getHooks().contains(LuaPowerHook.ON_DAMAGED_BY_PLAYER));
        assertTrue(definition.getHooks().contains(LuaPowerHook.ON_EXIT_COMBAT));
    }
}
