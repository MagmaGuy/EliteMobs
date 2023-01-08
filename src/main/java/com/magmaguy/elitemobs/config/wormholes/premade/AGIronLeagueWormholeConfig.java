package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGIronLeagueWormholeConfig extends WormholeConfigFields {
    public AGIronLeagueWormholeConfig() {
        super("ag_iron_league_arena_wormhole",
                true,
                "em_adventurers_guild,246.5,93,275.5,-89,0",
                "iron_league_arena.yml",
                Wormhole.WormholeStyle.ICOSAHEDRON);
        setLocation1Text("&f『Iron League Arena』 &aLvl 50");
        setLocation2Text("&f『Adventurers Guild』");
        setParticleColor(0);
        setBlindPlayer(true);
    }
}
