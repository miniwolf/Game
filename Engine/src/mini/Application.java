package mini;

import mini.system.ApplicationContext;
import mini.system.ApplicationSystem;
import mini.system.SystemListener;

public class Application implements SystemListener {
    protected ApplicationContext context;

    /**
     * Starts the application in {@link Type#Display display} mode.
     *
     * @see #start(com.jme3.system.JmeContext.Type)
     */
    public void start(){
        start(false);
    }

    /**
     * Starts the application.
     * Creating a rendering context and executing
     * the main loop in a separate thread.
     */
    public void start(boolean waitFor){
        if (context != null && context.isCreated()){
            System.err.println("Warning: start() called when application already created!");
            return;
        }

        System.out.println("Starting application: " + getClass().getName());
        context = ApplicationSystem.newContext();
        context.setSystemListener(this);
        context.create(waitFor);
    }

    /**
     * Do not call manually.
     * Callback from ContextListener.
     */
    public void update(){
        //runQueuedTasks();

        //timer.update();

//        if (inputEnabled){
//            inputManager.update(timer.getTimePerFrame());
//        }

        // user code here..
    }
}
