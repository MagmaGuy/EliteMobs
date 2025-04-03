package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptFloat;
import com.magmaguy.elitemobs.powers.scripts.primitives.ScriptInteger;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Particle;

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
        private ScriptFloat speed = new ScriptFloat(0.01f);
        @Getter
        private Boolean moveToTarget = null;
        @Getter
        private ScriptRelativeVectorBlueprint relativeVectorBlueprint = null;

        public ScriptParticleBlueprint(Map<?, ?> entry, String scriptName, String filename) {
            this.filename = filename;
            this.scriptName = scriptName;
            processMapList(entry);
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
