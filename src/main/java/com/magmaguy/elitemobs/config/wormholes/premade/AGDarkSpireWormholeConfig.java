package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGDarkSpireWormholeConfig extends WormholeConfigFields {
    public AGDarkSpireWormholeConfig() {
        super("ag_dark_spire_wormhole",
                true,
                "em_adventurers_guild,284.5,105,296.5,0,0",
                "dark_spire_minidungeon.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&0『Dark Spire Minidungeon』 &cLvls 100-120");
        setParticleColor(0x000000);
    }
}
