package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.translations.TranslationsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class LanguageCommand extends AdvancedCommand {
    public LanguageCommand() {
        super(List.of("language"));
        List<String> suggestions = new ArrayList<>(TranslationsConfig.getTranslationConfigs().keySet().stream().toList());
        suggestions.add("english");
        addArgument("language", suggestions);
        setUsage("/em language <filename>");
        setPermission("elitemobs.language");
        setDescription("Sets the language that the server will use for EliteMobs, based on a translation file in the translation files.");
    }

    @Override
    public void execute(CommandData commandData) {
        String language = commandData.getStringArgument("language");
        if (!TranslationsConfig.getTranslationConfigs().containsKey(language) && !language.equals("english")) {
            Logger.sendMessage(commandData.getCommandSender(), "Language not found. Valid languages:");
            TranslationsConfig.getTranslationConfigs().keySet().forEach(key -> Logger.sendMessage(commandData.getCommandSender(), key));
            return;
        }
        DefaultConfig.setLanguage(commandData.getCommandSender(), language);
        Logger.sendMessage(commandData.getCommandSender(), "&2Language set to " + language +
                " ! &4Translations are created and manged for free by the community through Crowdin ( https://crowdin.com/project/elitemobs ), use at your own discretion!");
    }
}