package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGInvasionWormholeConfig extends WormholeConfigFields {
    public AGInvasionWormholeConfig() {
        super("ag_invasion_wormhole",
                true,
                "em_adventurers_guild,282.5,77,226.5,-156,0",
                "invasion_minidungeon.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&8『Invasion Minidungeon』 &aLvls 35-55");
        setParticleColor(0x005600);
    }
}
