package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGColosseumWormholeConfig extends WormholeConfigFields {
    public AGColosseumWormholeConfig() {
        super("ag_colosseum_wormhole",
                true,
                "em_adventurers_guild,278.5,72,219.5,-90,0",
                "colosseum_lair.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&6『Colosseum Minidungeon』 &6Lvl 70");
        setParticleColor(0xFFB000);
    }
}
