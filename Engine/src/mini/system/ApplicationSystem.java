package mini.system;

import mini.Application;

public class ApplicationSystem {
    private static ApplicationSystemDelegate systemDelegate;

    public static ApplicationContext newContext() {
        checkDelegate();
        return systemDelegate.newContext();
    }

    private static ApplicationSystemDelegate tryLoadDelegate(String className) throws InstantiationException, IllegalAccessException {
        try {
            return (ApplicationSystemDelegate) Class.forName(className).newInstance();
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static void checkDelegate() {
        if (systemDelegate == null) {
            try {
                systemDelegate = tryLoadDelegate("mini.system.JmeDesktopSystem");
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
}
