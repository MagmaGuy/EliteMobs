package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class ElitePolarBear extends EliteMobProperties {

    public ElitePolarBear() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.POLAR_BEAR).getName());

        this.entityType = EntityType.POLAR_BEAR;

        this.defaultMaxHealth = 30;

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.POLAR_BEAR).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
