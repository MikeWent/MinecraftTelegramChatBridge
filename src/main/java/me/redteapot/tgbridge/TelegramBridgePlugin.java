package me.redteapot.tgbridge;

import org.bukkit.plugin.java.JavaPlugin;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.logging.Level;

@SuppressWarnings("unused")
public class TelegramBridgePlugin extends JavaPlugin {
    private BotSession telegramBotSession = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (telegramBotSession != null) {
            return;
        }

        try {
            final PluginConfig config = PluginConfig.fromSpigotConfiguration(getConfig());
            final BridgeTelegramBot bridgeTelegramBot = new BridgeTelegramBot(
                    getLogger(),
                    config,
                    message -> getServer().broadcastMessage(message)
            );
            final MinecraftChatListener minecraftChatListener = new MinecraftChatListener(
                    getLogger(),
                    bridgeTelegramBot,
                    config.tgMessageTemplate()
            );

            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotSession = api.registerBot(bridgeTelegramBot);
            getServer().getPluginManager().registerEvents(minecraftChatListener, this);
            minecraftChatListener.start();
        } catch (TelegramApiException e) {
            getLogger().log(Level.SEVERE, "Could not initialize Telegram API", e);
        }
    }

    @Override
    public void onDisable() {
        try {
            telegramBotSession.stop();
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Exception while stopping Telegram bot session", e);
        } finally {
            telegramBotSession = null;
        }
    }
}
