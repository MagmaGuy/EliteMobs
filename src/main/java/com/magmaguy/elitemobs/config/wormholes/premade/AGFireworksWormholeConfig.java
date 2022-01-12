package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGFireworksWormholeConfig extends WormholeConfigFields {
    public AGFireworksWormholeConfig() {
        super("ag_fireworks_wormhole",
                true,
                "em_adventurers_guild,296.5,102,296.5,-90,0",
                "fireworks_lair.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&6『Fireworks Lair』 &aLvl 50");
        setParticleColor(0xFFE100);
    }
}
