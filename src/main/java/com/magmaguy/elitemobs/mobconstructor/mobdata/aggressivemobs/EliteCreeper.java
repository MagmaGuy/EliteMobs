package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.powers.defensivepowers.Invisibility;
import org.bukkit.entity.EntityType;

public class EliteCreeper extends EliteMobProperties {

    public EliteCreeper() {

        this.name = ChatColorConverter.convert(MobPropertiesConfig.getMobProperties().get(EntityType.CREEPER).getName());

        this.entityType = EntityType.CREEPER;

        this.defaultMaxHealth = 20;

        this.validDefensivePowers.addAll(super.getAllDefensivePowers());
        this.validOffensivePowers.addAll(super.getAllOffensivePowers());
        this.validMiscellaneousPowers.addAll(super.getAllMiscellaneousPowers());

//        todo: add a way to configure powers per entity type
        ElitePower elitePower = null;
        for (ElitePower elitePower1 : this.validDefensivePowers)
            if (elitePower1 instanceof Invisibility)
                elitePower = elitePower1;

        if (elitePower != null)
            this.validDefensivePowers.remove(elitePower);

        isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.CREEPER).isEnabled();

        if (this.isEnabled)
            eliteMobData.add(this);

    }

}
