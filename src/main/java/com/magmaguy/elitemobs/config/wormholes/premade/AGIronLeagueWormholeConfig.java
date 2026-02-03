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
        setLocation1Text("<g:#C0C0C0:#FFFFFF>『Iron League Arena』</g> <g:#98FB98:#90EE90>Lvl 50</g>");
        setLocation2Text("<g:#FFD700:#FFA500>『Adventurer's Guild』</g>");
        setParticleColor(0);
        setBlindPlayer(true);
    }
}
