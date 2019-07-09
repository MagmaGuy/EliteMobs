package com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class SuperPig extends SuperMobProperties {

    public SuperPig() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.PIG).getName());

        this.entityType = EntityType.PIG;

        this.defaultMaxHealth = 10;

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.PIG).isEnabled();

        if (this.isEnabled) {
            superMobTypeList.add(this.entityType);
            superMobData.add(this);
        }

    }

}
