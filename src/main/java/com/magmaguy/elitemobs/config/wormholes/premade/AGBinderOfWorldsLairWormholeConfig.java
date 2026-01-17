package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGBinderOfWorldsLairWormholeConfig extends WormholeConfigFields {
    public AGBinderOfWorldsLairWormholeConfig() {
        super("ag_binder_of_worlds_wormhole",
                true,
                "em_adventurers_guild,297.5,93,297.5,180,0",
                "shadow_of_the_binder_of_worlds_lair.yml",
                Wormhole.WormholeStyle.CRYSTAL);
        setBlindPlayer(true);
        setLocation1Text("&5『Shadow of the Binder Of Worlds』 &5Lvl 200");
        setParticleColor(0x9600FF);
    }
}
