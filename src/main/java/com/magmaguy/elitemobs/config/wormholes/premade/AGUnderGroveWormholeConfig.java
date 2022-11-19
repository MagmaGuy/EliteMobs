package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGUnderGroveWormholeConfig extends WormholeConfigFields {
    public AGUnderGroveWormholeConfig(){
        super("ag_under_grove_wormhole",
                true,
                "em_adventurers_guild,284.5,108,296.5,0,0",
                "beasts_sanctuary_lair.yml",
                Wormhole.WormholeStyle.CRYSTAL);
        setBlindPlayer(true);
        setLocation1Text("&2『Under Grove Lair』 &cLvl 170");
        setParticleColor(0x9600FF);
    }
}
