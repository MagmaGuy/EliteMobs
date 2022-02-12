package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGPrimisWormholeConfig extends WormholeConfigFields {
    public AGPrimisWormholeConfig() {
        super("ag_primis_wormhole",
                true,
                "em_adventurers_guild,290.5,98,302.5,109,0",
                "primis_adventure.yml",
                Wormhole.WormholeStyle.ICOSAHEDRON);
        setBlindPlayer(true);
        setLocation1Text("&2『Primis Adventure』 &aLvls 0-20");
        setParticleColor(0x008000);
        setSizeMultiplier(2);
    }
}

