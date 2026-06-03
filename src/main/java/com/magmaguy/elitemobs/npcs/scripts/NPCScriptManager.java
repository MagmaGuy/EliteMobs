package com.magmaguy.elitemobs.npcs.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class NPCScriptManager {

    private static final Map<String, NPCScriptDefinition> definitions = new LinkedHashMap<>();

    private NPCScriptManager() {
    }

    public static void initialize() {
        definitions.clear();
        Path scriptDirectory = getScriptDirectory();
        try {
            Files.createDirectories(scriptDirectory);
            discoverScripts(scriptDirectory);
        } catch (IOException exception) {
            Logger.warn("Failed to initialize NPC Lua script directory " + scriptDirectory + ".");
            exception.printStackTrace();
        }
    }

    public static Path getScriptDirectory() {
        return MetadataHandler.PLUGIN.getDataFolder().toPath().resolve("npc_scripts");
    }

    public static Map<String, NPCScriptDefinition> discoverScripts(Path scriptDirectory) {
        LinkedHashMap<String, NPCScriptDefinition> discoveredDefinitions = new LinkedHashMap<>();
        File[] files = scriptDirectory.toFile().listFiles();
        if (files == null) {
            return discoveredDefinitions;
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        for (File file : files) {
            if (file.isDirectory()) continue;
            if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".lua")) {
                continue;
            }
            try {
                NPCScriptDefinition definition = loadNPCScript(file.getName(), file);
                discoveredDefinitions.put(file.getName(), definition);
                definitions.put(file.getName(), definition);
            } catch (IOException exception) {
                Logger.warn("Failed to read NPC Lua script file " + file.getName() + ".");
            } catch (Exception exception) {
                Logger.warn("Failed to load NPC Lua script file " + file.getName() + ".");
                exception.printStackTrace();
            }
        }
        return discoveredDefinitions;
    }

    public static NPCScriptDefinition loadNPCScript(String registryKey, File file) throws IOException {
        String source = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        source = source.replace("\r", "");
        return NPCScriptDefinition.validate(registryKey, file, source);
    }

    public static NPCScriptDefinition getDefinition(String fileName) {
        return definitions.get(fileName);
    }

    public static void shutdown() {
        definitions.clear();
    }
}
