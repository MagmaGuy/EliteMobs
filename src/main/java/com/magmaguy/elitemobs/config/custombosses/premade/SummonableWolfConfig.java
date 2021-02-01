package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class SummonableWolfConfig extends CustomBossConfigFields {
    public SummonableWolfConfig() {
        super("summonable_wolf",
                EntityType.WOLF.toString(),
                true,
                "$normalLevel Wolfy",
                "dynamic",
                3,
                false,
                3,
                1,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                null,
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
