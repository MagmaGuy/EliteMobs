package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGBeastsSanctuaryWormholeConfig extends WormholeConfigFields {
    public AGBeastsSanctuaryWormholeConfig(){
        super("ag_beasts_sanctuary_wormhole",
                true,
                "em_adventurers_guild,296.5,105,296.5,90,0",
                "beasts_sanctuary_lair.yml",
                Wormhole.WormholeStyle.CRYSTAL);
        setBlindPlayer(true);
        setLocation1Text("&5『Beasts Sanctuary Lair』 &cLvl 130");
        setParticleColor(0x9600FF);
    }
}
