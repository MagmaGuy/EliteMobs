package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ConfigurationExporter {
    private ConfigurationExporter() {
    }

    public static void initializeConfigs() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Path configurationsPath = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath());
                if (!Files.isDirectory(Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "exports"))) {
                    try {
                        Files.createDirectory(Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "exports"));
                    } catch (Exception exception) {
                        Logger.warn("Failed to create exports directory! Tell the dev!");
                        exception.printStackTrace();
                    }
                }
                try {
                    File rspFile = Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "exports" + File.separatorChar + "elitemobs_resource_pack.zip").toFile();
                    if (rspFile.exists()) rspFile.delete();
                    downloadFile("https://magmaguy.com/downloads/elitemobs_resource_pack.zip", Paths.get(configurationsPath.normalize() + "" + File.separatorChar + "exports").toAbsolutePath().toString());
                } catch (Exception e) {
                    Logger.warn("Failed to download official resource pack! Tell the dev!");
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);
    }

    public static void downloadFile(String fileURL, String saveFilePath) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // Check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");

            if (disposition != null) {
                // Extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10, disposition.length() - 1);
                }
            } else {
                // Extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1);
            }

            // Opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePathWithName = saveFilePath + File.separatorChar + fileName;

            // Opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePathWithName);

            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

        } else {
            Logger.warn("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }

    public static byte[] sha1CodeByteArray(File file) throws IOException, NoSuchAlgorithmException {
        FileInputStream fileInputStream = new FileInputStream(file);
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, digest);
        byte[] bytes = new byte[1024];
        // read all file content
        while (digestInputStream.read(bytes) > 0) digest = digestInputStream.getMessageDigest();
        return digest.digest();
    }
}
