package com.clickablelinks;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LinkifyUtil {

    private static final Pattern URL_PATTERN =
            Pattern.compile("(?i)\\b(?:https?://|www\\.)[^\\s<>\"]+");

    private static final String TRAILING_PUNCTUATION = ".,;:!?)]}'\"";

    private LinkifyUtil() {
    }

    /** Returns the same text with every URL made clickable. If there are no URLs, returns the original. */
    public static Text linkify(Text original) {
        MutableText result = Text.empty();
        boolean[] changed = {false};

        original.visit((style, string) -> {
            appendLinkified(result, style, string, changed);
            return Optional.empty();
        }, Style.EMPTY);

        return changed[0] ? result : original;
    }

    private static void appendLinkified(MutableText out, Style style, String string, boolean[] changed) {
        // Don't touch text that already has a click action (e.g. links added by the server).
        if (style.getClickEvent() != null) {
            out.append(Text.literal(string).setStyle(style));
            return;
        }

        Matcher matcher = URL_PATTERN.matcher(string);
        int last = 0;

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Strip punctuation that is probably not part of the link.
            while (end > start && TRAILING_PUNCTUATION.indexOf(string.charAt(end - 1)) >= 0) {
                end--;
            }

            String url = string.substring(start, end);
            String target = url.regionMatches(true, 0, "www.", 0, 4) ? "https://" + url : url;

            if (!isValid(target)) {
                continue;
            }

            if (start > last) {
                out.append(Text.literal(string.substring(last, start)).setStyle(style));
            }

            Style linkStyle = style
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, target))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(target)))
                    .withUnderline(true);
            if (style.getColor() == null) {
                linkStyle = linkStyle.withColor(Formatting.AQUA);
            }

            out.append(Text.literal(url).setStyle(linkStyle));
            last = end;
            changed[0] = true;
        }

        if (last < string.length()) {
            out.append(Text.literal(string.substring(last)).setStyle(style));
        }
    }

    private static boolean isValid(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            return uri.getHost() != null && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (Exception e) {
            return false;
        }
    }
}
