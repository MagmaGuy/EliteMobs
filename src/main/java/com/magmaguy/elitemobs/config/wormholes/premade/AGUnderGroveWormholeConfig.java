package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGUnderGroveWormholeConfig extends WormholeConfigFields {
    public AGUnderGroveWormholeConfig() {
        super("ag_under_grove_wormhole",
                true,
                "em_adventurers_guild,289.5,93,263.5,-90,0",
                "under_grove_lair.yml",
                Wormhole.WormholeStyle.CRYSTAL);
        setBlindPlayer(true);
        setLocation1Text("&2『Under Grove Lair』 &cLvl 170");
        setParticleColor(0x9600FF);
    }
}
