package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGKnightsCastleWormholeConfig extends WormholeConfigFields {
    public AGKnightsCastleWormholeConfig(){
        super("ag_knights_castle_lair_wormhole",
                true,
                "em_adventurers_guild,281.5,104.5,302.5,-45,0",
                "the_knight_castle.yml",
                Wormhole.WormholeStyle.ICOSAHEDRON);
        setLocation1Text("&7『Knight's Castle』 &aLvl 95");
        setLocation2Text("&f『Adventurers Guild』");
        setParticleColor(0);
        setBlindPlayer(true);
    }
}
