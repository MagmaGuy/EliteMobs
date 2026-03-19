package com.magmaguy.elitemobs.config.luapowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.lua.LuaPowerManager;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import com.magmaguy.shaded.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class LuaPowersConfig {

    @Getter
    private static LinkedHashMap<String, PowersConfigFields> luaPowers = new LinkedHashMap<>();

    private final LinkedHashMap<String, LuaPowersConfigFields> pendingPremades = new LinkedHashMap<>();
    private final File powersDirectory;

    public LuaPowersConfig() {
        luaPowers = new LinkedHashMap<>();
        powersDirectory = new File(MetadataHandler.PLUGIN.getDataFolder(), "powers");
        if (!powersDirectory.exists() && !powersDirectory.mkdirs()) {
            Logger.warn("Failed to create powers directory for premade Lua powers.");
            return;
        }

        for (LuaPowersConfigFields luaPowersConfigFields : discoverPremades()) {
            pendingPremades.put(luaPowersConfigFields.getFilename().toLowerCase(Locale.ROOT), luaPowersConfigFields);
        }

        directoryCrawler(powersDirectory);
        for (LuaPowersConfigFields luaPowersConfigFields : new ArrayList<>(pendingPremades.values())) {
            initialize(luaPowersConfigFields);
        }
    }

    private List<LuaPowersConfigFields> discoverPremades() {
        Reflections reflections = new Reflections("com.magmaguy.elitemobs.config.luapowers.premade");
        return reflections.getSubTypesOf(LuaPowersConfigFields.class).stream()
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .sorted(Comparator.comparing(Class::getSimpleName))
                .map(clazz -> {
                    try {
                        return clazz.getDeclaredConstructor().newInstance();
                    } catch (Exception exception) {
                        throw new IllegalStateException("Failed to initialize premade Lua power config " + clazz.getName(), exception);
                    }
                })
                .collect(Collectors.toList());
    }

    private void directoryCrawler(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        java.util.Arrays.sort(files, Comparator.comparing(File::getName));
        for (File file : files) {
            if (file.isDirectory()) {
                directoryCrawler(file);
            } else {
                fileInitializer(file);
            }
        }
    }

    private void fileInitializer(File file) {
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".lua")) {
            return;
        }

        LuaPowersConfigFields luaPowersConfigFields = pendingPremades.remove(file.getName().toLowerCase(Locale.ROOT));
        if (luaPowersConfigFields == null) {
            return;
        }

        initialize(luaPowersConfigFields, file);
    }

    private void initialize(LuaPowersConfigFields luaPowersConfigFields) {
        File file = new File(powersDirectory, luaPowersConfigFields.getFilename());
        try {
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
                    Logger.warn("Failed to create directories for premade Lua power " + luaPowersConfigFields.getFilename() + ".");
                    return;
                }
                Files.writeString(file.toPath(), luaPowersConfigFields.getSource(), StandardCharsets.UTF_8);
            }
            initialize(luaPowersConfigFields, file);
        } catch (IOException exception) {
            Logger.warn("Failed to create premade Lua power file " + luaPowersConfigFields.getFilename() + ".");
            exception.printStackTrace();
        }
    }

    private void initialize(LuaPowersConfigFields luaPowersConfigFields, File file) {
        try {
            PowersConfigFields configFields = LuaPowerManager.loadLuaPower(
                    luaPowersConfigFields.getFilename(),
                    file,
                    luaPowersConfigFields.getEffect(),
                    luaPowersConfigFields.getPowerType());

            luaPowers.put(luaPowersConfigFields.getFilename(), configFields);
        } catch (IOException exception) {
            Logger.warn("Failed to read premade Lua power file " + luaPowersConfigFields.getFilename() + ".");
            exception.printStackTrace();
        } catch (Exception exception) {
            Logger.warn("Failed to load premade Lua power " + luaPowersConfigFields.getFilename() + ".");
            exception.printStackTrace();
        }
    }
}
