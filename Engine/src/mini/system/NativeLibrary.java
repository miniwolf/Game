package mini.system;

/**
 * Holds information about a native library for a particular platform.
 *
 * @author Kirill Vainer
 */
final class NativeLibrary {

    private final String name;
    private final Platform platform;
    private final String pathInNativesJar;
    private final String extractedAsFileName;

    /**
     * Key for map to find a library for a name and platform.
     */
    static final class Key {

        private final String name;
        private final Platform platform;

        public Key(String name, Platform platform) {
            this.name = name;
            this.platform = platform;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + this.name.hashCode();
            hash = 79 * hash + this.platform.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            return this.name.equals(other.name) && this.platform == other.platform;
        }
    }

    /**
     * The name of the library.
     * Generally only used as a way to uniquely identify the library.
     *
     * @return name of the library.
     */
    public String getName() {
        return name;
    }

    /**
     * The OS + architecture combination for which this library
     * should be extracted.
     *
     * @return platform associated to this native library
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * The filename that the library should be extracted as.
     *
     * In some cases can be different than {@link #getPathInNativesJar() path in natives jar},
     * since the names of the libraries specified in the jars are often incorrect.
     * If set to <code>null</code>, then the same name as the filename in
     * natives jar shall be used.
     *
     * @return the name that should be given to the extracted file.
     */
    public String getExtractedAsName() {
        return extractedAsFileName;
    }

    /**
     * Path inside the natives jar or classpath where the library is located.
     *
     * This library must be compatible with the {@link #getPlatform() platform}
     * which this library is associated with.
     *
     * @return path to the library in the classpath
     */
    public String getPathInNativesJar() {
        return pathInNativesJar;
    }

    /**
     * Create a new NativeLibrary.
     */
    public NativeLibrary(String name, Platform platform, String pathInNativesJar, String extractedAsFileName) {
        this.name = name;
        this.platform = platform;
        this.pathInNativesJar = pathInNativesJar;
        this.extractedAsFileName = extractedAsFileName;
    }

    /**
     * Create a new NativeLibrary.
     */
    public NativeLibrary(String name, Platform platform, String pathInNativesJar) {
        this(name, platform, pathInNativesJar, null);
    }
}
