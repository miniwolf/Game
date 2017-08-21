package mini.system;

public enum Platform {

    /**
     * Microsoft Windows 32 bit
     */
    Windows32,

    /**
     * Microsoft Windows 64 bit
     */
    Windows64(true),

    /**
     * Linux 32 bit
     */
    Linux32,

    /**
     * Linux 64 bit
     */
    Linux64(true),

    /**
     * Apple Mac OS X 32 bit
     */
    MacOSX32,

    /**
     * Apple Mac OS X 64 bit
     */
    MacOSX64(true),

    /**
     * Apple Mac OS X 32 bit PowerPC
     */
    MacOSX_PPC32,

    /**
     * Apple Mac OS X 64 bit PowerPC
     */
    MacOSX_PPC64(true);

    private final boolean is64bit;

    public boolean is64Bit() {
        return is64bit;
    }

    Platform(boolean is64bit) {
        this.is64bit = is64bit;
    }

    Platform() {
        this(false);
    }
}
