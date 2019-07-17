package com.magmaguy.elitemobs.items.potioneffects;

import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ElitePotionEffect {

    public enum Target {
        SELF,
        TARGET
    }

    public enum ApplicationMethod {
        ONHIT,
        CONTINUOUS
    }

    private PotionEffect potionEffect;
    private Target target;
    private ApplicationMethod applicationMethod;
    private double value;

    public ElitePotionEffect(String string) {
        try {
            String[] stringObject = string.split(",");
            int duration = 2 * 20;
            if (PotionEffectType.getByName(stringObject[0].toLowerCase()).equals(PotionEffectType.NIGHT_VISION))
                duration = 15 * 20;
            this.potionEffect = new PotionEffect(PotionEffectType.getByName(stringObject[0].toLowerCase()), duration, Integer.parseInt(stringObject[1]));
            this.value = EconomySettingsConfig.getPotionEffectWorth(potionEffect.getType());
            if (stringObject.length < 3) {
                this.target = Target.SELF;
                this.applicationMethod = ApplicationMethod.CONTINUOUS;
                return;
            }

            this.target = Target.valueOf(stringObject[2].toUpperCase());

            if (stringObject.length < 4) {
                applicationMethod = ApplicationMethod.CONTINUOUS;
                return;
            }

            this.applicationMethod = ApplicationMethod.valueOf(stringObject[3].toUpperCase());

        } catch (Exception ex) {
            new WarningMessage("Detected invalid potion effect entry: " + string);
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
}
