package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.command.CommandSender;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LanguageCommand extends AdvancedCommand {

    // 1) the remote‐available languages, now WITH .yml
    private static final List<String> REMOTE_LANGUAGES = List.of(
            "chineseSimplified.yml", "chineseTraditional.yml", "czech.yml", "dutch.yml",
            "french.yml", "german.yml", "hungarian.yml", "indonesian.yml", "italian.yml",
            "japanese.yml", "korean.yml", "polish.yml", "portugueseBrazilian.yml", "romanian.yml",
            "russian.yml", "spanish.yml", "turkish.yml", "vietnamese.yml"
    );

    private final List<String> suggestions;

    public LanguageCommand() {
        super(List.of("language"));

        // 2) collect remote + on-disk + english.yml
        Set<String> langs = new LinkedHashSet<>(REMOTE_LANGUAGES);

        Path folder = Paths.get(
                MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(),
                "translations"
        );
        if (Files.isDirectory(folder)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(folder, "*.yml")) {
                for (Path p : ds) {
                    // keep the .yml suffix here
                    langs.add(p.getFileName().toString());
                }
            } catch (Exception e) {
                Logger.warn("Could not list translations folder: " + e.getMessage());
            }
        }

        // always allow the default
        langs.add("english.yml");

        suggestions = new ArrayList<>(langs);
        addArgument("language",
                new ListStringCommandArgument(suggestions, "<language.yml>")
        );

        setUsage("/em language <language.yml>");
        setPermission("elitemobs.language");
        setDescription("Sets the language for EliteMobs (fetches from remote if needed).");
    }

    @Override
    public void execute(CommandData commandData) {
        String langFile = commandData.getStringArgument("language");
        CommandSender sender = commandData.getCommandSender();

        // validate against full filenames
        if (!suggestions.contains(langFile)) {
            Logger.sendMessage(sender, "Language not found. Valid options:");
            suggestions.forEach(s -> Logger.sendMessage(sender, s));
            return;
        }

        // if non-English, ensure we have the file locally
        if (!langFile.equals("english.yml")) {
            Path folder = Paths.get(
                    MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(),
                    "translations"
            );
            Path target = folder.resolve(langFile);

            if (!Files.exists(target)) {
                Logger.sendMessage(sender, "&eDownloading " + langFile + "…");
                if (!downloadRemoteLanguage(langFile, target)) {
                    Logger.sendMessage(sender, "&cFailed to download " + langFile + ". Language not changed.");
                    return;
                }
                Logger.sendMessage(sender, "&2Downloaded " + langFile + " successfully.");
            }
        }

        // finally, flip the config and reload
        DefaultConfig.setLanguage(sender, langFile);
        Logger.sendMessage(sender,
                "&2Language set to " + langFile +
                        " ! &4Translations are managed remotely—use at your own discretion!");
    }

    private boolean downloadRemoteLanguage(String fileName, Path outPath) {
        String apiUrl = "https://magmaguy.com/api/elitemobs_translations/" + fileName;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(5_000);

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Logger.warn("Download failed: HTTP " + conn.getResponseCode());
                return false;
            }

            Files.createDirectories(outPath.getParent());
            try (InputStream in = conn.getInputStream();
                 OutputStream out = Files.newOutputStream(outPath, StandardOpenOption.CREATE_NEW)) {
                byte[] buf = new byte[4096];
                int r;
                while ((r = in.read(buf)) != -1) {
                    out.write(buf, 0, r);
                }
            }
            conn.disconnect();
            return true;

        } catch (Exception ex) {
            Logger.warn("Error downloading " + fileName + ": " + ex.getMessage());
            return false;
        }
    }
}
