package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGNorthPoleWormholeConfig extends WormholeConfigFields {
    public AGNorthPoleWormholeConfig() {
        super("ag_north_pole_wormhole",
                true,
                "em_adventurers_guild,299.5,102.5,302.5,-133,0",
                "north_pole_minidungeon.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&f『North Pole Minidungeon』 &6Lvls 55-60");
        setParticleColor(0xFFFFFF);
    }
}
