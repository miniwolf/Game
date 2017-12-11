package mini.system;

import mini.asset.AssetManager;

public class ApplicationSystem {
    private static ApplicationSystemDelegate systemDelegate;

    public static ApplicationContext newContext(ApplicationContext.Type contextType) {
        checkDelegate();
        return systemDelegate.newContext();
    }

    public static Platform getPlatform() {
        checkDelegate();
        return systemDelegate.getPlatform();
    }

    /**
     * Displays an error message to the user in whichever way the context
     * feels is appropriate. If this is a headless or an offscreen surface
     * context, this method should do nothing.
     *
     * @param message The error message to display. May contain new line
     * characters.
     */
    public static void showErrorDialog(String message){
        checkDelegate();
        systemDelegate.showErrorDialog(message);
    }

    private static ApplicationSystemDelegate tryLoadDelegate(String className) throws InstantiationException, IllegalAccessException {
        try {
            return (ApplicationSystemDelegate) Class.forName(className).newInstance();
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static void setSystemDelegate(ApplicationSystemDelegate systemDelegate) {
        ApplicationSystem.systemDelegate = systemDelegate;
    }

    public static boolean isLowPermissions() {
        checkDelegate();
        return systemDelegate.isLowPermissions();
    }

    @SuppressWarnings("unchecked")
    private static void checkDelegate() {
        if (systemDelegate == null) {
            try {
                systemDelegate = tryLoadDelegate("mini.system.ApplicationDesktopSystem");
                if (systemDelegate == null) {
                    // None of the system delegates were found ..
                    System.err.println("Severe: Failed to find an ApplicationSystem delegate!\n"
                                       + "Ensure desktop is in the classpath.");
                }
            } catch (InstantiationException | IllegalAccessException ex ) {
                System.err.println("Severe: Failed to create ApplicationSystem delegate:\n" + ex);
            }
        }
    }

    public static AssetManager newAssetManager() {
        return systemDelegate.newAssetManager();
    }
}
