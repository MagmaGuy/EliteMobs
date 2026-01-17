package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGSewersWormholeConfig extends WormholeConfigFields {
    public AGSewersWormholeConfig() {
        super("ag_sewers_wormhole",
                true,
                "em_adventurers_guild,297.5,93,258.5,90,0",
                "sewers_minidungeon.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&1『Sewers Minidungeon』 &aLvls 20-35");
        setParticleColor(0xCACBC);
    }
}
