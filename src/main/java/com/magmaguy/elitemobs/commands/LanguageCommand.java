package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.translations.TranslationCsvParser;
import com.magmaguy.elitemobs.config.translations.TranslationsConfigFields;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private static Request activeRequest;

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
        language = language.trim();
        if (language.toLowerCase(java.util.Locale.ROOT).endsWith(".csv") ||
                language.toLowerCase(java.util.Locale.ROOT).endsWith(".yml")) {
            language = language.substring(0, language.length() - 4);
        }

        String requestedLanguage = language;
        language = suggestions.stream()
                .filter(s -> s.equalsIgnoreCase(requestedLanguage))
                .findFirst()
                .orElse(null);
        if (language == null) {
            Logger.sendMessage(sender, CommandMessagesConfig.getLanguageNotFoundMessage());
            suggestions.forEach(s -> Logger.sendMessage(sender, CommandMessagesConfig.getLanguageListPrefix() + s));
            return;
        }

        // A newer selection or reload owns activation, including choices needing no download.
        shutdown();
        // Handle special cases
        if (language.equals("english")) {
            // English uses plugin defaults directly, no CSV
            DefaultConfig.setLanguage(sender, language);
            Logger.sendMessage(sender, CommandMessagesConfig.getLanguageSetEnglishMessage());
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
                Logger.sendMessage(sender, CommandMessagesConfig.getLanguageGeneratingCustomMessage());
                if (!generateCustomCsv(target)) {
                    Logger.sendMessage(sender, CommandMessagesConfig.getLanguageGenerateFailedMessage());
                    return;
                }
                //The recorded baselines describe the file that was just replaced, not this one.
                TranslationsConfigFields.discardBaseline(folder, language);
                Logger.sendMessage(sender, CommandMessagesConfig.getLanguageGenerateSuccessMessage());
            }

            DefaultConfig.setLanguage(sender, language);
            Logger.sendMessage(sender, CommandMessagesConfig.getLanguageSetCustomMessage());
            return;
        }

        // Regular language - download if not present
        Path folder = Paths.get(
                MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(),
                "translations"
        );
        Path target = folder.resolve(language + ".csv");

        if (!Files.exists(target)) {
            Logger.sendMessage(sender, CommandMessagesConfig.getLanguageDownloadingMessage().replace("$language", language));
            Request request = new Request(language, target, sender);
            activeRequest = request;
            try {
                request.worker = Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> prepare(request));
            } catch (RuntimeException failure) {
                request.cancel();
                activeRequest = null;
                Logger.sendMessage(sender, CommandMessagesConfig.getLanguageDownloadFailedMessage().replace("$language", language));
            }
            return;
        }

        // Set the language and reload
        DefaultConfig.setLanguage(sender, language);
        Logger.sendMessage(sender, CommandMessagesConfig.getLanguageSetMessage().replace("$language", language));
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

    private static void prepare(Request request) {
        boolean queued = false;
        try {
            request.download();
            synchronized (request) {
                if (request.cancelled) return;
                request.completion = Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> complete(request));
                queued = true;
            }
        } catch (Exception failure) {
            synchronized (request) {
                if (!request.cancelled) {
                    Logger.warn("Error downloading " + request.language + ".csv: " + failure.getMessage());
                    try {
                        request.completion = Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                            if (activeRequest != request || request.cancelled) return;
                            activeRequest = null;
                            Logger.sendMessage(request.sender, CommandMessagesConfig.getLanguageDownloadFailedMessage()
                                    .replace("$language", request.language));
                        });
                    } catch (RuntimeException ignored) { /* Shutdown owns cancellation. */ }
                }
            }
        } finally {
            if (!queued) request.removeTemporary();
        }
    }

    private static void complete(Request request) {
        try {
            if (activeRequest != request || request.cancelled) return;
            // A local file supplied while downloading wins over the obsolete prepared copy.
            if (!Files.exists(request.target)) {
                Files.move(request.temporary, request.target, StandardCopyOption.ATOMIC_MOVE);
                TranslationsConfigFields.discardBaseline(request.target.getParent(), request.language);
            }
            activeRequest = null;
            Logger.sendMessage(request.sender, CommandMessagesConfig.getLanguageDownloadSuccessMessage()
                    .replace("$language", request.language));
            DefaultConfig.setLanguage(request.sender, request.language);
            Logger.sendMessage(request.sender, CommandMessagesConfig.getLanguageSetMessage().replace("$language", request.language));
        } catch (Exception failure) {
            Logger.warn("Could not activate language " + request.language + ": " + failure.getMessage());
            Logger.sendMessage(request.sender, CommandMessagesConfig.getLanguageDownloadFailedMessage().replace("$language", request.language));
        } finally {
            if (activeRequest == request) activeRequest = null;
            request.removeTemporary();
        }
    }

    public static void shutdown() {
        Request previous = activeRequest;
        activeRequest = null;
        if (previous != null) previous.cancel();
    }

    private static final class Request {
        final String language;
        final Path target;
        final Path temporary;
        final CommandSender sender;
        volatile boolean cancelled;
        BukkitTask worker;
        BukkitTask completion;

        Request(String language, Path target, CommandSender sender) {
            this.language = language;
            this.target = target;
            this.sender = sender;
            temporary = target.resolveSibling(target.getFileName() + "." + UUID.randomUUID() + ".download");
        }

        void download() throws Exception {
            HttpURLConnection current = (HttpURLConnection) new URL(
                    "https://magmaguy.com/api/elitemobs_translations/" + language + ".csv").openConnection();
            if (cancelled) { current.disconnect(); return; }
            try {
                current.setRequestMethod("GET");
                current.setConnectTimeout(10_000);
                current.setReadTimeout(30_000);
                int status = current.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) throw new java.io.IOException("HTTP " + status);
                Files.createDirectories(target.getParent());
                try (InputStream in = current.getInputStream();
                     OutputStream out = Files.newOutputStream(temporary, StandardOpenOption.CREATE_NEW)) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while (!cancelled && (count = in.read(buffer)) != -1) out.write(buffer, 0, count);
                }
                if (!cancelled) TranslationCsvParser.parse(temporary);
            } finally {
                current.disconnect();
            }
        }

        synchronized void cancel() {
            cancelled = true;
            if (worker != null) worker.cancel();
            if (completion != null) completion.cancel();
            removeTemporary(); // An active worker repeats cleanup after closing its stream.
        }

        void removeTemporary() {
            try { Files.deleteIfExists(temporary); }
            catch (java.io.IOException failure) {
                if (!cancelled) Logger.warn("Could not remove translation download " + temporary.getFileName() + ": " + failure.getMessage());
            }
        }
    }
}
