package mini.system;

public class MockApplicationSystemDelegate extends ApplicationSystemDelegate {
    @Override
    public void showErrorDialog(String message) {
    }

    @Override
    public ApplicationContext newContext() {
        return null;
    }
}
