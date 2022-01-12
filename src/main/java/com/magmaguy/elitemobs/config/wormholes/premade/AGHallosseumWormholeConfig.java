package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGHallosseumWormholeConfig extends WormholeConfigFields {
    public AGHallosseumWormholeConfig() {
        super("ag_hallosseum_wormhole",
                true,
                "em_adventurers_guild,290.5,101.5,293.5,-40,0",
                "hallosseum_lair.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&4『Hallosseum Lair』 &aLvl 50");
        setParticleColor(0x680000);
    }
}
