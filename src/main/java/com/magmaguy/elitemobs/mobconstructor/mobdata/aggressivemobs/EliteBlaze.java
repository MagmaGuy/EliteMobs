package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteBlaze extends EliteMobProperties {

    public EliteBlaze() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.BLAZE).getName());

        this.entityType = EntityType.BLAZE;

        this.defaultMaxHealth = 20;

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.BLAZE).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
