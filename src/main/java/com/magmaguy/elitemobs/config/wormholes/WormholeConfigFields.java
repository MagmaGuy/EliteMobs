package com.magmaguy.elitemobs.config.wormholes;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import lombok.Getter;
import lombok.Setter;
import wormhole.Wormhole;

public class WormholeConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {

    @Getter
    @Setter
    private String location1;
    @Getter
    @Setter
    private String location2;
    @Getter
    @Setter
    private String location1Text;
    @Getter
    @Setter
    private String location2Text;
    @Getter
    @Setter
    private String permission;
    @Getter
    @Setter
    private Wormhole.WormholeStyle style;
    @Getter
    @Setter
    private int particleColor;
    @Getter
    @Setter
    private double coinCost;
    @Getter
    @Setter
    private boolean blindPlayer;

    public WormholeConfigFields(String filename,
                                boolean isEnabled,
                                String location1,
                                String location2,
                                Wormhole.WormholeStyle style) {
        super(filename, isEnabled);
        this.location1 = location1;
        this.location2 = location2;
        this.style = style;
    }

    public WormholeConfigFields(String filename, boolean isEnabled) {
        super(filename, isEnabled);
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.location1 = processString("location1", location1, null, true);
        this.location1Text = processString("location1Text", location1Text, null, false);
        this.location2 = processString("location2", location2, null, true);
        this.location2Text = processString("location2Text", location2Text, null, false);
        this.permission = processString("permission", permission, null, false);
        this.coinCost = processDouble("coinCost", coinCost, 0, false);
        this.style = processEnum("style", style, Wormhole.WormholeStyle.CUBE, Wormhole.WormholeStyle.class, false);
        this.particleColor = processInt("particleColor", particleColor, 0x800080, false);
        this.blindPlayer = processBoolean("blindPlayer", blindPlayer, false, false);
    }

}
