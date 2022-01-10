package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGVampireManorWormholeConfig  extends WormholeConfigFields {
    public AGVampireManorWormholeConfig() {
        super("ag_vampire_manor_wormhole",
                true,
                "em_adventurers_guild,292.5,62,222.5,113,0",
                "vampire_manor.yml",
                Wormhole.WormholeStyle.CRYSTAL);
        setBlindPlayer(true);
        setLocation1Text("&4『Vampire Manor Lair』 &5Lvls 100-130");
        setParticleColor(0x6A0000);
    }
}
