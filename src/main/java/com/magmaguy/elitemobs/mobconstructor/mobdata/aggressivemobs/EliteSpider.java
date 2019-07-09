package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteSpider extends EliteMobProperties {

    public EliteSpider() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.SPIDER).getName());

        this.entityType = EntityType.SPIDER;

        this.defaultMaxHealth = 16;

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.SPIDER).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
