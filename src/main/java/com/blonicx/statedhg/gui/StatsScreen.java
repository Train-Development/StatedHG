package com.blonicx.statedhg.gui;

import com.blonicx.basecore.api.hglabor.HGLaborStats;
import com.blonicx.basecore.api.hglabor.enums.HGLaborGameModes;
import com.blonicx.basecore.api.hglabor.enums.ffa.FFAValues;
import com.blonicx.basecore.api.minecraft.client.utils.PlayerData;
import com.blonicx.statedhg.StatedHG;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.io.IOException;

public class StatsScreen extends Screen {
    public Screen parent;

    private String kills;
    private String deaths;
    private String bounty;
    private String xp;
    private String username;

    private final String ModOwner = "belte11";
    private final String Goat = "noriskk";

    private JSONObject playerStats;

    private JSONArray leaderboard;

    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static Identifier playerHeadTexture;
    //private static Identifier firstPlaceAward = Identifier.of("statedhg", "textures/gui/first_place_award.png");


    // Leaderboard Values //
    private final int boxWidth = 200;
    private final int boxHeight = 120;

    public StatsScreen() {
        super(Text.literal("Stats"));
    }

    @Override
    protected void init() {
        defaultButtons();
        myStatsTab();
    }

    // Utils //
    private void updateStats() {
        kills = playerStats.optString("kills", "0");
        deaths = playerStats.optString("deaths", "0");
        bounty = playerStats.optString("bounty", "0");
        xp = playerStats.optString("xp", "0");
    }

    private void updateLeaderboard(FFAValues sort) {
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
                    updateHeadTexture(username);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                username = "Unknown";
            }

            StatedHG.LOGGER.info("JSONData: " + leaderboard);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    // Fetch the Player head from an api
    private void updateHeadTexture(String username) {
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

    // Tabs //
    private void defaultButtons(){
        int boxX = (width - boxWidth) / 2;
        int boxY = 75;

        int labelX = boxX + 10;
        int valueX = boxX + boxWidth - 50;

        ButtonWidget myStatsTabButton = ButtonWidget.builder(Text.translatable("statedhg.button.myStatsTabButton"), (btn) -> {
            this.clearChildren();
            defaultButtons();
            myStatsTab();
        }).dimensions(boxX + 60, boxY - 65, 80, 20).build();

        ButtonWidget searchPlayerTabButton = ButtonWidget.builder(Text.translatable("statedhg.button.searchPlayerTabButton"), (btn) -> {
            this.clearChildren();
            defaultButtons();
            searchTab();
        }).dimensions(boxX + 150, boxY - 65, 80, 20).build();

        ButtonWidget leaderboardTabButton = ButtonWidget.builder(Text.translatable("statedhg.button.leaderboardTabButton"), (btn) -> {
            this.clearChildren();
            defaultButtons();
            leaderboardTab();
        }).dimensions(boxX - 30, boxY - 65, 80, 20).build();

        this.addDrawableChild(leaderboardTabButton);
        this.addDrawableChild(myStatsTabButton);
        this.addDrawableChild(searchPlayerTabButton);
    }

    private void myStatsTab(){
        try{
            int boxX = (width - boxWidth) / 2;
            int boxY = 75;

            int labelX = boxX + 10;
            int valueX = boxX + boxWidth - 50;

            username = MinecraftClient.getInstance().player.getName().getString();
            playerStats = HGLaborStats.PlayerStats(username , HGLaborGameModes.FFA);
            updateHeadTexture(username);
            updateStats();
            drawStatsBox(boxX, boxY, boxWidth, boxHeight, labelX, valueX);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void searchTab(){
        int boxX = (width - boxWidth) / 2;
        int boxY = 75;

        int labelX = boxX + 10;
        int valueX = boxX + boxWidth - 50;

        TextFieldWidget playerInput = new TextFieldWidget(this.textRenderer, boxX + 10, boxY - 30, 100, 20, Text.of("Player"));
        playerInput.setPlaceholder(Text.translatable("statedhg.input_field.searchInputPlaceholder"));

        ButtonWidget searchButton = ButtonWidget.builder(Text.translatable("statedhg.button.searchButton"), (btn) -> {
            try {
                if (playerInput.getText().isEmpty()){
                    this.clearChildren();
                    defaultButtons();
                    searchTab();
                }else {
                    playerStats = HGLaborStats.PlayerStats(playerInput.getText(), HGLaborGameModes.FFA);
                    username = playerInput.getText();
                    this.clearChildren();
                    defaultButtons();
                    searchTab();
                    updateHeadTexture(username);
                    updateStats();
                    drawStatsBox(boxX, boxY, boxWidth, boxHeight, labelX, valueX);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).dimensions(boxX + 120, boxY - 30, 70, 20).build();

        this.addDrawableChild(playerInput);
        this.addDrawableChild(searchButton);
    }

    private void leaderboardTab(){
        int boxX = (width - boxWidth) / 2;
        int boxY = 75;

        int labelX = boxX + 10;
        int valueX = boxX + boxWidth - 50;

        ButtonWidget killsTabButton = ButtonWidget.builder(Text.translatable("statedhg.button.killsTabButton"), (btn) -> {
            updateLeaderboard(FFAValues.KILLS);
        }).dimensions(boxX + 150, boxY - 30, 60, 20).build();

        ButtonWidget deathsTabButton = ButtonWidget.builder(Text.translatable("statedhg.button.deathsTabButton"), (btn) -> {
            updateLeaderboard(FFAValues.DEATHS);
        }).dimensions(boxX + 70, boxY - 30, 60, 20).build();

        ButtonWidget xpTabButton = ButtonWidget.builder(Text.translatable("statedhg.button.xpTabButton"), (btn) -> {
            updateLeaderboard(FFAValues.XP);
        }).dimensions(boxX - 10, boxY - 30, 60, 20).build();

        this.addDrawableChild(killsTabButton);
        this.addDrawableChild(deathsTabButton);
        this.addDrawableChild(xpTabButton);

        drawStatsBox(boxX, boxY, boxWidth, boxHeight, labelX, valueX);
    }

    // Boxe //
    private void drawStatsBox(int x, int y, int width, int height, int labelX, int valueX) {
        // Draw the rounded rectangle
        this.addDrawable((context, mouseX, mouseY, delta) -> {
            context.fill(x, y, x + width, y + height, 0xAA000000); // Semi-transparent black

            // Draw the player head
            if (playerHeadTexture != null) {
                client.getTextureManager().bindTexture(playerHeadTexture);
                context.drawTexture(playerHeadTexture, valueX, y + 10, 32, 32, 0, 0, 32, 32, 32, 32);
            }

            // Special Player Titles
            if (username.equalsIgnoreCase (ModOwner)) {
                context.drawText(textRenderer, com.blonicx.basecore.api.utils.io.Text.rainbowText("Mod Owner"), labelX, y + 25, 0xFFFFFF, false);
            } else if (username.equalsIgnoreCase(Goat)) {
                context.drawText(textRenderer, com.blonicx.basecore.api.utils.io.Text.rainbowText("Goat/Server Owner"), labelX, y + 25, 0xFFFFFF, false);
            }

            //context.drawTexture(firstPlaceAward, labelX, y + 35, 32, 32, 0, 0, 32, 32, 32, 32);

            context.drawText(textRenderer, username, labelX, y + 10, 0xFFFFFF, false);
            context.drawText(textRenderer, "Kills:", labelX, y + 55, 0xFFFFFF, false);
            context.drawText(textRenderer, kills, valueX, y + 55, 0xFFFFFF, false);

            context.drawText(textRenderer, "Deaths:", labelX, y + 70, 0xFFFFFF, false);
            context.drawText(textRenderer, deaths, valueX, y + 70, 0xFFFFFF, false);

            context.drawText(textRenderer, "Bounty:", labelX, y + 85, 0xFFFFFF, false);
            context.drawText(textRenderer, bounty, valueX, y + 85, 0xFFFFFF, false);

            context.drawText(textRenderer, "XP:", labelX, y + 100, 0xFFFFFF, false);
            context.drawText(textRenderer, xp, valueX, y + 100, 0xFFFFFF, false);
        });
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
}