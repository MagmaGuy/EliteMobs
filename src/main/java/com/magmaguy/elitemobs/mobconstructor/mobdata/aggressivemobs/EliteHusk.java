package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteHusk extends EliteMobProperties {

    public EliteHusk() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.HUSK).getName());

        this.entityType = EntityType.HUSK;

        this.defaultMaxHealth = 20;

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.HUSK).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
