package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import wormhole.Wormhole;

public class AdventurersGuildWormholeConfig extends WormholeConfigFields {
    public AdventurersGuildWormholeConfig() {
        super("adventurers_guild_wormhole",
                true,
                "em_adventurers_guild,204.5,89,235.5,-89,0",
                "your_world_here,0.5,64,0.5,0,0",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
    }
}
