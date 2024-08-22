package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class PillagerCaravanLeaderBoss extends CustomBossesConfigFields {
    public PillagerCaravanLeaderBoss() {
        super("pillager_caravan_leader",
                EntityType.PILLAGER,
                true,
                "$eventBossLevel &cPillager Caravan Leader",
                "dynamic");
        setPersistent(true);
        setPowers(new ArrayList<>(List.of("bonus_loot.yml", "arrow_fireworks.yml", "arrow_rain.yml",
                "summonable:summonType=ON_COMBAT_ENTER:filename=pillager_caravan_guard.yml:spawnNearby=true:inheritLevel=true",
                "summonable:summonType=ON_COMBAT_ENTER:filename=pillager_caravan_guard.yml:spawnNearby=true:inheritLevel=true")));
        setMountedEntity("pillager_caravan_beast.yml");
        setFollowDistance(100);
        setDamageMultiplier(2);
        setHealthMultiplier(2);
        setUniqueLootList(new ArrayList<>(List.of("summon_merchant_scroll.yml:0.5",
                "summon_merchant_scroll.yml:0.5",
                "summon_merchant_scroll.yml:0.5",
                "summon_merchant_scroll.yml:0.5",
                "summon_merchant_scroll.yml:0.5")));
        setSpawnMessage("&cA pillager caravan has been sighted!");
        majorBossDeathString("The pillager caravan has been vanquished!");
        setLocationMessage("&cPillager Caravan: $distance blocks away!");
        setAnnouncementPriority(2);
    }
}
