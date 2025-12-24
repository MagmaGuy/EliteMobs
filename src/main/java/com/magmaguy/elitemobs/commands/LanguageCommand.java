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

/**
 * Command to set the language for EliteMobs.
 * Downloads CSV translation files from remote server if not present locally.
 *
 * Special languages:
 * - "english": Uses plugin defaults directly, no CSV involved
 * - "custom": Auto-generates an English→English CSV for customization or adding new languages
 */
public class LanguageCommand extends AdvancedCommand {

    // Remote-available languages (CSV files hosted on server)
    private static final List<String> REMOTE_LANGUAGES = List.of(
            "french", "german", "spanish", "italian", "portuguese", "portugueseBrazilian",
            "russian", "chineseSimplified", "chineseTraditional", "japanese", "korean",
            "polish", "dutch", "czech", "hungarian", "romanian", "turkish", "vietnamese", "indonesian"
    );

    private final List<String> suggestions;

    public LanguageCommand() {
        super(List.of("language"));

        // Collect remote + on-disk + english + custom
        Set<String> langs = new LinkedHashSet<>();
        langs.add("english");  // Default - uses plugin files directly
        langs.add("custom");   // Auto-generated English→English for customization
        langs.addAll(REMOTE_LANGUAGES);

        Path folder = Paths.get(
                MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(),
                "translations"
        );
        if (Files.isDirectory(folder)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(folder, "*.csv")) {
                for (Path p : ds) {
                    String filename = p.getFileName().toString();
                    // Skip data files
                    if (filename.endsWith("_data.csv")) continue;
                    // Add language name without extension
                    langs.add(filename.replace(".csv", ""));
                }
            } catch (Exception e) {
                Logger.warn("Could not list translations folder: " + e.getMessage());
            }
        }

        suggestions = new ArrayList<>(langs);
        addArgument("language",
                new ListStringCommandArgument(suggestions, "<language>")
        );

        setUsage("/em language <language>");
        setPermission("elitemobs.language");
        setDescription("Sets the language for EliteMobs (downloads from remote if needed).");
    }

    @Override
    public void execute(CommandData commandData) {
        String language = commandData.getStringArgument("language");
        CommandSender sender = commandData.getCommandSender();

        // Normalize language name
        language = language.replace(".csv", "").replace(".yml", "");

        String finalLanguage = language;
        if (!suggestions.stream().anyMatch(s -> s.equalsIgnoreCase(finalLanguage))) {
            Logger.sendMessage(sender, "&cLanguage not found. Valid options:");
            suggestions.forEach(s -> Logger.sendMessage(sender, "  - " + s));
            return;
        }

        // Handle special cases
        if (language.equals("english")) {
            // English uses plugin defaults directly, no CSV
            DefaultConfig.setLanguage(sender, language);
            Logger.sendMessage(sender, "&2Language set to English! Using plugin defaults.");
            return;
        }

        if (language.equals("custom")) {
            // Custom auto-generates if not present
            Path folder = Paths.get(
                    MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(),
                    "translations"
            );
            Path target = folder.resolve("custom.csv");

            if (!Files.exists(target)) {
                Logger.sendMessage(sender, "&eGenerating custom.csv template...");
                if (!generateCustomCsv(target)) {
                    Logger.sendMessage(sender, "&cFailed to generate custom.csv. Language not changed.");
                    return;
                }
                Logger.sendMessage(sender, "&2Generated custom.csv! Edit it to customize text or add translations.");
            }

            DefaultConfig.setLanguage(sender, language);
            Logger.sendMessage(sender,
                    "&2Language set to custom! " +
                    "&eEdit plugins/EliteMobs/translations/custom.csv to customize text. " +
                    "New keys will be added automatically as the plugin runs.");
            return;
        }

        // Regular language - download if not present
        Path folder = Paths.get(
                MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(),
                "translations"
        );
        Path target = folder.resolve(language + ".csv");

        if (!Files.exists(target)) {
            Logger.sendMessage(sender, "&eDownloading " + language + ".csv...");
            if (!downloadRemoteLanguage(language, target)) {
                Logger.sendMessage(sender, "&cFailed to download " + language + ".csv. Language not changed.");
                return;
            }
            Logger.sendMessage(sender, "&2Downloaded " + language + ".csv successfully.");
        }

        // Set the language and reload
        DefaultConfig.setLanguage(sender, language);
        Logger.sendMessage(sender,
                "&2Language set to " + language + "! " +
                "&eTranslations are community-managed. Use at your own discretion.");
    }

    /**
     * Generates a custom.csv template with English→English structure.
     * The file starts with just a header, and keys are added as the plugin runs.
     */
    private boolean generateCustomCsv(Path outPath) {
        try {
            Files.createDirectories(outPath.getParent());
            // Create with header only - keys will be populated as add() is called
            // Include UTF-8 BOM for Excel/editor compatibility
            String header = "\uFEFF\"key\",\"en\",\"custom\"\n";
            Files.writeString(outPath, header, java.nio.charset.StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (Exception ex) {
            Logger.warn("Error generating custom.csv: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Downloads a language CSV file from the remote server.
     */
    private boolean downloadRemoteLanguage(String language, Path outPath) {
        String apiUrl = "https://magmaguy.com/api/elitemobs_translations/" + language + ".csv";
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(30_000);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Logger.warn("Download failed: HTTP " + responseCode + " for " + language + ".csv");
                return false;
            }

            Files.createDirectories(outPath.getParent());
            try (InputStream in = conn.getInputStream();
                 OutputStream out = Files.newOutputStream(outPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                byte[] buf = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buf)) != -1) {
                    out.write(buf, 0, bytesRead);
                }
            }
            conn.disconnect();
            return true;

        } catch (Exception ex) {
            Logger.warn("Error downloading " + language + ".csv: " + ex.getMessage());
            return false;
        }
    }
}
