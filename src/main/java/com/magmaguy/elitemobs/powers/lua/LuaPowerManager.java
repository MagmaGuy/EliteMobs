package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.config.powers.LuaPowerConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields.PowerType;
import com.magmaguy.magmacore.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public final class LuaPowerManager {

    private static final Map<String, LuaPowerDefinition> definitions = new LinkedHashMap<>();

    private LuaPowerManager() {
    }

    public static Map<String, PowersConfigFields> discoverLuaPowers(Collection<PowersConfigFields> loadedYamlPowers) {
        LinkedHashMap<String, PowersConfigFields> discoveredPowers = new LinkedHashMap<>();
        LinkedHashSet<File> powerDirectories = new LinkedHashSet<>();
        for (PowersConfigFields loadedYamlPower : loadedYamlPowers) {
            if (loadedYamlPower.getFile() != null && loadedYamlPower.getFile().getParentFile() != null) {
                powerDirectories.add(loadedYamlPower.getFile().getParentFile());
            }
        }
        for (File powerDirectory : powerDirectories) {
            discoverDirectory(powerDirectory, discoveredPowers);
        }
        return discoveredPowers;
    }

    public static void shutdown() {
        definitions.clear();
    }

    private static void discoverDirectory(File directory, Map<String, PowersConfigFields> discoveredPowers) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        for (File file : files) {
            if (file.isDirectory()) {
                discoverDirectory(file, discoveredPowers);
                continue;
            }
            if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".lua")) {
                continue;
            }
            try {
                discoveredPowers.put(file.getName(), loadLuaPower(file.getName(), file, null, PowerType.MISCELLANEOUS));
            } catch (IOException exception) {
                Logger.warn("Failed to read Lua power file " + file.getName() + ".");
            } catch (Exception exception) {
                Logger.warn("Failed to load Lua power file " + file.getName() + ".");
                exception.printStackTrace();
            }
        }
    }

    public static LuaPowerConfigFields loadLuaPower(String registryKey,
                                                    File file,
                                                    String effect,
                                                    PowerType powerType) throws IOException {
        String source = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        LuaPowerDefinition definition = LuaPowerDefinition.validate(registryKey, file, source);
        definitions.put(registryKey, definition);
        return new LuaPowerConfigFields(registryKey, file, definition, effect, powerType);
    }
}
