package com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import org.bukkit.entity.EntityType;

public class SuperSheep extends SuperMobProperties {

    public SuperSheep() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.SHEEP).getName());

        this.entityType = EntityType.SHEEP;

        this.defaultMaxHealth = 8;

        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.SHEEP).isEnabled();

        if (this.isEnabled) {
            superMobTypeList.add(this.entityType);
            superMobData.add(this);
        }

    }

}
