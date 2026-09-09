package com.magmaguy.elitemobs.config.npcs.premade;

import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCInteractions;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolMode;
import org.bukkit.entity.Villager;

import java.util.List;

/** Default Guild circuit. Patrol nodes are offsets from the configured spawn. */
public final class GuildPatrolConfig extends NPCsConfigFields {
    public GuildPatrolConfig() {
        super("guild_patrol", true,
                "<g:#A3C75B:#5F8A36>Gribble the Lookout</g>",
                "<g:#7A9A45:#4D6B2C><Guild Patrol></g>",
                Villager.Profession.ARMORER,
                "em_adventurers_guild,286.531,92,284.350,0,0",
                List.of(), List.of(), List.of(), false, 3,
                NPCInteractions.NPCInteractionType.NONE);
        setSyncMovement(true);
    }

    @Override
    public void processConfigFields() {
        processBoolean("patrol.enabled", true, false, true);
        processString("patrol.mode", PatrolMode.LOOP.name(), null, true);
        processDouble("patrol.speed", 1D, 1D, true);
        processStringList("patrol.nodes", List.of(
                "0,0,0",
                "0.126,0,12.284",
                "13.895,0,12.206",
                "14.014,0,0.002"), null, true);
        super.processConfigFields();
    }
}
