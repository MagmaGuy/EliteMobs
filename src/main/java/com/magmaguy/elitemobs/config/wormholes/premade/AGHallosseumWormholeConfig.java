package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGHallosseumWormholeConfig extends WormholeConfigFields {
    public AGHallosseumWormholeConfig() {
        super("ag_hallosseum_wormhole",
                true,
                "em_adventurers_guild,281.5,100.5,302.5,-46,0",
                "hallosseum_lair.yml",
                Wormhole.WormholeStyle.CRYSTAL);
        setBlindPlayer(true);
        setLocation1Text("&4『Hallosseum Lair』 &aLvl 30");
        setParticleColor(0x680000);
    }
}
