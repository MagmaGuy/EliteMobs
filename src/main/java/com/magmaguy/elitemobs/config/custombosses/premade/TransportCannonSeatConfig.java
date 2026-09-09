package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Map;

/** Default cannon seat is an ordinary editable boss definition, including its invisibility script. */
public final class TransportCannonSeatConfig extends CustomBossesConfigFields {
    public TransportCannonSeatConfig() {
        super("transport_cannon_seat", EntityType.PIG, true, "", "1");
        setDropsEliteMobsLoot(false);
        setDropsVanillaLoot(false);
        setDropsRandomLoot(false);
        setDropsSkillXP(false);
        setAi(true);
    }

    @Override
    public void processConfigFields() {
        // Premades are also used on reload. Only seed the script into a fresh definition.
        if (fileConfiguration.getKeys(false).isEmpty()) {
            fileConfiguration.set("eliteScript.HideSeat.Events", List.of("EliteMobSpawnEvent"));
            fileConfiguration.set("eliteScript.HideSeat.Actions", List.of(Map.of(
                    "action", "POTION_EFFECT", "potionEffectType", "INVISIBILITY",
                    "amplifier", 0, "duration", Integer.MAX_VALUE, "Target", Map.of("targetType", "SELF"))));
            fileConfiguration.setComments("entityType", List.of("An invisible seat for cannon travel. Use another customboss file for a visible mount."));
        }
        super.processConfigFields();
    }
}
