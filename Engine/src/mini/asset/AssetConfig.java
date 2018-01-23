package mini.asset;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Scanner;

public final class AssetConfig {
    private static Class acquireClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static void loadText(AssetManager assetManager, URL configFile) throws IOException {
        try (InputStream inputStream = configFile.openStream()) {
            Scanner scanner = new Scanner(inputStream, "UTF-8");
            scanner.useLocale(Locale.ENGLISH);
            while (scanner.hasNext()) {
                String cmd = scanner.next();
                switch (cmd) {
                    case "LOADER":
                        parseLoader(assetManager, scanner);
                        break;
                    case "LOCATOR":
                        parseLocator(assetManager, scanner);
                        break;
                    case "#":
                        scanner.nextLine();
                        continue;
                    default:
                        throw new IOException("Unexpected command: " + cmd);
                }
            }
        }
    }

    private static void parseLocator(AssetManager assetManager, Scanner scanner) {
        String rootPath = scanner.next();
        String locatorClass = scanner.nextLine().trim();

        Class<? extends AssetLocator> clazz = acquireClass(locatorClass);
        if (clazz != null) {
            assetManager.registerLocator(clazz, rootPath);
        } else {
            System.err.println("Warning: Cannot find locator " + locatorClass);
        }
    }

    private static void parseLoader(AssetManager assetManager, Scanner scanner) {
        String loaderClass = scanner.next();
        String colon = scanner.next();
        if (!":".equals(colon)) {
            throw new IllegalArgumentException("Expected ':' got '" + colon + "'");
        }

        String extensionsList = scanner.nextLine();
        String[] extensions = extensionsList.split(",");
        for (int i = 0; i < extensions.length; i++) {
            extensions[i] = extensions[i].trim();
        }

        Class<? extends AssetLoader> clazz = acquireClass(loaderClass);
        if (clazz != null) {
            assetManager.registerLoader(clazz, extensions);
        } else {
            System.err.println("Warning: Cannot find loader " + loaderClass);
        }
    }
}
