package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGInvasionWormholeConfig extends WormholeConfigFields {
    public AGInvasionWormholeConfig() {
        super("ag_invasion_wormhole",
                true,
                "em_adventurers_guild,284.5,101,296.5,0,0",
                "invasion_minidungeon.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&8『Invasion Minidungeon』 &aLvls 35-45");
        setParticleColor(0x005600);
    }
}
