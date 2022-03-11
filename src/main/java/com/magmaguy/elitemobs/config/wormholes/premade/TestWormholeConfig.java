package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class TestWormholeConfig extends WormholeConfigFields {
    public TestWormholeConfig() {
        super("test_wormhole",
                false,
                "em_adventurers_guild,213.5,88,239.5,0,0",
                "em_adventurers_guild,276.5,92,232.5,0,0",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
    }
}
