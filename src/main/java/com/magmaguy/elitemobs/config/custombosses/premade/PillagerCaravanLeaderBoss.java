package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.Arrays;

public class PillagerCaravanLeaderBoss extends CustomBossesConfigFields {
    public PillagerCaravanLeaderBoss() {
        super("pillager_caravan_leader",
                EntityType.PILLAGER,
                true,
                "$eventBossLevel &cPillager Caravan Leader",
                "dynamic");
        setPersistent(true);
        setPowers(Arrays.asList("bonus_loot.yml", "arrow_fireworks.yml", "arrow_rain.yml",
                "summonable:summonType=ON_COMBAT_ENTER:filename=pillager_caravan_guard.yml:spawnNearby=true:inheritLevel=true",
                "summonable:summonType=ON_COMBAT_ENTER:filename=pillager_caravan_guard.yml:spawnNearby=true:inheritLevel=true"));
        setMountedEntity("pillager_caravan_beast.yml");
        setFollowDistance(100);
        setDamageMultiplier(2);
        setHealthMultiplier(2);
        setUniqueLootList(Arrays.asList("summon_merchant_scroll.yml:0.5",
                "summon_merchant_scroll.yml:0.5",
                "summon_merchant_scroll.yml:0.5",
                "summon_merchant_scroll.yml:0.5",
                "summon_merchant_scroll.yml:0.5"));
    }
}
