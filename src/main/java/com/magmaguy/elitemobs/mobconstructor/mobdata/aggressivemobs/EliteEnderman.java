package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteEnderman extends EliteMobProperties {

    public EliteEnderman() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.ENDERMAN).getName());

        this.entityType = EntityType.ENDERMAN;

        this.defaultMaxHealth = 40;

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.ENDERMAN).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
