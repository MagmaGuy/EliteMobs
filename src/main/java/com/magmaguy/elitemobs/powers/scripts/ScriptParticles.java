package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.utils.shapes.Shape;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.magmaguy.elitemobs.utils.MapListInterpreter.*;

public class ScriptParticles {

    private final List<ScriptParticle> scriptParticles = new ArrayList<>();

    public ScriptParticles(List<Map<?, ?>> entries, String scriptName) {
        for (Map<?, ?> entry : entries)
            scriptParticles.add(new ScriptParticle(entry, scriptName));
    }

    public void visualize(Shape shape) {
        scriptParticles.forEach(scriptParticle -> scriptParticle.visualize(shape));
    }

    private class ScriptParticle {
        private final String scriptName;
        private double x = 0.01;
        private double y = 0.01;
        private double z = 0.01;
        private int amount = 1;
        private Particle particle = Particle.FLAME;
        private double speed = 0.01;

        public ScriptParticle(Map<?, ?> entry, String scriptName) {
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
            switch (key.toLowerCase()) {
                case "x" -> x = parseDouble(key, value, scriptName);
                case "y" -> y = parseDouble(key, value, scriptName);
                case "z" -> z = parseDouble(key, value, scriptName);
                case "amount" -> amount = parseInteger(key, value, scriptName);
                case "particle" -> particle = parseEnum(key, value, Particle.class, scriptName);
                case "speed" -> speed = parseInteger(key, value, scriptName);
            }
        }

        private void visualize(Shape shape) {
            shape.visualize(particle, amount, x, y, z, speed);
        }
    }
}
