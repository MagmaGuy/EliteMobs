package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteDrowned extends EliteMobProperties {

    public EliteDrowned() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.DROWNED).getName());

        this.entityType = EntityType.DROWNED;

        this.defaultMaxHealth = 20;

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.DROWNED).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
