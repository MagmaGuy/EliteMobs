package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class EliteWitch extends EliteMobProperties {

    public EliteWitch() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.WITCH).getName());

        this.entityType = EntityType.WITCH;

        this.defaultMaxHealth = 26;

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.WITCH).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
