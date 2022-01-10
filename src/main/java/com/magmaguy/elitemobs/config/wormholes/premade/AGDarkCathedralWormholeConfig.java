package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGDarkCathedralWormholeConfig extends WormholeConfigFields {
    public AGDarkCathedralWormholeConfig() {
        super("ag_dark_cathedral_wormhole",
                true,
                "em_adventurers_guild,285.5,72,212.5,0,0",
                "colosseum_lair.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&0『Dark Cathedral Lair』 &6Lvl 75");
        setParticleColor(0x000000);
    }
}
