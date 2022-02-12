package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGPirateShipWormholeConfig extends WormholeConfigFields {
    public AGPirateShipWormholeConfig() {
        super("ag_pirate_ship_wormhole",
                true,
                "em_adventurers_guild,299.5,98.5,302.5,133,0",
                "pirate_ship_minidungeon.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&9『Pirate Ship Minidungeon』 &aLvls 10-20");
        setParticleColor(0x000077);
    }
}
