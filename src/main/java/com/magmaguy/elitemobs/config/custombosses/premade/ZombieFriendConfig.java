package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

public class ZombieFriendConfig extends CustomBossesConfigFields {
    public ZombieFriendConfig() {
        super("zombie_friends_friend",
                EntityType.ZOMBIE,
                true,
                "$reinforcementLevel &7Zombie Friend",
                "dynamic");
        setHealthMultiplier(0.5);
        setDamageMultiplier(0.5);
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
    }
}
