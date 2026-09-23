package com.magmaguy.elitemobs.items.potioneffects;

import com.magmaguy.elitemobs.config.LegacyValueConverter;
import com.magmaguy.elitemobs.config.potioneffects.PotionEffectsConfig;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;

public class ElitePotionEffect {

    private final PotionEffect potionEffect;
    private final Target target;
    private final ApplicationMethod applicationMethod;
    private final double value;

    public ElitePotionEffect(String definition) {
        if (definition == null) throw new IllegalArgumentException("Missing potion definition");
        String[] fields = definition.split(",", -1);
        if (fields.length < 2 || fields.length > 4)
            throw new IllegalArgumentException("Expected effect,amplifier[,target[,applicationMethod]]: " + definition);
        String nativeName = LegacyValueConverter.parsePotionEffect(fields[0].trim()).toLowerCase(Locale.ROOT);
        PotionEffectType type = Registry.EFFECT.get(new NamespacedKey("minecraft", nativeName));
        if (type == null) throw new IllegalArgumentException("Unknown native potion effect: " + nativeName);
        var config = PotionEffectsConfig.getPotionEffect(type.getKey().getKey());
        if (config == null)
            throw new IllegalArgumentException("Native effect " + nativeName + " has no EliteMobs equipment-effect configuration");
        int amplifier = Integer.parseInt(fields[1].trim());
        if (amplifier < 0 || amplifier > 255) throw new IllegalArgumentException("Potion amplifier must be 0..255");
        Target target = fields.length >= 3 ? Target.valueOf(fields[2].trim().toUpperCase(Locale.ROOT)) : Target.SELF;
        ApplicationMethod method = fields.length >= 4
                ? ApplicationMethod.valueOf(fields[3].trim().toUpperCase(Locale.ROOT)) : ApplicationMethod.CONTINUOUS;
        int duration = type.equals(PotionEffectType.NIGHT_VISION) ? 15 * 20 : 2 * 20;
        if (method == ApplicationMethod.ONHIT) duration = Math.multiplyExact(config.getOnHitDuration(), 20);
        if (duration <= 0 || !Double.isFinite(config.getValue()) || config.getValue() < 0)
            throw new IllegalArgumentException("Invalid potion duration or value for " + nativeName);
        this.potionEffect = new PotionEffect(type, duration, amplifier);
        this.target = target;
        this.applicationMethod = method;
        this.value = config.getValue();
    }

    public boolean isEnabled() {
        var config = PotionEffectsConfig.getPotionEffect(potionEffect.getType().getKey().getKey());
        return config != null && config.isEnabled();
    }

    public PotionEffect getPotionEffect() {
        return potionEffect;
    }

    public Target getTarget() {
        return target;
    }

    public ApplicationMethod getApplicationMethod() {
        return applicationMethod;
    }

    public double getValue() {
        return value;
    }

    public enum Target {
        SELF,
        TARGET
    }

    public enum ApplicationMethod {
        ONHIT,
        CONTINUOUS
    }
}
