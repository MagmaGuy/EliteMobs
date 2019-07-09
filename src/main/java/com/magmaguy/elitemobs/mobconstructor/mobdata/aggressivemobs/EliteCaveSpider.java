package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteCaveSpider extends EliteMobProperties {

    public EliteCaveSpider() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.CAVE_SPIDER).getName());

        this.entityType = EntityType.CAVE_SPIDER;

        this.defaultMaxHealth = 12;

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.CAVE_SPIDER).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
