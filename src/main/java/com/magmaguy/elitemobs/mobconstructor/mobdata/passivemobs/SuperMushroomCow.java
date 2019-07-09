package com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class SuperMushroomCow extends SuperMobProperties {

    public SuperMushroomCow() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.MUSHROOM_COW).getName());

        this.entityType = EntityType.MUSHROOM_COW;

        this.defaultMaxHealth = 10;

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.MUSHROOM_COW).isEnabled();

        if (this.isEnabled) {
            superMobTypeList.add(this.entityType);
            superMobData.add(this);
        }

    }

}
