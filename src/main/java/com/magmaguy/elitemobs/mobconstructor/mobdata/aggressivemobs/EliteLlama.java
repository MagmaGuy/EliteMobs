package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteLlama extends EliteMobProperties {
    public EliteLlama() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.LLAMA).getName();
        this.entityType = EntityType.LLAMA;
        this.defaultMaxHealth = 30;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.LLAMA).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.LLAMA).isEnabled();
        eliteMobData.add(this);
    }
}
