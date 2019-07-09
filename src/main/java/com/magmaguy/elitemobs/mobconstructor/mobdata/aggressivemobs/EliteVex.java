package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteVex extends EliteMobProperties {

    public EliteVex() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.VEX).getName());

        this.entityType = EntityType.VEX;

        this.defaultMaxHealth = 14;

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.VEX).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
