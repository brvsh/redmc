package org.redmc.redmc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RedMCClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(RedMCConstants.MOD_ID);
    private KeyBinding menuKeyBind;
    private MinecraftClient client;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing...");
        client = MinecraftClient.getInstance();

        // register keybind(s)
        menuKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "RedMC"
        ));

        // register tick event(s)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKeyBind.wasPressed()) {
                // TODO: open menu, placeholder is clearing chat

                client.inGameHud.getChatHud().clear(true);
            }
        });
        LOGGER.info("Initialized!");
    }

    public static String getModVersion(String modId) {
        ModMetadata modMetadata = FabricLoader.getInstance().getModContainer(modId).get().getMetadata();
        return modMetadata.getVersion().getFriendlyString();
    }
}