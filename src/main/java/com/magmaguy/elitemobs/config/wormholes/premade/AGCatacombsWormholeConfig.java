package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGCatacombsWormholeConfig  extends WormholeConfigFields {
    public AGCatacombsWormholeConfig() {
        super("ag_catacombs_wormhole",
                true,
                "em_adventurers_guild,296.5,98,296.5,98,0",
                "catacombs_lair.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&7『Catacombs Lair』 &aLvls 5-15");
        setParticleColor(0x797979);
    }
}
