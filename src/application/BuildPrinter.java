package application;

import build.BuildView;
import model.attribute.ComponentType;
import model.component.Component;

import java.util.ArrayList;
import java.util.List;

public final class BuildPrinter {
    private static final String EMPTY_SLOT = "-- empty --";
    private BuildPrinter() {}

    public static String render(BuildView build) {
        String title  = "PC BUILD: " + build.name();
        String footer = "Total draw: " + build.totalWattage() + " W";

        List<String> headers = new ArrayList<>();
        List<List<String>> bodies = new ArrayList<>();
        for (ComponentType type : ComponentType.values()) {
            List<Component> list = build.getComponents(type);
            headers.add(type + " [" + list.size() + "]");
            List<String> body = new ArrayList<>();
            if (list.isEmpty()) body.add("  " + EMPTY_SLOT);
            else for (Component c : list) body.add("  * " + describe(c));
            bodies.add(body);
        }

        int width = Math.max(title.length(), footer.length());
        for (int i = 0; i < headers.size(); i++) {
            width = Math.max(width, headers.get(i).length());
            for (String line : bodies.get(i)) width = Math.max(width, line.length());
        }
        width += 2;

        String bar  = "═".repeat(width);
        String thin = "─".repeat(width);
        StringBuilder sb = new StringBuilder();
        sb.append("╔").append(bar).append("╗\n");
        sb.append("║").append(center(title, width)).append("║\n");
        sb.append("╠").append(bar).append("╣\n");
        for (int i = 0; i < headers.size(); i++) {
            sb.append("║ ").append(pad(headers.get(i), width - 1)).append("║\n");
            for (String line : bodies.get(i))
                sb.append("║ ").append(pad(line, width - 1)).append("║\n");
            if (i < headers.size() - 1) sb.append("╟").append(thin).append("╢\n");
        }
        sb.append("╠").append(bar).append("╣\n");
        sb.append("║ ").append(pad(footer, width - 1)).append("║\n");
        sb.append("╚").append(bar).append("╝\n");
        return sb.toString();
    }

    private static String describe(Component c) { return c.name() + " (" + c.powerDrawWatts() + " W)"; }
    private static String pad(String s, int width) { return String.format("%-" + width + "s", s); }
    private static String center(String s, int width) {
        int left = (width - s.length()) / 2;
        return " ".repeat(left) + s + " ".repeat(width - s.length() - left);
    }
}
