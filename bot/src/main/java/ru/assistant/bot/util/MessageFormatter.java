package ru.assistant.bot.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * MessageFormatter
 * @author agent
 * @since 06.02.2026
 */
@Component
public class MessageFormatter {

    private static final Pattern MARKDOWN_IN_TEMPLATE = Pattern.compile("[*_\\[\\]()`]");

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");

    private boolean needsMarkdown(String template) {
        return MARKDOWN_IN_TEMPLATE.matcher(template).find();
    }

    public String escapeForMarkdown(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        if (URL_PATTERN.matcher(text).matches()) {
            return text;
        }

        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("`", "\\`");
    }


    public FormattedMessage formatMessage(String template, Object... args) {
        try {
            if (needsMarkdown(template)) {
                Object[] escapedArgs = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof String) {
                        escapedArgs[i] = escapeForMarkdown((String) args[i]);
                    } else {
                        escapedArgs[i] = args[i];
                    }
                }

                String formattedMessage = String.format(template, escapedArgs);
                return new FormattedMessage(formattedMessage, "Markdown");

            } else {
                String formattedMessage = String.format(template, args);
                return new FormattedMessage(formattedMessage, null);
            }

        } catch (Exception e) {
            return new FormattedMessage(template, null);
        }
    }

    public FormattedMessage formatPlainText(String template, Object... args) {
        try {
            String formattedMessage = String.format(template, args);
            return new FormattedMessage(formattedMessage, null);
        } catch (Exception e) {
            return new FormattedMessage(template, null);
        }
    }

    public FormattedMessage formatMarkdown(String template, Object... args) {
        try {
            Object[] escapedArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof String) {
                    escapedArgs[i] = escapeForMarkdown((String) args[i]);
                } else {
                    escapedArgs[i] = args[i];
                }
            }

            String formattedMessage = String.format(template, escapedArgs);
            return new FormattedMessage(formattedMessage, "Markdown");

        } catch (Exception e) {
            return new FormattedMessage(template, "Markdown");
        }
    }

    public static class FormattedMessage {
        private final String text;
        private final String parseMode;

        public FormattedMessage(String text, String parseMode) {
            this.text = text;
            this.parseMode = parseMode;
        }

        public String getText() {
            return text;
        }

        public String getParseMode() {
            return parseMode;
        }

        public boolean hasParseMode() {
            return parseMode != null && !parseMode.isEmpty();
        }
    }
}