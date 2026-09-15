package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.WorldFolderResolver;
import com.magmaguy.magmacore.util.WorldBlueprint;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorldBlueprintCompatibilityTest {
    @TempDir Path root;

    @Test void terrainOnlyBlueprintReachesTheModernInstanceDirectory() throws Exception {
        Path data = root.resolve("plugins").resolve("EliteMobs");
        Path region = data.resolve("world_blueprints").resolve("pack").resolve("map").resolve("region").resolve("r.0.0.mca");
        Files.createDirectories(region.getParent());
        Files.write(region, new byte[]{1, 2, 3});
        JavaPlugin previous = MetadataHandler.PLUGIN;
        JavaPlugin plugin = mock(JavaPlugin.class);
        World primary = mock(World.class);
        when(plugin.getDataFolder()).thenReturn(data.toFile());
        when(primary.getName()).thenReturn("world");
        when(primary.getWorldFolder()).thenReturn(root.resolve("world").resolve("dimensions").resolve("minecraft").resolve("overworld").toFile());
        MetadataHandler.PLUGIN = plugin;
        try (var bukkit = mockStatic(Bukkit.class); var log = mockStatic(Logger.class)) {
            bukkit.when(Bukkit::getWorldContainer).thenReturn(root.toFile());
            bukkit.when(Bukkit::getWorlds).thenReturn(List.of(primary));
            assertNotNull(WorldInstantiator.cloneWorld("map", "map_1", "pack"));
            assertArrayEquals(Files.readAllBytes(region), Files.readAllBytes(root.resolve("world").resolve("dimensions").resolve("minecraft").resolve("map_1").resolve("region").resolve("r.0.0.mca")));
            assertTrue(Files.isRegularFile(region), "Blueprint source remains intact");
        } finally { MetadataHandler.PLUGIN = previous; }
    }

    @Test void migratedWorldIsRecognizedWithoutDimensionLevelDat() throws Exception {
        Path dimension = root.resolve("world").resolve("dimensions").resolve("minecraft").resolve("map");
        Files.createDirectories(dimension.resolve("data").resolve("paper"));
        Files.write(dimension.resolve("data").resolve("paper").resolve("metadata.dat"), new byte[]{1});
        Files.write(dimension.resolve("data").resolve("paper").resolve("level_overrides.dat"), new byte[]{1});
        World primary = mock(World.class);
        when(primary.getName()).thenReturn("world");
        when(primary.getWorldFolder()).thenReturn(root.resolve("world").resolve("dimensions").resolve("minecraft").resolve("overworld").toFile());
        try (var bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getWorldContainer).thenReturn(root.toFile());
            bukkit.when(Bukkit::getWorlds).thenReturn(List.of(primary));
            assertTrue(WorldFolderResolver.hasModernLayout("map"));
            assertEquals(dimension.toFile(), WorldFolderResolver.resolve("map"));
        }
    }

    @Test void netherTerrainIsMappedForBothStorageLayoutsAndSourceSurvives() throws Exception {
        Path source = root.resolve("source");
        Path file = source.resolve("DIM-1").resolve("region").resolve("r.-1.0.mca");
        Files.createDirectories(file.getParent()); Files.writeString(file, "original terrain");
        var blueprint = WorldBlueprint.inspect(source, World.Environment.NETHER);
        blueprint.copyTo(root.resolve("modern"), World.Environment.NETHER, true);
        blueprint.copyTo(root.resolve("legacy"), World.Environment.NETHER, false);
        assertEquals("original terrain", Files.readString(root.resolve("modern").resolve("region").resolve("r.-1.0.mca")));
        assertEquals("original terrain", Files.readString(root.resolve("legacy").resolve("DIM-1").resolve("region").resolve("r.-1.0.mca")));
        assertEquals("original terrain", Files.readString(file));
        assertThrows(java.io.IOException.class, () -> WorldBlueprint.inspect(source, World.Environment.NORMAL));
    }

    @Test void completeLegacyWorldRetainsItsMetadataAndOmitsIdentity() throws Exception {
        Path source = root.resolve("source");
        Files.createDirectories(source.resolve("DIM1").resolve("region"));
        Files.writeString(source.resolve("DIM1").resolve("region").resolve("r.0.0.mca"), "terrain");
        Files.writeString(source.resolve("level.dat"), "metadata");
        Files.createDirectories(source.resolve("data").resolve("minecraft"));
        Files.writeString(source.resolve("data").resolve("minecraft").resolve("weather.dat"), "namespaced saved data");
        Files.writeString(source.resolve("uid.dat"), "identity");
        Files.writeString(source.resolve("session.lock"), "lock");
        var blueprint = WorldBlueprint.inspect(source, World.Environment.THE_END);
        assertFalse(blueprint.usesModernDestination(true));
        blueprint.copyTo(root.resolve("instance"), World.Environment.THE_END, true);
        assertEquals("metadata", Files.readString(root.resolve("instance").resolve("level.dat")));
        assertEquals("namespaced saved data", Files.readString(root.resolve("instance").resolve("data").resolve("minecraft").resolve("weather.dat")));
        assertTrue(Files.exists(root.resolve("instance").resolve("DIM1").resolve("region").resolve("r.0.0.mca")));
        assertFalse(Files.exists(root.resolve("instance").resolve("uid.dat")));
        assertFalse(Files.exists(root.resolve("instance").resolve("session.lock")));
        assertTrue(Files.exists(source.resolve("uid.dat")));
    }

    @Test void modernExportPreservesSettingsButDoesNotCloneIdentityOrDowngrade() throws Exception {
        Path source = root.resolve("source");
        Files.createDirectories(source.resolve("region")); Files.createDirectories(source.resolve("data").resolve("paper"));
        Files.writeString(source.resolve("region").resolve("r.0.0.mca"), "terrain");
        Files.writeString(source.resolve("data").resolve("paper").resolve("metadata.dat"), "identity");
        Files.writeString(source.resolve("data").resolve("paper").resolve("level_overrides.dat"), "settings");
        var blueprint = WorldBlueprint.inspect(source, World.Environment.NORMAL);
        assertThrows(java.io.IOException.class, () -> blueprint.copyTo(root.resolve("old"), World.Environment.NORMAL, false));
        assertFalse(Files.exists(root.resolve("old")));
        blueprint.copyTo(root.resolve("modern"), World.Environment.NORMAL, true);
        assertFalse(Files.exists(root.resolve("modern").resolve("data").resolve("paper").resolve("metadata.dat")));
        assertEquals("settings", Files.readString(root.resolve("modern").resolve("data").resolve("paper").resolve("level_overrides.dat")));
    }

    @Test void occupiedDestinationAndAmbiguousBlueprintAreRejectedWithoutChangingData() throws Exception {
        Path source = root.resolve("source"); Files.createDirectories(source.resolve("region"));
        Files.writeString(source.resolve("region").resolve("r.0.0.mca"), "terrain");
        Path destination = root.resolve("existing"); Files.createDirectory(destination);
        Files.writeString(destination.resolve("keep"), "player world");
        var blueprint = WorldBlueprint.inspect(source, World.Environment.NORMAL);
        assertThrows(java.io.IOException.class, () -> blueprint.copyTo(destination, World.Environment.NORMAL, true));
        assertEquals("player world", Files.readString(destination.resolve("keep")));
        Files.createDirectories(source.resolve("DIM-1").resolve("region"));
        Files.writeString(source.resolve("DIM-1").resolve("region").resolve("r.0.0.mca"), "other dimension");
        assertThrows(java.io.IOException.class, () -> WorldBlueprint.inspect(source, World.Environment.NETHER));
    }
}
