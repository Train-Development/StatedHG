package com.blonicx.statedhg.utils;

import com.blonicx.basecore.api.hglabor.HGLaborStats;
import com.blonicx.basecore.api.hglabor.enums.HGLaborGameModes;
import com.blonicx.basecore.api.hglabor.enums.ffa.FFAValues;

import com.blonicx.basecore.api.minecraft.client.utils.PlayerData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class DataReceiver {
    // JSON Vars //
    public static JSONObject playerStats;
    public static JSONArray leaderboard;

    // Minecraft Vars //
    public static final MinecraftClient client = MinecraftClient.getInstance();
    public static Identifier playerHeadTexture;

    // Stats //
    public static String kills;
    public static String deaths;
    public static String bounty;
    public static String xp;

    public static String username;
    public static String cachedUsername = "";

    public static void loadMyStats(){
        try{
            if (MinecraftClient.getInstance().player != null){
                username = MinecraftClient.getInstance().player.getName().getString();
            }
            playerStats = HGLaborStats.PlayerStats(username , HGLaborGameModes.FFA);
            updateHeadTexture();
            updateStats();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void updateStats() {
        kills = playerStats.optString("kills", "0");
        deaths = playerStats.optString("deaths", "0");
        bounty = playerStats.optString("bounty", "0");
        xp = playerStats.optString("xp", "0");
    }


    public static void updateLeaderboard(FFAValues sort) {
        try{
            leaderboard = HGLaborStats.TopPlayers(sort, HGLaborGameModes.FFA, 1);

            JSONObject first_place = leaderboard.getJSONObject(0);

            if (first_place != null) {
                try {
                    username = PlayerData.getUsernameFromUUID(first_place.get("playerId").toString());
                    kills = first_place.optString("kills", "0");
                    deaths = first_place.optString("deaths", "0");
                    bounty = first_place.optString("bounty", "0");
                    xp = first_place.optString("xp", "0");
                    updateHeadTexture();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                username = "Unknown";
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    // Fetch the Player head from an api
    public static void updateHeadTexture() {
        if (!username.isEmpty()) {
            String url = "https://mc-heads.net/avatar/" + username + "/64";
            CompletableFuture.runAsync(() -> {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    BufferedImage image = ImageIO.read(inputStream);
                    inputStream.close();
                    connection.disconnect();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", baos);
                    NativeImage nativeImage = NativeImage.read(baos.toByteArray());
                    NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
                    playerHeadTexture = client.getTextureManager().registerDynamicTexture("player_head", texture);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
