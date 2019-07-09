package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class ZombieFriendsConfig extends PowersConfigFields {
    public ZombieFriendsConfig() {
        super("zombie_friends",
                true,
                "Friends",
                Material.ZOMBIE_HEAD.toString());
    }
}
