package mini.system;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static mini.system.Platform.*;

/**
 * Utility class to register, extract, and load native libraries.
 * <br>
 * Register your own libraries via the {@link #registerNativeLibrary(String, Platform, String, String)} method, for
 * each platform.
 * You can then extract this library (depending on platform), by
 * using {@link #loadNativeLibrary(java.lang.String, boolean) }.
 * <br>
 * Example:<br>
 * <code><pre>
 * NativeLibraryLoader.registerNativeLibrary("mystuff", Platform.Windows32, "native/windows/mystuff.dll");
 * NativeLibraryLoader.registerNativeLibrary("mystuff", Platform.Windows64, "native/windows/mystuff64.dll");
 * NativeLibraryLoader.registerNativeLibrary("mystuff", Platform.Linux32,   "native/linux/libmystuff.so");
 * NativeLibraryLoader.registerNativeLibrary("mystuff", Platform.Linux64,   "native/linux/libmystuff64.so");
 * NativeLibraryLoader.registerNativeLibrary("mystuff", Platform.MacOSX32,  "native/macosx/libmystuff.jnilib");
 * NativeLibraryLoader.registerNativeLibrary("mystuff", Platform.MacOSX64,  "native/macosx/libmystuff.jnilib");
 * </pre></code>
 * <br>
 * This will register the library. Load it via: <br>
 * <code><pre>
 * NativeLibraryLoader.loadNativeLibrary("mystuff", true);
 * </pre></code>
 * It will load the right library automatically based on the platform.
 *
 * @author Kirill Vainer
 */
public final class NativeLibraryLoader {
    private static final byte[] buf = new byte[1024 * 100];
    private static File extractionFolderOverride = null;
    private static File extractionFolder = null;

    private static final HashMap<NativeLibrary.Key, NativeLibrary> nativeLibraryMap
            = new HashMap<NativeLibrary.Key, NativeLibrary>();

    /**
     * Register a new known library.
     * <p>
     * This simply registers a known library, the actual extraction and loading
     * is performed by calling {@link #loadNativeLibrary(java.lang.String, boolean) }.
     *
     * @param name          The name / ID of the library (not OS or architecture specific).
     * @param platform      The platform for which the in-natives-jar path has
     *                      been specified for.
     * @param path          The path inside the natives-jar or classpath
     *                      corresponding to this library. Must be compatible with the platform
     *                      argument.
     * @param extractAsName The filename that the library should be extracted as,
     *                      if null, use the same name as in the path.
     */
    public static void registerNativeLibrary(String name, Platform platform,
                                             String path, String extractAsName) {
        nativeLibraryMap.put(new NativeLibrary.Key(name, platform),
                new NativeLibrary(name, platform, path, extractAsName));
    }

    /**
     * Register a new known JNI library.
     * <p>
     * This simply registers a known library, the actual extraction and loading
     * is performed by calling {@link #loadNativeLibrary(java.lang.String, boolean) }.
     * <p>
     * This method should be called several times for each library name,
     * each time specifying a different platform + path combination.
     *
     * @param name     The name / ID of the library (not OS or architecture specific).
     * @param platform The platform for which the in-natives-jar path has
     *                 been specified for.
     * @param path     The path inside the natives-jar or classpath
     *                 corresponding to this library. Must be compatible with the platform
     *                 argument.
     */
    public static void registerNativeLibrary(String name, Platform platform,
                                             String path) {
        registerNativeLibrary(name, platform, path, null);
    }

    static {
        // LWJGL
        registerNativeLibrary("lwjgl", Platform.Windows32, "lib/native/windows/lwjgl.dll");
        registerNativeLibrary("lwjgl", Platform.Windows64, "lib/native/windows/lwjgl64.dll");
        registerNativeLibrary("lwjgl", Linux32, "lib/native/linux/liblwjgl.so");
        registerNativeLibrary("lwjgl", Linux64, "lib/native/linux/liblwjgl64.so");
        registerNativeLibrary("lwjgl", MacOSX32, "lib/native/macosx/liblwjgl.dylib");
        registerNativeLibrary("lwjgl", MacOSX64, "lib/native/macosx/liblwjgl.dylib");
    }

    private NativeLibraryLoader() {
    }

    /**
     * Determine if native bullet is on the classpath.
     * <p>
     * Currently the context extracts the native bullet libraries, so
     * this method is needed to determine if it is needed.
     * Ideally, native bullet should be responsible for its own natives.
     *
     * @return True native bullet is on the classpath, false otherwise.
     */
    public static boolean isUsingNativeBullet() {
        try {
            Class clazz = Class.forName("com.jme3.bullet.util.NativeMeshUtil");
            return clazz != null;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    /**
     * Specify a custom location where native libraries should
     * be extracted to. Ensure this is a unique path not used
     * by other applications to extract their libraries.
     * Set to <code>null</code> to restore default
     * functionality.
     *
     * @param path Path where to extract native libraries.
     */
    public static void setCustomExtractionFolder(String path) {
        extractionFolderOverride = new File(path).getAbsoluteFile();
    }

    /**
     * Returns the folder where native libraries will be extracted.
     * This is automatically determined at run-time based on the
     * following criteria:<br>
     * <ul>
     * <li>If a {@link #setCustomExtractionFolder(java.lang.String) custom
     * extraction folder} has been specified, it is returned.
     * <li>If the user can write to the working folder, then it
     * is returned.</li>
     * <li>Otherwise, the {@link ApplicationSystem#getStorageFolder() storage folder}
     * is used, to prevent collisions, a special subfolder is used
     * called <code>natives_&lt;hash&gt;</code> where &lt;hash&gt;
     * is computed automatically as the XOR of the classpath hash code
     * and the last modified date of this class.
     *
     * @return Path where natives will be extracted to.
     */
    public static File getExtractionFolder() {
        if (extractionFolderOverride != null) {
            return extractionFolderOverride;
        }
        if (extractionFolder == null) {
            File workingFolder = new File("").getAbsoluteFile();
            if (!workingFolder.canWrite()) {
                setExtractionFolderToUserCache();
            } else {
                try {
                    File file = new File(workingFolder + File.separator + ".jmetestwrite");
                    file.createNewFile();
                    file.delete();
                    extractionFolder = workingFolder;
                } catch (Exception e) {
                    setExtractionFolderToUserCache();
                }
            }
        }
        return extractionFolder;
    }

    /**
     * Determine jME3's cache folder for the user account based on the OS.
     * <p>
     * If the OS cache folder is missing, the assumption is that this
     * particular version of the OS does not have a dedicated cache folder,
     * hence, we use the user's home folder instead as the root.
     * <p>
     * The folder returned is as follows:<br>
     * <ul>
     * <li>Windows: ~\AppData\Local\mini</li>
     * <li>Mac OS X: ~/Library/Caches/mini</li>
     * <li>Linux: ~/.cache/mini</li>
     * </ul>
     *
     * @return the user cache folder.
     */
    private static File getUserCacheFolder() {
        File userHomeFolder = new File(System.getProperty("user.home"));
        File userCacheFolder = null;

        switch (ApplicationSystem.getPlatform()) {
            case Linux32:
            case Linux64:
                userCacheFolder = new File(userHomeFolder, ".cache");
                break;
            case MacOSX32:
            case MacOSX64:
            case MacOSX_PPC32:
            case MacOSX_PPC64:
                userCacheFolder = new File(new File(userHomeFolder, "Library"), "Caches");
                break;
            case Windows32:
            case Windows64:
                userCacheFolder = new File(new File(userHomeFolder, "AppData"), "Local");
                break;
        }

        if (!userCacheFolder.exists()) {
            // Fallback to home directory if cache folder is missing
            return new File(userHomeFolder, ".mini");
        }

        return new File(userCacheFolder, "mini");
    }

    private static void setExtractionFolderToUserCache() {
        File extractFolderInHome = getUserCacheFolder();

        if (!extractFolderInHome.exists()) {
            extractFolderInHome.mkdir();
        }

        extractionFolder = new File(extractFolderInHome, "natives_" + Integer.toHexString(computeNativesHash()));

        if (!extractionFolder.exists()) {
            extractionFolder.mkdir();
        }

        System.err.println("Working directory is not writable. "
                + "Natives will be extracted to:\n" + extractionFolder);
    }

    private static int computeNativesHash() {
        URLConnection conn = null;
        try {
            String classpath = System.getProperty("java.class.path");
            URL url = Thread.currentThread().getContextClassLoader().getResource("mini/system/NativeLibraryLoader.class");

            StringBuilder sb = new StringBuilder(url.toString());
            if (sb.indexOf("jar:") == 0) {
                sb.delete(0, 4);
                sb.delete(sb.indexOf("!"), sb.length());
                sb.delete(sb.lastIndexOf("/") + 1, sb.length());
            }
            try {
                url = new URL(sb.toString());
            } catch (MalformedURLException ex) {
                throw new UnsupportedOperationException(ex);
            }

            conn = url.openConnection();
            int hash = classpath.hashCode() ^ (int) conn.getLastModified();
            return hash;
        } catch (IOException ex) {
            throw new UnsupportedOperationException(ex);
        } finally {
            if (conn != null) {
                try {
                    conn.getInputStream().close();
                    conn.getOutputStream().close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public static File[] getJarsWithNatives() {
        Set<File> jarFiles = new HashSet<>();
        for (Map.Entry<NativeLibrary.Key, NativeLibrary> lib : nativeLibraryMap.entrySet()) {
            File jarFile = getJarForNativeLibrary(lib.getValue().getPlatform(), lib.getValue().getName());
            if (jarFile != null) {
                jarFiles.add(jarFile);
            }
        }
        return jarFiles.toArray(new File[0]);
    }

    public static void extractNativeLibraries(Platform platform, File targetDir) throws IOException {
        for (Map.Entry<NativeLibrary.Key, NativeLibrary> lib : nativeLibraryMap.entrySet()) {
            if (lib.getValue().getPlatform() == platform) {
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }
                extractNativeLibrary(platform, lib.getValue().getName(), targetDir);
            }
        }
    }

    private static String mapLibraryName_emulated(String name, Platform platform) {
        switch (platform) {
            case MacOSX32:
            case MacOSX64:
                return name + ".dylib";
            case Windows32:
            case Windows64:
                return name + ".dll";
            default:
                return name + ".so";
        }
    }

    /**
     * Removes platform-specific portions of a library file name so
     * that it can be accepted by {@link System#loadLibrary(java.lang.String) }.
     * <p>
     * E.g.<br>
     * <ul>
     * <li>jinput-dx8_64.dll => jinput-dx8_64</li>
     * <li>liblwjgl64.so => lwjgl64</li>
     * <li>libopenal.so => openal</li>
     * </ul>
     *
     * @param filename The filename to strip platform-specific parts
     * @return The stripped library name
     */
    private static String unmapLibraryName(String filename) {
        StringBuilder sb = new StringBuilder(filename);
        if (sb.indexOf("lib") == 0 && !filename.toLowerCase().endsWith(".dll")) {
            sb.delete(0, 3);
        }
        int dot = sb.lastIndexOf(".");
        if (dot > 0) {
            sb.delete(dot, sb.length());
        }
        return sb.toString();
    }

    public static File getJarForNativeLibrary(Platform platform, String name) {
        NativeLibrary library = nativeLibraryMap.get(new NativeLibrary.Key(name, platform));
        if (library == null) {
            return null;
        }

        String pathInJar = library.getPathInNativesJar();
        if (pathInJar == null) {
            return null;
        }

        String fileNameInJar;
        if (pathInJar.contains("/")) {
            fileNameInJar = pathInJar.substring(pathInJar.lastIndexOf("/") + 1);
        } else {
            fileNameInJar = pathInJar;
        }

        URL url = Thread.currentThread().getContextClassLoader().getResource(pathInJar);
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource(fileNameInJar);
        }

        if (url == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(url.toString());
        if (sb.indexOf("jar:file:/") == 0) {
            sb.delete(0, 9);
            sb.delete(sb.indexOf("!"), sb.length());
            return new File(sb.toString());
        } else {
            return null; // not a jar
        }
    }

    public static void extractNativeLibrary(Platform platform, String name, File targetDir) throws IOException {
        NativeLibrary library = nativeLibraryMap.get(new NativeLibrary.Key(name, platform));
        if (library == null) {
            return;
        }

        String pathInJar = library.getPathInNativesJar();
        if (pathInJar == null) {
            return;
        }

        String fileNameInJar;
        if (pathInJar.contains("/")) {
            fileNameInJar = pathInJar.substring(pathInJar.lastIndexOf("/") + 1);
        } else {
            fileNameInJar = pathInJar;
        }

        URL url = Thread.currentThread().getContextClassLoader().getResource(pathInJar);
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource(fileNameInJar);
        }

        if (url == null) {
            return;
        }

        String loadedAsFileName;
        if (library.getExtractedAsName() != null) {
            loadedAsFileName = library.getExtractedAsName();
        } else {
            loadedAsFileName = fileNameInJar;
        }

        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();

        File targetFile = new File(targetDir, loadedAsFileName);
        OutputStream out = null;
        try {
            out = new FileOutputStream(targetFile);
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    /**
     * First extracts the native library and then loads it.
     *
     * @param name       The name of the library to load.
     * @param isRequired If true and the library fails to load, throw exception. If
     *                   false, do nothing if it fails to load.
     */
    public static void loadNativeLibrary(String name, boolean isRequired) {
        Platform platform = ApplicationSystem.getPlatform();
        NativeLibrary library = nativeLibraryMap.get(new NativeLibrary.Key(name, platform));

        if (library == null) {
            // No library exists for this platform.
            if (isRequired) {
                throw new UnsatisfiedLinkError(
                        "The required native library '" + name + "'"
                                + " is not available for your OS: " + platform);
            } else {
                System.out.println("The optional native library ''" + name + "''" +
                        " is not available for your OS: " + platform);
                return;
            }
        }

        final String pathInJar = library.getPathInNativesJar();

        if (pathInJar == null) {
            // This platform does not require the native library to be loaded.
            return;
        }

        final String fileNameInJar;

        if (pathInJar.contains("/")) {
            fileNameInJar = pathInJar.substring(pathInJar.lastIndexOf("/") + 1);
        } else {
            fileNameInJar = pathInJar;
        }

        URL url = Thread.currentThread().getContextClassLoader().getResource(pathInJar);

        if (url == null) {
            // Try the root of the classpath as well.
            url = Thread.currentThread().getContextClassLoader().getResource(fileNameInJar);
        }

        if (url == null) {
            // Attempt to load it as a system library.
            String unmappedName = unmapLibraryName(fileNameInJar);
            try {
                // XXX: HACK. Vary loading method based on library name..
                // lwjgl and jinput handle loading by themselves.
                if (!name.equals("lwjgl") && !name.equals("jinput")) {
                    // Need to unmap it from library specific parts.
                    System.loadLibrary(unmappedName);
                    System.out.println("Loaded system installed "
                            + "version of native library: " + unmappedName);
                }
            } catch (UnsatisfiedLinkError e) {
                if (isRequired) {
                    throw new UnsatisfiedLinkError(
                            "The required native library '" + unmappedName + "'"
                                    + " was not found in the classpath via '" + pathInJar
                                    + "'. Error message: " + e.getMessage());
                } else {
                    System.out.println("The optional native library ''" + unmappedName + "''" +
                            " was not found in the classpath via ''" + pathInJar + "''" +
                            ". Error message: " + e.getMessage());
                }
            }

            return;
        }

        // The library has been found and is ready to be extracted.
        // Determine what filename it should be extracted as.
        String loadedAsFileName;
        if (library.getExtractedAsName() != null) {
            loadedAsFileName = library.getExtractedAsName();
        } else {
            // Just use the original filename as it is in the JAR.
            loadedAsFileName = fileNameInJar;
        }

        File extactionDirectory = getExtractionFolder();
        URLConnection conn;
        InputStream in;

        try {
            conn = url.openConnection();
            in = conn.getInputStream();
        } catch (IOException ex) {
            // Maybe put more detail here? Not sure..
            throw new UnsatisfiedLinkError("Failed to open file: '" + url +
                    "'. Error: " + ex);
        }

        File targetFile = new File(extactionDirectory, loadedAsFileName);
        OutputStream out = null;
        try {
            if (targetFile.exists()) {
                // OK, compare last modified date of this file to
                // file in jar
                long targetLastModified = targetFile.lastModified();
                long sourceLastModified = conn.getLastModified();

                // Allow ~1 second range for OSes that only support low precision
                if (targetLastModified + 1000 > sourceLastModified) {
                    System.out.println("Not copying library " + loadedAsFileName + "."
                            + "Latest already extracted.");
                    return;
                }
            }

            out = new FileOutputStream(targetFile);
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            in = null;
            out.close();
            out = null;

            // NOTE: On OSes that support "Date Created" property,
            // this will cause the last modified date to be lower than
            // date created which makes no sense
            targetFile.setLastModified(conn.getLastModified());
        } catch (IOException ex) {
            if (ex.getMessage().contains("used by another process")) {
                return;
            } else {
                throw new UnsatisfiedLinkError("Failed to extract native "
                        + "library to: " + targetFile);
            }
        } finally {
            // XXX: HACK. Vary loading method based on library name..
            // lwjgl and jinput handle loading by themselves.
            switch (name) {
                case "lwjgl":
                case "lwjgl3":
                    System.setProperty("org.lwjgl.librarypath",
                            extactionDirectory.getAbsolutePath());
                    break;
                case "jinput":
                    System.setProperty("net.java.games.input.librarypath",
                            extactionDirectory.getAbsolutePath());
                    break;
                default:
                    // all other libraries (openal, bullet, custom)
                    // will load directly in here.
                    System.load(targetFile.getAbsolutePath());
                    break;
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                }
            }
        }

        System.out.println("Loaded native library from ''" + url + "'' into ''" + targetFile + "''");
    }

}

