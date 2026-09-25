package web;

import java.util.Locale;

// Minimal JSON helpers so the project needs no external libraries
final class Json {

    private Json() {
    }

    static String str(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

    static String num(Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return "null";
        }
        return String.format(Locale.US, "%.2f", value);
    }
}
