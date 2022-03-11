package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGFireworksWormholeConfig extends WormholeConfigFields {
    public AGFireworksWormholeConfig() {
        super("ag_fireworks_wormhole",
                true,
                "em_adventurers_guild,296.5,99,308.5,-174,0",
                "fireworks_lair.yml",
                Wormhole.WormholeStyle.CRYSTAL);
        setBlindPlayer(true);
        setLocation1Text("&6『Fireworks Lair』 &aLvl 20");
        setParticleColor(0xFFE100);
    }
}
