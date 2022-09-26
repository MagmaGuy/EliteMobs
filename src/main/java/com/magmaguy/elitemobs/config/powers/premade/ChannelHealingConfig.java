package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.ChannelHealing;

public class ChannelHealingConfig extends PowersConfigFields {
    public ChannelHealingConfig() {
        super("channel_healing",
                true,
                null,
                1,
                0,
                ChannelHealing.class,
                PowerType.MISCELLANEOUS);
    }
}
