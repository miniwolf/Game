package mini.system;

public interface ApplicationContext {
    /**
     * @return True if the context has been created but not yet destroyed.
     */
    boolean isCreated();

    /**
     * Sets the listener that will receive events relating to context
     * creation, update, and destroy.
     */
    public void setSystemListener(SystemListener listener);
}
