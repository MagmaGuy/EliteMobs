package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGYggdrasilWormholeConfig extends WormholeConfigFields {
    public AGYggdrasilWormholeConfig() {
        super("ag_yggdrasil_wormhole",
                true,
                "em_adventurers_guild,290.5, 102.5, 312.5, -180.0, 0.0",
                "yggdrasil_realm.yml",
                Wormhole.WormholeStyle.CUBE);
        setBlindPlayer(true);
        setLocation1Text("&6『Yggdrasil Realm』 &5Lvls 75-85");
        setParticleColor(0x6A0000);
    }
}
