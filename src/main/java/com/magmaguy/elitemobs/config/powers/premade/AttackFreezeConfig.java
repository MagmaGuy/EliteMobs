package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackFreezeConfig extends PowersConfigFields {
    public static String freezeMessage;
    public AttackFreezeConfig() {
        super("attack_freeze",
                true,
                "Cryomancer",
                Material.PACKED_ICE.toString());
    }

    @Override
    public void processAdditionalFields(){
        freezeMessage = ConfigurationEngine.setString(fileConfiguration, "freezeMessage", "&8[EM] &9You've been frozen!");
    }
}