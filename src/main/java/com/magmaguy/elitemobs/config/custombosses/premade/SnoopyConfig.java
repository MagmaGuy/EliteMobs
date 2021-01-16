package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

/**
 * In the loving memory of Snoopy, the best boy
 * January 2006 - January 2021
 * Gone but not forgotten.
 */
public class SnoopyConfig extends CustomBossConfigFields {
    public SnoopyConfig() {
        super("snoopy",
                EntityType.WOLF.toString(),
                true,
                "&6{&4☠&6} &fSnoopy",
                "dynamic",
                3,
                false,
                10,
                5,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                Arrays.asList("invulnerability_fire.yml"),
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                Arrays.asList(Material.BONE.toString()),
                Arrays.asList("Woof!"),
                Arrays.asList("Woof!"),
                0D,
                false,
                0,
                0D,
                null,
                0);
    }
}
