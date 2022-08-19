package com.magmaguy.elitemobs.config.commands.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.commands.CommandsConfigFields;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class CheckTierOthersConfig extends CommandsConfigFields {
    public static String message1, message2, message3, noPermMessage;

    public CheckTierOthersConfig() {
        super("check_tier_others");
    }

    @Override
    public void generateConfigDefaults(File file, FileConfiguration fileConfiguration) {
        message1 = ConfigurationEngine.setString(file, fileConfiguration, "message1", "&7[EM] $player's combat tier is $tier", true);
        message2 = ConfigurationEngine.setString(file, fileConfiguration, "message2", "&7[EM] $player's guild tier bonus is $tier", true);
        message3 = ConfigurationEngine.setString(file, fileConfiguration, "message3", "&7[EM] &a$player's total threat tier is $tier", true);
    }

}
