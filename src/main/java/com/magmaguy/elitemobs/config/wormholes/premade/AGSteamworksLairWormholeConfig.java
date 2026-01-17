package com.magmaguy.elitemobs.config.wormholes.premade;

import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.wormhole.Wormhole;

public class AGSteamworksLairWormholeConfig extends WormholeConfigFields {
    public AGSteamworksLairWormholeConfig() {
        super("ag_steamworks_lair_wormhole",
                true,
                "em_adventurers_guild,289.5,93,258.5,-90,0",
                "steamworks_lair.yml",
                Wormhole.WormholeStyle.ICOSAHEDRON);
        setLocation1Text("&6『The Steamworks Lair』 &aLvl 140");
        setLocation2Text("&f『Adventurers Guild』");
        setParticleColor(0);
        setBlindPlayer(true);
    }
}
