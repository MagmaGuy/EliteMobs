package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGIronLeagueWormholeConfig extends WormholeConfigFields {
    public AGIronLeagueWormholeConfig() {
        super("ag_iron_league_arena_wormhole",
                true,
                "em_adventurers_guild,227.5,94,258.5,0,0",
                "iron_league_arena.yml",
                Wormhole.WormholeStyle.ICOSAHEDRON);
        setLocation1Text("&f『Iron League Arena』 &aLvl 50");
        setLocation2Text("&f『Adventurers Guild』");
        setParticleColor(0);
        setBlindPlayer(true);
    }
}
