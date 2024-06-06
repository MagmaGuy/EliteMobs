package com.magmaguy.elitemobs.powers.scripts.caching;

import com.magmaguy.elitemobs.utils.WarningMessage;
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
        @Getter
        private double x = 0.01;
        @Getter
        private double y = 0.01;
        @Getter
        private double z = 0.01;
        @Getter
        private int amount = 1;
        @Getter
        private int red = 255;
        @Getter
        private int green = 255;
        @Getter
        private int blue = 255;
        @Getter
        private int toRed = 255;
        @Getter
        private int toGreen = 255;
        @Getter
        private int toBlue = 255;
        @Getter
        private Particle particle = Particle.FLAME;
        @Getter
        private double speed = 0.01;
        @Getter
        private Boolean moveToTarget = null;
        @Getter
        private ScriptRelativeVectorBlueprint relativeVectorBlueprint = null;
        private final String filename;

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
                case "x" -> x = parseDouble(key, value, scriptName);
                case "y" -> y = parseDouble(key, value, scriptName);
                case "z" -> z = parseDouble(key, value, scriptName);
                case "amount" -> amount = parseInteger(key, value, scriptName);
                case "particle" -> particle = parseEnum(key, value, Particle.class, scriptName);
                case "speed" -> speed = parseDouble(key, value, scriptName);
                case "red" -> red = parseInteger(key, value, scriptName);
                case "green" -> green = parseInteger(key, value, scriptName);
                case "blue" -> blue = parseInteger(key, value, scriptName);
                case "tored" -> toRed = parseInteger(key, value, scriptName);
                case "togreen" -> toGreen = parseInteger(key, value, scriptName);
                case "toblue" -> toBlue = parseInteger(key, value, scriptName);
                case "movetotarget" -> moveToTarget = parseBoolean(key, value, scriptName);
                case "relativevector" -> relativeVectorBlueprint = new ScriptRelativeVectorBlueprint(scriptName, filename, (Map<String, ?>) value);
                default -> new WarningMessage("Key " + key + " in script " + scriptName + " in file " + filename + " for script particles is not a valid key!");
            }
        }
    }
}
