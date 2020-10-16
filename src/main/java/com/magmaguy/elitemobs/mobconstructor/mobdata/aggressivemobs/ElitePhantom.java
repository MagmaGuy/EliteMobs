package com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.powers.miscellaneouspowers.GroundPound;
import com.magmaguy.elitemobs.powers.offensivepowers.AttackLightning;
import org.bukkit.entity.EntityType;

public class ElitePhantom extends EliteMobProperties {

    public ElitePhantom() {
        this.name = MobPropertiesConfig.getMobProperties().get(EntityType.PHANTOM).getName();
        this.entityType = EntityType.PHANTOM;
        this.defaultMaxHealth = 20;
        this.baseDamage = MobPropertiesConfig.getMobProperties().get(EntityType.PHANTOM).getBaseDamage();
        this.isEnabled = MobPropertiesConfig.getMobProperties().get(EntityType.PHANTOM).isEnabled();
        removeOffensivePower(new AttackLightning());
        super.removeDefensivePower(new GroundPound());
        if (this.isEnabled)
            eliteMobData.add(this);
    }

}
