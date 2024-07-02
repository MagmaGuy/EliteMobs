package com.magmaguy.elitemobs.items.potioneffects;

import com.magmaguy.elitemobs.config.LegacyValueConverter;
import com.magmaguy.elitemobs.config.potioneffects.PotionEffectsConfig;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;

public class ElitePotionEffect {

    private PotionEffect potionEffect;
    private Target target;
    private ApplicationMethod applicationMethod;
    private double value;

    public ElitePotionEffect(String string) {
        try {
            String[] stringObject = string.split(",");
            int duration = 2 * 20;
            PotionEffectType potionEffectType = Registry.EFFECT.get(new NamespacedKey("minecraft", LegacyValueConverter.parsePotionEffect(stringObject[0]).toLowerCase(Locale.ROOT)));
            if (potionEffectType == null) {
                new WarningMessage("Failed to get valid potion effect for " + stringObject[0].toLowerCase(Locale.ROOT));
                return;
            }
            if (potionEffectType.equals(PotionEffectType.NIGHT_VISION))
                duration = 15 * 20;
            this.potionEffect = new PotionEffect(potionEffectType, duration, Integer.parseInt(stringObject[1]));
            this.value = PotionEffectsConfig.getPotionEffect(potionEffectType.getKey().getKey()).getValue();
            if (stringObject.length < 3) {
                this.target = Target.SELF;
                this.applicationMethod = ApplicationMethod.CONTINUOUS;
                return;
            }

            this.target = Target.valueOf(stringObject[2].toUpperCase(Locale.ROOT));

            if (stringObject.length < 4) {
                applicationMethod = ApplicationMethod.CONTINUOUS;
                return;
            }

            this.applicationMethod = ApplicationMethod.valueOf(stringObject[3].toUpperCase(Locale.ROOT));

            if (this.applicationMethod.equals(ApplicationMethod.ONHIT))
                this.potionEffect = new PotionEffect(potionEffectType,
                        PotionEffectsConfig.getPotionEffect(potionEffect.getType().getKey().getKey()).getOnHitDuration() * 20,
                        Integer.parseInt(stringObject[1]));

        } catch (Exception ex) {
            new WarningMessage("Detected invalid potion effect entry: " + string);
            ex.printStackTrace();
        }
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
