package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGSewersWormholeConfig extends WormholeConfigFields {
    public AGSewersWormholeConfig() {
        super("ag_sewers_wormhole",
                true,
                "em_adventurers_guild,285.5,77,226.5,-180,0",
                "sewers_minidungeon.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&1『Sewers Minidungeon』 &aLvls 5-70");
        setParticleColor(0xCACBC);
    }
}
