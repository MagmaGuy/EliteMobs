package com.magmaguy.elitemobs.config.commands.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.commands.CommandsConfigFields;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class CheckTierConfig extends CommandsConfigFields {
    public static String message1, message2, message3, noPermMessage;

    public CheckTierConfig() {
        super("check_tier");
    }

    @Override
    public void generateConfigDefaults(File file, FileConfiguration fileConfiguration) {
        message1 = ConfigurationEngine.setString(file, fileConfiguration, "message1", "&7[EM] Your combat tier is $tier", true);
        message2 = ConfigurationEngine.setString(file, fileConfiguration, "message2", "&7[EM] Your guild tier bonus is $tier", true);
        message3 = ConfigurationEngine.setString(file, fileConfiguration, "message3", "&7[EM] &aYour total threat tier is $tier", true);
    }

}
