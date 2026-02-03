package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGPrimisWormholeConfig extends WormholeConfigFields {
    public AGPrimisWormholeConfig() {
        super("ag_primis_wormhole",
                true,
                "em_adventurers_guild,293.5,95,290.5,90,0",
                "primis_adventure.yml",
                Wormhole.WormholeStyle.ICOSAHEDRON);
        setBlindPlayer(true);
        setLocation1Text("<g:#228B22:#32CD32>『Primis Adventure』</g> <g:#98FB98:#90EE90>Lvls 0-20</g>");
        setParticleColor(0x008000);
        setSizeMultiplier(2);
    }
}

