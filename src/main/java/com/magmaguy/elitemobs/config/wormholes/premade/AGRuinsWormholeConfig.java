package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGRuinsWormholeConfig extends WormholeConfigFields {
    public AGRuinsWormholeConfig() {
        super("ag_ruins_wormhole",
                true,
                "em_adventurers_guild,290.5,107.5,311.5,-135,0",
                "the_ruins.yml",
                Wormhole.WormholeStyle.CRYSTAL);
        setBlindPlayer(true);
        setLocation1Text("&8『Ruins Lair』 &cLvl 150");
        setParticleColor(0x141414);
    }
}
