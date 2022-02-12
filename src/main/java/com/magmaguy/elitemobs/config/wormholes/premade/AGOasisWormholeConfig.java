package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGOasisWormholeConfig extends WormholeConfigFields {
    public AGOasisWormholeConfig() {
        super("ag_oasis_wormhole",
                true,
                "em_adventurers_guild,290.5,99.5,311.5,-133,0",
                "oasis_adventure.yml",
                Wormhole.WormholeStyle.ICOSAHEDRON);
        setBlindPlayer(true);
        setLocation1Text("&2『Oasis Adventure』 &aLvls 20-55");
        setParticleColor(0xFFFF00);
    }
}
