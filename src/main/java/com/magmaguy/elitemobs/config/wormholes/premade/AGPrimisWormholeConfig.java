package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGPrimisWormholeConfig extends WormholeConfigFields {
    public AGPrimisWormholeConfig() {
        super("ag_primis_wormhole",
                true,
                "em_adventurers_guild,292.5,77,219.5,90,0",
                "primis_adventure.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&2『Primis Adventure』 &aLvls 0-15");
        setParticleColor(0x008000);
    }
}

