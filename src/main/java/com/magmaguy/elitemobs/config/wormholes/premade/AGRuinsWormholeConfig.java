package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGRuinsWormholeConfig extends WormholeConfigFields {
    public AGRuinsWormholeConfig() {
        super("ag_ruins_wormhole",
                true,
                "em_adventurers_guild,291.5,67,213.5,45,0",
                "the_ruins.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&8『Ruins Lair』 &cLvl 150");
        setParticleColor(0x141414);
    }
}
