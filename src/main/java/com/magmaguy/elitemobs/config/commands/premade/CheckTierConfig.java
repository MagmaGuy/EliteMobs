package com.magmaguy.elitemobs.config.commands.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.commands.CommandsConfigFields;
import org.bukkit.configuration.file.FileConfiguration;

public class CheckTierConfig extends CommandsConfigFields {
    public static String message1, message2, message3, noPermMessage;

    public CheckTierConfig() {
        super("check_tier");
    }

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        message1 = ConfigurationEngine.setString(fileConfiguration, "message1", "&7[EM] Your combat tier is $tier");
        message2 = ConfigurationEngine.setString(fileConfiguration, "message2", "&7[EM] Your guild tier bonus is $tier");
        message3 = ConfigurationEngine.setString(fileConfiguration, "message3", "&7[EM] &aYour total threat tier is $tier");
    }

}
