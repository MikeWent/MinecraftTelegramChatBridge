package me.redteapot.tgbridge;

import me.redteapot.tgbridge.templates.Template;
import me.redteapot.tgbridge.templates.TemplateSubstitutor;
import me.redteapot.tgbridge.utils.UserUtils;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashMap;
import java.util.Map;

import static me.redteapot.tgbridge.utils.Sanitizing.sanitizeForMinecraft;
import static me.redteapot.tgbridge.utils.Sanitizing.sanitizeForTelegram;
import static me.redteapot.tgbridge.utils.UserUtils.fallback;
import static me.redteapot.tgbridge.utils.UserUtils.getFullName;

public record PluginConfig(
        String username,
        String token,
        long chatID,
        int threadID,
        Template<Message> mcMessageTemplate,
        Template<AsyncPlayerChatEvent> tgMessageTemplate
) {
    private static final Map<String, TemplateSubstitutor<Message>> mcSubstitutors = new HashMap<>();
    private static final Map<String, TemplateSubstitutor<AsyncPlayerChatEvent>> tgSubstitutors = new HashMap<>();

    static {
        mcSubstitutors.put("firstName", message ->
                sanitizeForMinecraft(message.getFrom().getFirstName()));
        mcSubstitutors.put("lastName", message ->
                sanitizeForMinecraft(fallback(message.getFrom(), User::getLastName, User::getFirstName)));
        mcSubstitutors.put("fullName", message ->
                sanitizeForMinecraft(getFullName(message.getFrom())));
        mcSubstitutors.put("username", message ->
                sanitizeForMinecraft(fallback(message.getFrom(), User::getUserName, UserUtils::getFullName)));
        mcSubstitutors.put("message", message ->
                sanitizeForMinecraft(message.getText()));

        tgSubstitutors.put("username", event ->
                sanitizeForTelegram(event.getPlayer().getName()));
        tgSubstitutors.put("message", event ->
                sanitizeForTelegram(event.getMessage()));
    }

    public static PluginConfig fromSpigotConfiguration(Configuration configuration) {
        return new PluginConfig(
                configuration.getString("telegram.botUsername"),
                configuration.getString("telegram.botToken"),
                configuration.getLong("telegram.chatID"),
                configuration.getInt("telegram.threadID"),
                Template.fromString(configuration.getString("mc.messageTemplate"), mcSubstitutors),
                Template.fromString(configuration.getString("telegram.messageTemplate"), tgSubstitutors)
        );
    }
}
