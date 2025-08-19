package xyz.tomsoz.pluginBase.common;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.internal.parser.Token;
import net.kyori.adventure.text.minimessage.internal.parser.TokenParser;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * String formatting utility
 */
@UtilityClass
public class Text {
    private final MiniMessage miniMessage = MiniMessage.builder().build();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();
    private final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();
    private final Pattern REMOVE_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-fA-F])|&[0-9a-fA-Fk-orK-OR]");

    @SafeVarargs
    public Component text(@NotNull String message, Tuple<String, Object>... args) {
        return text(null, message, args);
    }

    @SafeVarargs
    public Component text(@NotNull List<String> message, Tuple<String, Object>... args) {
        return text(String.join("\n", message), args);
    }

    @SafeVarargs
    public Component text(@Nullable Player player, @NotNull String message, Tuple<String, Object>... args) {
        return replace(miniMessage.deserialize(unescape(miniMessage.serialize(legacySerializer.deserialize("<!italic>" + message)))), args);
    }

    @SafeVarargs
    public List<Component> list(@NotNull List<String> list, Tuple<String, Object>... args) {
        return list(null, list, args);
    }

    @SafeVarargs
    public List<Component> list(@Nullable Player player, List<String> list, Tuple<String, Object>... args) {
        return list.stream().map(string -> Text.text(player, string, args)).collect(Collectors.toList());
    }

    /**
     * Unescapes minimessage tags.
     * <p>
     * This will be removed once minimessage adds the option to prevent the serializer from escaping them in the first place.
     * </p>
     *
     * @param input the minimessage formatted string with escaped tags
     * @return the minimessage formatted string without escaped tags
     */
    @SuppressWarnings("UnstableApiUsage")
    private String unescape(@NotNull String input) {
        List<Token> tokens = TokenParser.tokenize(input, false);
        tokens.sort(Comparator.comparingInt(Token::startIndex));
        StringBuilder output = new StringBuilder();
        int lastIndex = 0;
        for (Token token : tokens) {
            int start = token.startIndex();
            int end = token.endIndex();
            if (lastIndex < start) output.append(input, lastIndex, start);
            output.append(TokenParser.unescape(input.substring(start, end), 0,
                    end - start, escape -> escape == TokenParser.TAG_START || escape == TokenParser.ESCAPE));
            lastIndex = end;
        }

        if (lastIndex < input.length()) output.append(input.substring(lastIndex));
        return output.toString();
    }

    @SafeVarargs
    public Component replace(Component message, Tuple<String, Object>... args) {
        if (args.length == 0) return message;

        Component result = message;
        for (Tuple<String, Object> replacement : args) {
            Object value = replacement.second();
            if (value instanceof Component comp) {
                result = result.replaceText(b -> b.match(replacement.first()).replacement(comp));
            } else {
                result = result.replaceText(b -> b.match(replacement.first()).replacement(String.valueOf(value)));
            }
        }

        if (!result.children().isEmpty()) {
            List<Component> children = new ArrayList<>(result.children().size());
            for (Component child : result.children()) {
                ClickEvent event = child.clickEvent();
                if (event != null) {
                    String eventValue = event.payload().toString();
                    for (Tuple<String, Object> replacement : args) {
                        Object value = replacement.second();
                        String replaceWith = value instanceof TextComponent tc ? tc.content() : String.valueOf(value);
                        eventValue = eventValue.replace(replacement.first(), replaceWith);
                    }
                    if (!eventValue.equals(event.payload().toString())) {
                        child = child.clickEvent(ClickEvent.clickEvent(event.action(), eventValue));
                    }
                }
                children.add(replace(child, args));
            }
            result = result.children(children);
        }

        return result;
    }

    /**
     * Capitalizes the first letter of a string
     *
     * @param str The string
     * @return Capitalized string
     */
    public String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Strip colour codes from a string, including hex codes, codes starting with the section symbol,
     * codes starting with an ampersand and minimessage codes.
     *
     * @param str String with colour codes
     * @return String without colour codes
     */
    public String removeColourCodes(String str) {
        str = legacySerializer.serialize(miniMessage.deserialize(str));
        str = REMOVE_PATTERN.matcher(str).replaceAll("");
        return str;
    }

    public double getAmountFromString(String amountString) throws NumberFormatException {
        if (amountString.toLowerCase().contains("nan") || amountString.toLowerCase().contains("infinity")) {
            throw new NumberFormatException();
        }

        double multi = 1;
        if (amountString.toLowerCase().endsWith("k")) {
            multi = 1_000;
            amountString = amountString.replace("k", "");
            amountString = amountString.replace("K", "");
        } else if (amountString.toLowerCase().endsWith("m")) {
            multi = 1_000_000;
            amountString = amountString.replace("m", "");
            amountString = amountString.replace("M", "");
        } else if (amountString.toLowerCase().endsWith("b")) {
            multi = 1_000_000_000;
            amountString = amountString.replace("b", "");
            amountString = amountString.replace("B", "");
        } else if (amountString.toLowerCase().endsWith("t")) {
            multi = 1_000_000_000_000L;
            amountString = amountString.replace("t", "");
            amountString = amountString.replace("T", "");
        } else if (amountString.toLowerCase().endsWith("q")) {
            multi = 1_000_000_000_000_000L;
            amountString = amountString.replace("q", "");
            amountString = amountString.replace("Q", "");
        }

        return Double.parseDouble(amountString) * multi;
    }

    public Component extractItemName(ItemStack item) {
        return Component.text(extractItemNameString(item));
    }

    public String extractItemNameString(ItemStack item) {
        return item.getItemMeta() == null ? "" : item.getItemMeta().getDisplayName();
    }
}
