package com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class SuperCow extends SuperMobProperties {

    public SuperCow() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.COW).getName());

        this.entityType = EntityType.COW;

        this.defaultMaxHealth = 10;

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.COW).isEnabled();

        if (this.isEnabled) {
            superMobTypeList.add(this.entityType);
            superMobData.add(this);
        }

    }

}
