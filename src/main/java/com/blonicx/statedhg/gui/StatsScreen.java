package com.blonicx.statedhg.gui;

import com.blonicx.basecore.api.hglabor.HGLaborStats;
import com.blonicx.basecore.api.hglabor.enums.HGLaborGameModes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
    private String xp;
    private String username;
    private JSONObject playerStats;
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static Identifier playerHeadTexture;

    private final int boxWidth = 200;
    private final int boxHeight = 120;

    public StatsScreen() {
        super(Text.literal("Stats"));
    }

    @Override
    protected void init() {
        int boxX = (width - boxWidth) / 2;
        int boxY = 50;

        int labelX = boxX + 10;
        int valueX = boxX + boxWidth - 50;

        TextFieldWidget playerInput = new TextFieldWidget(this.textRenderer, boxX + 10, boxY - 30, 100, 20, Text.of("Player"));

        ButtonWidget searchButton = ButtonWidget.builder(Text.of("Search"), (btn) -> {
            try {
                playerStats = HGLaborStats.PlayerStats(playerInput.getText(), HGLaborGameModes.FFA);
                username = playerInput.getText();
                drawStatsBox(boxX, boxY, boxWidth, boxHeight, labelX, valueX);
                updateStats();
                updateHeadTexture();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).dimensions(boxX + 120, boxY - 30, 70, 20).build();

        this.addDrawableChild(playerInput);
        this.addDrawableChild(searchButton);
    }

    private void updateStats() {
        kills = playerStats.optString("kills", "0");
        deaths = playerStats.optString("deaths", "0");
        xp = playerStats.optString("xp", "0");
    }

    private void drawStatsBox(int x, int y, int width, int height, int labelX, int valueX) {
        // Draw the rounded rectangle
        this.addDrawable((context, mouseX, mouseY, delta) -> {
            context.fill(x, y, x + width, y + height, 0xAA000000); // Semi-transparent black

            // Draw the player head
            if (playerHeadTexture != null) {
                client.getTextureManager().bindTexture(playerHeadTexture);
                context.drawTexture(playerHeadTexture, width / 2 - 32, y + boxHeight / 4, 64, 64, 0, 0, 64, 64, 64, 64);
            }

            // Draw the text
            context.drawText(textRenderer, username, labelX, y+ 10, 0xFFFFFF, false);
            context.drawText(textRenderer, "Kills:", labelX, y + 40, 0xFFFFFF, false);
            context.drawText(textRenderer, kills, valueX, y + 40, 0xFFFFFF, false);
            context.drawText(textRenderer, "Deaths:", labelX, y + 70, 0xFFFFFF, false);
            context.drawText(textRenderer, deaths, valueX, y + 70, 0xFFFFFF, false);
            context.drawText(textRenderer, "XP:", labelX, y + 100, 0xFFFFFF, false);
            context.drawText(textRenderer, xp, valueX, y + 100, 0xFFFFFF, false);
        });
    }

    // Fetch the Player head from an api
    private void updateHeadTexture() {
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

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
}