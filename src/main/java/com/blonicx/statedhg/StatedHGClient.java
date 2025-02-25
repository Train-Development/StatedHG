package com.blonicx.statedhg;

import com.blonicx.statedhg.gui.StatsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;


public class StatedHGClient implements ClientModInitializer {

    private static KeyBinding keyBinding;
    private boolean statsScreenOpen = false;
    Screen currentScreen = MinecraftClient.getInstance().currentScreen;

    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.statedhg.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.statedhg"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                if(statsScreenOpen){
                    MinecraftClient.getInstance().setScreen(null);
                    statsScreenOpen = false;
                }else{
                    MinecraftClient.getInstance().setScreen(new StatsScreen());
                    statsScreenOpen = true;
                }
            }
        });
    }
}