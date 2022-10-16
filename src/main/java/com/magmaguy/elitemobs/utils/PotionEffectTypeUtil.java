package com.magmaguy.elitemobs.utils;

import lombok.Getter;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectTypeUtil {

    private static PotionEffectType getPotionEffectType(PotionEffectTypeEnum potionEffectTypeEnum) {
        return PotionEffectType.getByName(potionEffectTypeEnum.name());
    }

    public static PotionEffectType getByKey(String key) {
        for (PotionEffectTypeEnum potionEffectTypeEnum : PotionEffectTypeEnum.values())
            if (potionEffectTypeEnum.getKey().equals(key.toLowerCase()))
                return PotionEffectType.getByName(potionEffectTypeEnum.name());
        return null;
    }

    public enum PotionEffectTypeEnum {
        SPEED("speed"),
        SLOW("slowness"),
        FAST_DIGGING("haste"),
        SLOW_DIGGING("mining_fatigue"),
        INCREASE_DAMAGE("strength"),
        HEAL("instant_health"),
        HARM("instant_damage"),
        JUMP("jump_boost"),
        CONFUSION("nausea"),
        REGENERATION("regeneration"),
        DAMAGE_RESISTANCE("resistance"),
        FIRE_RESISTANCE("fire_resistance"),
        WATER_BREATHING("water_breathing"),
        INVISIBILITY("invisibility"),
        BLINDNESS("blindness"),
        NIGHT_VISION("night_vision"),
        HUNGER("hunger"),
        WEAKNESS("weakness"),
        POISON("poison"),
        WITHER("wither"),
        HEALTH_BOOST("health_boost"),
        ABSORPTION("absorption"),
        SATURATION("saturation"),
        GLOWING("glowing"),
        LEVITATION("levitation"),
        LUCK("luck"),
        UNLUCK("unluck"),
        SLOW_FALLING("slow_falling"),
        CONDUIT_POWER("conduit_power"),
        DOLPHINS_GRACE("dolphins_grace"),
        BAD_OMEN("bad_omen"),
        HERO_OF_THE_VILLAGE("hero_of_the_village"),
        DARKNESS("darkness");

        @Getter
        private final String key;

        PotionEffectTypeEnum(String key) {
            this.key = key;
        }
    }
}
