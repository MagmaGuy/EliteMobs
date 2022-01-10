package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGNorthPoleWormholeConfig  extends WormholeConfigFields {
    public AGNorthPoleWormholeConfig() {
        super("ag_north_pole_wormhole",
                true,
                "em_adventurers_guild,279.5,72,213.5,-45,0",
                "north_pole_minidungeon.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&f『North Pole Minidungeon』 &6Lvl 75");
        setParticleColor(0xFFFFFF);
    }
}
