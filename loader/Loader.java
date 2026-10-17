import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ShopVisuals installer/loader.
 * Installs Fabric for Minecraft 1.21.1, Fabric API and the bundled ShopVisuals jar,
 * then adds a "ShopVisuals" profile to the official launcher.
 * Only touches: <minecraft dir>/versions, /mods and launcher_profiles.json (backed up first).
 */
public class Loader {
    static final String MC = "1.21.1";
    static final String PROFILE_KEY = "shopvisuals";
    static final String MOD_RESOURCE = "/shopvisuals.jar";
    static final String UA = "ShopVisualsLoader/1.0";

    static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL).build();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Loader::gui);
    }

    // ---------------------------------------------------------------- GUI
    static void gui() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        Color bg = new Color(0x14, 0x14, 0x18), fg = new Color(0xEE, 0xEE, 0xEE), acc = new Color(0x7C, 0xE0, 0xB0);

        JFrame f = new JFrame("ShopVisuals Loader");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(520, 380);
        f.setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(bg);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("ShopVisuals  \u2022  Minecraft " + MC + " (Fabric)");
        title.setForeground(acc);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JTextField path = new JTextField(defaultMinecraftDir().toString());
        JButton browse = new JButton("...");
        JPanel pathRow = new JPanel(new BorderLayout(6, 0));
        pathRow.setOpaque(false);
        pathRow.add(path, BorderLayout.CENTER);
        pathRow.add(browse, BorderLayout.EAST);

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 8));
        top.setOpaque(false);
        top.add(title);
        top.add(pathRow);

        JTextArea log = new JTextArea();
        log.setEditable(false);
        log.setBackground(new Color(0x0C, 0x0C, 0x10));
        log.setForeground(fg);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(log);

        JButton install = new JButton("Install / Update");
        JProgressBar bar = new JProgressBar();
        bar.setVisible(false);
        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);
        bottom.add(bar, BorderLayout.NORTH);
        bottom.add(install, BorderLayout.CENTER);

        root.add(top, BorderLayout.NORTH);
        root.add(sp, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        f.setContentPane(root);

        Consumer<String> out = s -> SwingUtilities.invokeLater(() -> {
            log.append(s + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });

        browse.addActionListener(e -> {
            JFileChooser c = new JFileChooser(path.getText());
            c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (c.showOpenDialog(f) == JFileChooser.APPROVE_OPTION) path.setText(c.getSelectedFile().getPath());
        });

        install.addActionListener(e -> {
            install.setEnabled(false);
            bar.setIndeterminate(true);
            bar.setVisible(true);
            Path mc = Paths.get(path.getText().trim());
            new Thread(() -> {
                try {
                    install(mc, out);
                    out.accept("\nDone. Open the Minecraft Launcher and pick the 'ShopVisuals' profile.");
                } catch (Exception ex) {
                    out.accept("\nERROR: " + ex.getMessage());
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        install.setEnabled(true);
                        bar.setVisible(false);
                    });
                }
            }, "installer").start();
        });

        f.setVisible(true);
    }

    static Path defaultMinecraftDir() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home");
        if (os.contains("win")) {
            String appdata = System.getenv("APPDATA");
            return Paths.get(appdata != null ? appdata : home + "\\AppData\\Roaming", ".minecraft");
        }
        if (os.contains("mac")) return Paths.get(home, "Library", "Application Support", "minecraft");
        return Paths.get(home, ".minecraft");
    }

    // ------------------------------------------------------------ install
    static void install(Path mc, Consumer<String> log) throws Exception {
        if (!Files.isDirectory(mc)) throw new Exception("Minecraft folder not found: " + mc
                + "\nRun the official launcher once, or pick the right folder.");

        // 1. latest stable Fabric loader
        log.accept("Looking up the latest Fabric loader for " + MC + "...");
        String loaders = get("https://meta.fabricmc.net/v2/versions/loader/" + MC);
        Matcher m = Pattern.compile("\"loader\"\\s*:\\s*\\{[^}]*?\"version\"\\s*:\\s*\"([^\"]+)\"[^}]*?\"stable\"\\s*:\\s*true")
                .matcher(loaders);
        String loaderVer;
        if (m.find()) loaderVer = m.group(1);
        else {
            Matcher any = Pattern.compile("\"loader\"\\s*:\\s*\\{[^}]*?\"version\"\\s*:\\s*\"([^\"]+)\"").matcher(loaders);
            if (!any.find()) throw new Exception("Could not read the Fabric loader list.");
            loaderVer = any.group(1);
        }
        log.accept("Fabric loader " + loaderVer);

        // 2. version profile json
        String profileJson = get("https://meta.fabricmc.net/v2/versions/loader/" + MC + "/" + loaderVer + "/profile/json");
        Matcher idm = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"").matcher(profileJson);
        if (!idm.find()) throw new Exception("Fabric profile has no id.");
        String versionId = idm.group(1);
        Path vdir = mc.resolve("versions").resolve(versionId);
        Files.createDirectories(vdir);
        Files.writeString(vdir.resolve(versionId + ".json"), profileJson, StandardCharsets.UTF_8);
        log.accept("Installed version " + versionId);

        // 3. mods folder
        Path mods = mc.resolve("mods");
        Files.createDirectories(mods);

        // 4. Fabric API (Modrinth), skipped if present
        boolean hasApi;
        try (var s = Files.list(mods)) {
            hasApi = s.anyMatch(p -> p.getFileName().toString().toLowerCase().startsWith("fabric-api"));
        }
        if (hasApi) log.accept("Fabric API already present, skipping.");
        else {
            log.accept("Downloading Fabric API...");
            String q = "https://api.modrinth.com/v2/project/fabric-api/version?game_versions="
                    + URLEncoder.encode("[\"" + MC + "\"]", StandardCharsets.UTF_8)
                    + "&loaders=" + URLEncoder.encode("[\"fabric\"]", StandardCharsets.UTF_8);
            Matcher um = Pattern.compile("\"url\"\\s*:\\s*\"(https://cdn\\.modrinth\\.com/[^\"]+?\\.jar)\"").matcher(get(q));
            if (!um.find()) throw new Exception("Could not find a Fabric API build for " + MC + ".");
            String url = um.group(1);
            download(url, mods.resolve(url.substring(url.lastIndexOf('/') + 1)));
        }

        // 5. ShopVisuals jar (replaces older copies)
        try (var s = Files.list(mods)) {
            for (Path p : (Iterable<Path>) s::iterator) {
                if (p.getFileName().toString().toLowerCase().startsWith("shopvisuals")) {
                    Files.delete(p);
                    log.accept("Removed old " + p.getFileName());
                }
            }
        }
        try (InputStream in = Loader.class.getResourceAsStream(MOD_RESOURCE)) {
            if (in == null) throw new Exception("shopvisuals.jar is not bundled in this loader. Rebuild it (see README).");
            Files.copy(in, mods.resolve("shopvisuals.jar"), StandardCopyOption.REPLACE_EXISTING);
        }
        log.accept("Installed shopvisuals.jar");

        // 6. launcher profile
        addProfile(mc, versionId, log);
    }

    static void addProfile(Path mc, String versionId, Consumer<String> log) throws Exception {
        Path lp = mc.resolve("launcher_profiles.json");
        if (!Files.exists(lp)) {
            log.accept("launcher_profiles.json not found. Start the official launcher once, then run this again.");
            return;
        }
        String s = Files.readString(lp, StandardCharsets.UTF_8);
        if (s.contains("\"" + PROFILE_KEY + "\"")) {
            log.accept("Launcher profile already exists.");
            return;
        }
        int p = s.indexOf("\"profiles\"");
        int brace = p < 0 ? -1 : s.indexOf('{', p);
        if (brace < 0) {
            log.accept("Could not find 'profiles' in launcher_profiles.json, create the profile manually.");
            return;
        }
        Files.copy(lp, mc.resolve("launcher_profiles.json.bak"), StandardCopyOption.REPLACE_EXISTING);
        int i = brace + 1;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        boolean empty = i < s.length() && s.charAt(i) == '}';
        String entry = "\n    \"" + PROFILE_KEY + "\": {\"name\": \"ShopVisuals\", \"type\": \"custom\", "
                + "\"lastVersionId\": \"" + versionId + "\", \"icon\": \"Furnace\"}" + (empty ? "" : ",");
        Files.writeString(lp, s.substring(0, brace + 1) + entry + s.substring(brace + 1), StandardCharsets.UTF_8);
        log.accept("Added launcher profile (backup: launcher_profiles.json.bak)");
    }

    // ---------------------------------------------------------------- http
    static String get(String url) throws Exception {
        HttpResponse<String> r = HTTP.send(req(url), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 != 2) throw new Exception("HTTP " + r.statusCode() + " for " + url);
        return r.body();
    }

    static void download(String url, Path dest) throws Exception {
        HttpResponse<Path> r = HTTP.send(req(url), HttpResponse.BodyHandlers.ofFile(dest));
        if (r.statusCode() / 100 != 2) throw new Exception("HTTP " + r.statusCode() + " for " + url);
    }

    static HttpRequest req(String url) {
        return HttpRequest.newBuilder(URI.create(url)).header("User-Agent", UA).GET().build();
    }
}
