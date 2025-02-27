package com.blonicx.statedhg.gui;

import com.blonicx.basecore.api.hglabor.HGLaborStats;
import com.blonicx.basecore.api.hglabor.enums.HGLaborGameModes;
import com.blonicx.basecore.api.hglabor.enums.ffa.FFAValues;

import com.blonicx.statedhg.utils.DataReceiver;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.io.IOException;

public class StatsScreen extends Screen {
    public Screen parent;

    private final String ModOwner = "belte11";
    private final String Goat = "noriskk";

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

        ButtonWidget leaderboardTabButton = ButtonWidget.builder(Text.translatable("statedhg.button.topPlayersTabButton"), (btn) -> {
            this.clearChildren();
            defaultButtons();
            leaderboardTab();
        }).dimensions(boxX - 30, boxY - 65, 80, 20).build();

        this.addDrawableChild(leaderboardTabButton);
        this.addDrawableChild(myStatsTabButton);
        this.addDrawableChild(searchPlayerTabButton);
    }

    private void myStatsTab(){
        int boxX = (width - boxWidth) / 2;
        int boxY = 75;

        int labelX = boxX + 10;
        int valueX = boxX + boxWidth - 50;

        DataReceiver.loadMyStats();

        drawStatsBox(boxX, boxY, boxWidth, boxHeight, labelX, valueX);
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
                    DataReceiver.playerStats = HGLaborStats.PlayerStats(playerInput.getText(), HGLaborGameModes.FFA);
                    DataReceiver.username = playerInput.getText();
                    this.clearChildren();
                    defaultButtons();
                    searchTab();
                    DataReceiver.updateHeadTexture();
                    DataReceiver.updateStats();
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

        ButtonWidget deathsTabButton = ButtonWidget.builder(Text.translatable("statedhg.button.deathsTabButton"), (btn) -> {
            this.clearChildren();
            DataReceiver.updateLeaderboard(FFAValues.DEATHS);
            defaultButtons();
            leaderboardTab();
            drawStatsBox(boxX, boxY, boxWidth, boxHeight, labelX, valueX);
        }).dimensions(boxX + 150, boxY - 30, 60, 20).build();

        ButtonWidget killsTabButton = ButtonWidget.builder(Text.translatable("statedhg.button.killsTabButton"), (btn) -> {
            this.clearChildren();
            DataReceiver.updateLeaderboard(FFAValues.KILLS);
            defaultButtons();
            leaderboardTab();
            drawStatsBox(boxX, boxY, boxWidth, boxHeight, labelX, valueX);
        }).dimensions(boxX + 70, boxY - 30, 60, 20).build();

        ButtonWidget xpTabButton = ButtonWidget.builder(Text.translatable("statedhg.button.xpTabButton"), (btn) -> {
            this.clearChildren();
            defaultButtons();
            leaderboardTab();
            DataReceiver.updateLeaderboard(FFAValues.XP);
            drawStatsBox(boxX, boxY, boxWidth, boxHeight, labelX, valueX);
        }).dimensions(boxX - 10, boxY - 30, 60, 20).build();

        this.addDrawableChild(killsTabButton);
        this.addDrawableChild(deathsTabButton);
        this.addDrawableChild(xpTabButton);
    }

    // Box //
    private void drawStatsBox(int x, int y, int width, int height, int labelX, int valueX) {
        // Draw the rounded rectangle
        this.addDrawable((context, mouseX, mouseY, delta) -> {
            context.fill(x, y, x + width, y + height, 0xAA000000); // Semi-transparent black

            // Draw the player head
            if (DataReceiver.playerHeadTexture != null) {
                client.getTextureManager().bindTexture(DataReceiver.playerHeadTexture);
                context.drawTexture(DataReceiver.playerHeadTexture, valueX, y + 10, 32, 32, 0, 0, 32, 32, 32, 32);
            }

            // Special Player Titles
            if (DataReceiver.username.equalsIgnoreCase (ModOwner)) {
                context.drawText(textRenderer, com.blonicx.basecore.api.utils.io.Text.rainbowText("Mod Owner"), labelX, y + 25, 0xFFFFFF, false);
            } else if (DataReceiver.username.equalsIgnoreCase(Goat)) {
                context.drawText(textRenderer, com.blonicx.basecore.api.utils.io.Text.rainbowText("Goat/Server Owner"), labelX, y + 25, 0xFFFFFF, false);
            }

            context.drawText(textRenderer, DataReceiver.username, labelX, y + 10, 0xFFFFFF, false);
            context.drawText(textRenderer, "Kills:", labelX, y + 55, 0xFFFFFF, false);
            context.drawText(textRenderer, DataReceiver.kills, valueX, y + 55, 0xFFFFFF, false);

            context.drawText(textRenderer, "Deaths:", labelX, y + 70, 0xFFFFFF, false);
            context.drawText(textRenderer, DataReceiver.deaths, valueX, y + 70, 0xFFFFFF, false);

            context.drawText(textRenderer, "Bounty:", labelX, y + 85, 0xFFFFFF, false);
            context.drawText(textRenderer, DataReceiver.bounty, valueX, y + 85, 0xFFFFFF, false);

            context.drawText(textRenderer, "XP:", labelX, y + 100, 0xFFFFFF, false);
            context.drawText(textRenderer, DataReceiver.xp, valueX, y + 100, 0xFFFFFF, false);
        });
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
}