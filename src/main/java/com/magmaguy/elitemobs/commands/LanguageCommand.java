package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class LanguageCommand extends AdvancedCommand {
    public LanguageCommand() {
        super(List.of("language"));
        addArgument("language", TranslationsConfig.getTranslationConfigs().keySet().stream().toList());
        setUsage("/em language <filename>");
        setPermission("elitemobs.*");
        setDescription("Sets the language that the server will use for EliteMobs, based on a translation file in the translation files.");
    }

    @Override
    public void execute() {
        String language = getStringArgument("language");
        if (!TranslationsConfig.getTranslationConfigs().containsKey(language)) {
            Logger.sendMessage(getCurrentCommandSender(), "Language not found. Valid languages:");
            TranslationsConfig.getTranslationConfigs().keySet().forEach(key -> Logger.sendMessage(getCurrentCommandSender(), key));
            return;
        }
        DefaultConfig.setLanguage(getCurrentCommandSender(), language);
        Logger.sendMessage(getCurrentCommandSender(), "&2Language set to " + language +
                " ! &4Translations are created and manged for free by the community through Crowdin ( https://crowdin.com/project/elitemobs ), use at your own discretion!");
    }
}