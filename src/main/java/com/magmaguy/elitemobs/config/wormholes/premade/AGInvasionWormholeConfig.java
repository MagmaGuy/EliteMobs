package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGInvasionWormholeConfig extends WormholeConfigFields {
    public AGInvasionWormholeConfig() {
        super("ag_invasion_wormhole",
                true,
                "em_adventurers_guild,281.5,100.5,302.5,-46,0",
                "invasion_minidungeon.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&8『Invasion Minidungeon』 &aLvls 35-55");
        setParticleColor(0x005600);
    }
}
