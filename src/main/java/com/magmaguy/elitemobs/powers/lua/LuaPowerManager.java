package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.config.powers.LuaPowerConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields.PowerType;
import com.magmaguy.magmacore.scripting.ScriptDefinition;
import com.magmaguy.magmacore.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public final class LuaPowerManager {

    private static final Map<String, ScriptDefinition> definitions = new LinkedHashMap<>();

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
        return registerLuaPower(registryKey, file, source, effect, powerType).configFields;
    }

    /**
     * Validates source through the canonical EliteMobs script provider and registers the resulting
     * definition under {@code registryKey}. The source file is descriptive only; no file is read.
     */
    public static Registration registerLuaPower(String registryKey,
                                                File sourceFile,
                                                String source,
                                                String effect,
                                                PowerType powerType) {
        Objects.requireNonNull(registryKey, "registryKey");
        Objects.requireNonNull(sourceFile, "sourceFile");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(powerType, "powerType");
        source = source.replace("\r", "");
        // Boss Lua powers now produce a Magmacore ScriptDefinition (the unified runtime), parsed
        // and hook-validated through EliteMobsScriptProvider, instead of the old LuaPowerDefinition.
        ScriptDefinition definition = ScriptDefinition.validate(
                registryKey,
                sourceFile,
                source,
                new EliteMobsScriptProvider(sourceFile.getParentFile().toPath()));
        definitions.put(registryKey, definition);
        return new Registration(
                registryKey,
                definition,
                new LuaPowerConfigFields(registryKey, sourceFile, definition, effect, powerType));
    }

    /** Opaque EliteMobs-owned handle for one exact canonical Lua definition. */
    public static final class Registration implements AutoCloseable {
        private final String registryKey;
        private final ScriptDefinition definition;
        private final LuaPowerConfigFields configFields;
        private boolean closed;

        private Registration(
                String registryKey,
                ScriptDefinition definition,
                LuaPowerConfigFields configFields) {
            this.registryKey = registryKey;
            this.definition = definition;
            this.configFields = configFields;
        }

        public int executionPriority() {
            return definition.getPriority();
        }

        public Set<String> hookKeys() {
            LinkedHashSet<String> hooks = new LinkedHashSet<>();
            definition.getHooks().stream()
                    .map(hook -> hook.getKey())
                    .sorted()
                    .forEach(hooks::add);
            return Collections.unmodifiableSet(hooks);
        }

        public LuaElitePower newPower() {
            if (closed) throw new IllegalStateException("Lua power registration is closed");
            return new LuaElitePower(configFields);
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            definitions.remove(registryKey, definition);
        }
    }
}
