package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.magmacore.command.arguments.DynamicListStringCommandArgument;
import org.bukkit.entity.EntityType;

import java.util.Locale;

/** Commands use the configured elite catalog, independently of natural-spawn enablement. */
final class EliteEntityTypeCommandArgument extends DynamicListStringCommandArgument {
    EliteEntityTypeCommandArgument() {
        super(() -> MobPropertiesConfig.getMobProperties().keySet().stream()
                .map(EntityType::name).sorted().toList(), "<entityType>");
    }

    static EntityType parse(String input) {
        return EntityType.valueOf(input.toUpperCase(Locale.ROOT));
    }
}
