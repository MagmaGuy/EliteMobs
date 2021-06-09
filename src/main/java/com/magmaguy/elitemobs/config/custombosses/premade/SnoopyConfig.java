package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Collections;

/**
 * In the loving memory of Snoopy, the best boy
 * January 2006 - January 2021
 * Gone but not forgotten.
 */
public class SnoopyConfig extends CustomBossConfigFields {
    public SnoopyConfig() {
        super("snoopy",
                EntityType.WOLF.toString(),
                true,
                "&6{&4☠&6} &fSnoopy",
                "dynamic");
        setTimeout(3);
        setHealthMultiplier(10);
        setDamageMultiplier(5);
        setPowers(Collections.singletonList("invulnerability_fire.yml"));
        setDropsVanillaLoot(false);
        setDropsEliteMobsLoot(false);
        setTrails(Collections.singletonList(Material.BONE.toString()));
        setOnDamagedMessages(Collections.singletonList("Woof!"));
        setOnDamageMessages(Collections.singletonList("Woof!"));
        setSpawnMessage("&aAn extremely rare Snoopy has been sighted!");
        setAnnouncementPriority(3);
    }
}
