package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptFloat;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptInteger;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.*;

public class ScriptParticlesBlueprint {
    @Getter
    private final List<ScriptParticleBlueprint> particleBlueprints = new ArrayList<>();

    public ScriptParticlesBlueprint(List<Map<?, ?>> entries, String scriptName, String filename) {
        for (Map<?, ?> entry : entries)
            particleBlueprints.add(new ScriptParticleBlueprint(entry, scriptName, filename));
    }

    //Empty constructor to avoid having null values for this
    public ScriptParticlesBlueprint() {

    }

    public class ScriptParticleBlueprint {
        @Getter
        private final String scriptName;
        private final String filename;
        @Getter
        private ScriptFloat x = new ScriptFloat(0.01f);
        @Getter
        private ScriptFloat y = new ScriptFloat(0.01f);
        @Getter
        private ScriptFloat z = new ScriptFloat(0.01f);
        @Getter
        private ScriptInteger amount = new ScriptInteger(1);
        @Getter
        private ScriptInteger red = new ScriptInteger(255);
        @Getter
        private ScriptInteger green = new ScriptInteger(255);
        @Getter
        private ScriptInteger blue = new ScriptInteger(255);
        @Getter
        private ScriptInteger toRed = new ScriptInteger(255);
        @Getter
        private ScriptInteger toGreen = new ScriptInteger(255);
        @Getter
        private ScriptInteger toBlue = new ScriptInteger(255);
        @Getter
        private Particle particle = Particle.FLAME;
        @Getter
        private Material material = Material.STONE;
        @Getter
        private ScriptFloat speed = new ScriptFloat(0.01f);
        @Getter
        private Boolean moveToTarget = null;
        @Getter
        private ScriptRelativeVectorBlueprint relativeVectorBlueprint = null;

        //Lazily initialized caches: the blueprint is immutable after parsing, so the particle data
        //objects it produces can be built once and reused for every visualization.
        private transient Class<?> cachedDataType = null;
        private transient BlockData cachedBlockData = null;
        private transient ItemStack cachedItemStack = null;

        public ScriptParticleBlueprint(Map<?, ?> entry, String scriptName, String filename) {
            this.filename = filename;
            this.scriptName = scriptName;
            processMapList(entry);
        }

        /**
         * Cached {@link Particle#getDataType()} for this blueprint's particle.
         */
        public Class<?> getDataType() {
            if (cachedDataType == null) cachedDataType = particle.getDataType();
            return cachedDataType;
        }

        /**
         * Cached block data for particles that require {@link BlockData}, with the same STONE
         * fallback the per-spawn path used.
         */
        public BlockData getBlockData() {
            if (cachedBlockData == null) {
                Material blockMaterial = material;
                if (blockMaterial == null || !blockMaterial.isBlock()) blockMaterial = Material.STONE;
                cachedBlockData = blockMaterial.createBlockData();
            }
            return cachedBlockData;
        }

        /**
         * Cached item stack for particles that require {@link ItemStack}, with the same STONE
         * fallback the per-spawn path used.
         */
        public ItemStack getItemStack() {
            if (cachedItemStack == null) {
                Material itemMaterial = material;
                if (itemMaterial == null || !itemMaterial.isItem() || itemMaterial.isAir()) itemMaterial = Material.STONE;
                cachedItemStack = new ItemStack(itemMaterial);
            }
            return cachedItemStack;
        }

        private void processMapList(Map<?, ?> entry) {
            for (Map.Entry entrySet : entry.entrySet()) {
                String key = (String) entrySet.getKey();
                processKeyAndValue(key, entrySet.getValue());
            }
        }

        private void processKeyAndValue(String key, Object value) {
            switch (key.toLowerCase(Locale.ROOT)) {
                case "x" -> x = parseScriptFloat(key, value, scriptName);
                case "y" -> y = parseScriptFloat(key, value, scriptName);
                case "z" -> z = parseScriptFloat(key, value, scriptName);
                case "amount" -> amount = parseScriptInteger(key, value, scriptName);
                case "particle" -> particle = parseEnum(key, value, Particle.class, scriptName);
                case "material" -> material = parseEnum(key, value, Material.class, scriptName);
                case "speed" -> speed = parseScriptFloat(key, value, scriptName);
                case "red" -> red = parseScriptInteger(key, value, scriptName);
                case "green" -> green = parseScriptInteger(key, value, scriptName);
                case "blue" -> blue = parseScriptInteger(key, value, scriptName);
                case "tored" -> toRed = parseScriptInteger(key, value, scriptName);
                case "togreen" -> toGreen = parseScriptInteger(key, value, scriptName);
                case "toblue" -> toBlue = parseScriptInteger(key, value, scriptName);
                case "movetotarget" -> moveToTarget = parseBoolean(key, value, scriptName);
                case "relativevector" ->
                        relativeVectorBlueprint = new ScriptRelativeVectorBlueprint(scriptName, filename, (Map<String, ?>) value);
                default ->
                        Logger.warn("Key " + key + " in script " + scriptName + " in file " + filename + " for script particles is not a valid key!");
            }
        }
    }
}
