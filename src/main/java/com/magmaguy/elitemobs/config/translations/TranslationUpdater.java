package com.magmaguy.elitemobs.config.translations;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/**
 * Keeps a downloaded language file current, the way installed content packages are kept current.
 * <p>
 * The active language is checked shortly after startup and every 24 hours. The request carries the ETag of the copy
 * already held, so an unchanged file costs a 304 and no body. A changed file is parsed and kept as
 * {@code <language>.csv.update}. It is merged into the live file on the next restart or {@code /em reload}
 * ({@link TranslationsConfigFields}), because the running plugin holds the live file in memory and writes it back.
 */
public final class TranslationUpdater {

    /**
     * Languages published at {@code magmaguy.com/api/elitemobs_translations/<language>.csv}.
     */
    public static final List<String> REMOTE_LANGUAGES = List.of(
            "french", "german", "spanish", "italian", "portuguese", "portugueseBrazilian",
            "russian", "chineseSimplified", "chineseTraditional", "japanese", "korean",
            "polish", "dutch", "czech", "hungarian", "romanian", "turkish", "vietnamese", "indonesian"
    );
    static final String UPDATE_SUFFIX = ".csv.update";
    static final String APPLYING_SUFFIX = ".csv.applying";
    private static final String BASE_URL = "https://magmaguy.com/api/elitemobs_translations/";
    private static final String ETAG_SUFFIX = ".csv.etag";
    private static final long FIRST_CHECK_DELAY_TICKS = 20L * 60;
    private static final long CHECK_INTERVAL_TICKS = 20L * 60 * 60 * 24;
    private static BukkitTask checkTask;
    private static volatile long generation;

    private TranslationUpdater() {
    }

    public static void start() {
        shutdown();
        if (!DefaultConfig.isAutoUpdateTranslations() || DefaultConfig.getLanguage() == null) return;
        String fileName = DefaultConfig.getLanguage().replace(".yml", "").replace(".csv", "");
        String language = remoteLanguage(fileName);
        if (language == null) return;
        long current = generation;
        checkTask = Bukkit.getScheduler().runTaskTimerAsynchronously(MetadataHandler.PLUGIN,
                () -> check(language, fileName, current), FIRST_CHECK_DELAY_TICKS, CHECK_INTERVAL_TICKS);
    }

    public static void shutdown() {
        generation++;
        if (checkTask != null) checkTask.cancel();
        checkTask = null;
    }

    /**
     * @return the published spelling of a configured language, or null when it is not published (english, custom, or a
     * file the server made itself)
     */
    public static String remoteLanguage(String languageName) {
        return REMOTE_LANGUAGES.stream().filter(languageName::equalsIgnoreCase).findFirst().orElse(null);
    }

    /**
     * @param language the published spelling, for the URL
     * @param fileName the configured spelling, which names the files in the translations folder
     */
    private static void check(String language, String fileName, long checkGeneration) {
        Path folder = translationsFolder();
        //The first copy is installed by /em language, which also owns the switch to it.
        if (!Files.exists(folder.resolve(fileName + ".csv"))) return;
        Path temporary = folder.resolve(fileName + ".csv." + UUID.randomUUID() + ".download");
        try {
            String etag = download(language, temporary, readEtag(folder, fileName), () -> generation != checkGeneration);
            if (etag == null || generation != checkGeneration) return;
            Files.move(temporary, folder.resolve(fileName + UPDATE_SUFFIX),
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            recordEtag(folder, fileName, etag);
            Logger.info("Downloaded updated " + language + " translations. They are applied on the next restart or /em reload.");
        } catch (IOException | RuntimeException failure) {
            if (generation == checkGeneration)
                Logger.info("Could not check for " + language + " translation updates: " + failure.getMessage());
        } finally {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
                //A leftover partial download is harmless and has a unique name.
            }
        }
    }

    /**
     * Downloads a published language file into {@code temporary} and checks that EliteMobs can parse it.
     *
     * @param knownEtag the ETag of the copy already held, or null to always download
     * @param cancelled stops the transfer early; the caller discards whatever was written
     * @return the downloaded copy's ETag ("" when the server sent none), or null when the held copy is current or the
     * transfer was cancelled
     */
    public static String download(String language, Path temporary, String knownEtag, BooleanSupplier cancelled) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + language + ".csv").openConnection();
        try {
            if (cancelled.getAsBoolean()) return null;
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(30_000);
            if (knownEtag != null && !knownEtag.isEmpty()) connection.setRequestProperty("If-None-Match", knownEtag);
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_NOT_MODIFIED) return null;
            if (status != HttpURLConnection.HTTP_OK) throw new IOException("HTTP " + status);
            Files.createDirectories(temporary.getParent());
            try (InputStream in = connection.getInputStream();
                 OutputStream out = Files.newOutputStream(temporary, StandardOpenOption.CREATE_NEW)) {
                byte[] buffer = new byte[8192];
                int count;
                while (!cancelled.getAsBoolean() && (count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            }
            if (cancelled.getAsBoolean()) return null;
            TranslationCsvParser.parse(temporary);
            String etag = connection.getHeaderField("ETag");
            return etag == null ? "" : etag;
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Records a fresh copy installed by {@code /em language}: its ETag becomes the reference for update checks, and
     * any update downloaded for the copy it replaced is obsolete.
     */
    public static void freshCopyInstalled(Path folder, String language, String etag) {
        recordEtag(folder, language, etag);
        try {
            Files.deleteIfExists(folder.resolve(language + UPDATE_SUFFIX));
            Files.deleteIfExists(folder.resolve(language + APPLYING_SUFFIX));
        } catch (IOException failure) {
            Logger.warn("Could not remove an obsolete " + language + " translation update: " + failure.getMessage());
        }
    }

    private static String readEtag(Path folder, String language) {
        Path path = folder.resolve(language + ETAG_SUFFIX);
        try {
            return Files.exists(path) ? Files.readString(path, StandardCharsets.UTF_8).trim() : null;
        } catch (IOException failure) {
            return null;
        }
    }

    private static void recordEtag(Path folder, String language, String etag) {
        Path path = folder.resolve(language + ETAG_SUFFIX);
        try {
            if (etag == null || etag.isEmpty()) Files.deleteIfExists(path);
            else Files.writeString(path, etag, StandardCharsets.UTF_8);
        } catch (IOException failure) {
            Logger.warn("Could not record the version of translations/" + language + ".csv: " + failure.getMessage());
        }
    }

    private static Path translationsFolder() {
        return Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(), "translations");
    }
}
