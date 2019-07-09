package com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class SuperChicken extends SuperMobProperties {

    public SuperChicken() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.CHICKEN).getName());

        this.entityType = EntityType.CHICKEN;

        this.defaultMaxHealth = 4;

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.CHICKEN).isEnabled();

        if (this.isEnabled) {
            superMobTypeList.add(this.entityType);
            superMobData.add(this);
        }

    }

}
