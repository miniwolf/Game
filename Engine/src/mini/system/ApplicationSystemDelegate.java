package mini.system;

public abstract class ApplicationSystemDelegate {
    public abstract ApplicationContext newContext();

    public abstract void showErrorDialog(String message);

    public Platform getPlatform() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        boolean is64 = is64Bit(arch);
        if (os.contains("windows")) {
            return is64 ? Platform.Windows64 : Platform.Windows32;
        } else if (os.contains("linux") || os.contains("freebsd") || os.contains("sunos")) {
            return is64 ? Platform.Linux64 : Platform.Linux32;
        } else if (os.contains("mac os x") || os.contains("darwin")) {
            if (arch.startsWith("ppc")) {
                return is64 ? Platform.MacOSX_PPC64 : Platform.MacOSX_PPC32;
            } else {
                return is64 ? Platform.MacOSX64 : Platform.MacOSX32;
            }
        } else {
            throw new UnsupportedOperationException("The specified platform: " + os + " is not supported.");
        }
    }

    private boolean is64Bit(String arch) {
        switch (arch) {
            case "x86":
            case "ppc":
            case "PowerPC":
            case "armv7":
            case "armv7l":
            case "arm":
            case "i386":
            case "i686":
            case "universal":
            case "aarch32":
                return false;
            case "amd64":
            case "x86_64":
            case "ppc64":
            case "aarch64":
                return true;
            default:
                throw new UnsupportedOperationException("Unsupported architecture: " + arch);
        }
    }
}