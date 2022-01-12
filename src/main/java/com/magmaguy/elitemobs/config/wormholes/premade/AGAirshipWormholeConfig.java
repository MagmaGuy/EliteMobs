package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AGAirshipWormholeConfig extends WormholeConfigFields {
    public AGAirshipWormholeConfig() {
        super("ag_airship_wormhole",
                true,
                "em_adventurers_guild,284.5,101,296.5,0,0",
                "airship_minidungeon.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&f『Airship Minidungeon』 &aLvls 45-75");
        setParticleColor(0xFFFFFF);
    }
}
