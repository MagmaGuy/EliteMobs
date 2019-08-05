package com.magmaguy.elitemobs.config.commands.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.commands.CommandsConfigFields;
import org.bukkit.configuration.file.FileConfiguration;

public class CheckTierOthersConfig extends CommandsConfigFields {
    public CheckTierOthersConfig() {
        super("check_tier_others");
    }

    public static String message1, message2, message3, noPermMessage;

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        message1 = ConfigurationEngine.setString(fileConfiguration, "message1", "&7[EM] $player's combat tier is $tier");
        message2 = ConfigurationEngine.setString(fileConfiguration, "message2", "&7[EM] $player's guild tier bonus is $tier");
        message3 = ConfigurationEngine.setString(fileConfiguration, "message3", "&7[EM] &a$player's total threat tier is $tier");
    }

}
