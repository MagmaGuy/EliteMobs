package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGVampireManorWormholeConfig extends WormholeConfigFields {
    public AGVampireManorWormholeConfig() {
        super("ag_vampire_manor_wormhole",
                true,
                "em_adventurers_guild,289.5,93,268.5,-90,0",
                "vampire_manor_lair.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&4『Vampire Manor Lair』 &5Lvls 120-140");
        setParticleColor(0x6A0000);
    }
}
