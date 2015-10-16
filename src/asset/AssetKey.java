package asset;

import java.util.LinkedList;

/**
 * @author miniwolf
 */
public class AssetKey<T> {
    protected String name;
    protected transient String folder;
    protected transient String extension;

    public AssetKey(String name){
        this.name = reducePath(name);
        this.extension = getExtension(this.name);
    }

    public AssetKey(){
    }

    protected static String getExtension(String name) {
        int idx = name.lastIndexOf('.');
        //workaround for filenames ending with xml and another dot ending before that (my.mesh.xml)
        if (name.toLowerCase().endsWith(".xml")) {
            idx = name.substring(0, idx).lastIndexOf('.');
            if (idx == -1) {
                idx = name.lastIndexOf('.');
            }
        }
        if (idx <= 0 || idx == name.length() - 1) {
            return "";
        } else {
            return name.substring(idx + 1).toLowerCase();
        }
    }

    protected static String getFolder(String name) {
        int idx = name.lastIndexOf('/');
        if (idx <= 0 || idx == name.length() - 1) {
            return "";
        } else {
            return name.substring(0, idx + 1);
        }
    }

    /**
     * @return The asset path
     */
    public String getName() {
        return name;
    }

    /**
     * @return The extension of the <code>AssetKey</code>'s name. For example,
     * the name "Interface/Logo/Monkey.png" has an extension of "png".
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @return The folder in which the asset is located in.
     * E.g. if the {@link #getName() name} is "Models/MyModel/MyModel.j3o"
     * then "Models/MyModel/" is returned.
     */
    public String getFolder(){
        if (folder == null)
            folder = getFolder(name);

        return folder;
    }
    /**
     * Removes all relative elements of a path (A/B/../C.png and A/./C.png).
     * @param path The path containing relative elements
     * @return A path without relative elements
     */
    public static String reducePath(String path) {
        if (path == null || path.indexOf("./") == -1) {
            return path;
        }
        String[] parts = path.split("/");
        LinkedList<String> list = new LinkedList<String>();
        for (String string : parts) {
            if (string.length() == 0 || string.equals(".")) {
                //do nothing
            } else if (string.equals("..")) {
                if (list.size() > 0 && !list.getLast().equals("..")) {
                    list.removeLast();
                } else {
                    list.add("..");
                    System.out.println("SEVERE: Asset path is outside assetmanager root");
                }
            } else {
                list.add(string);
            }
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            String string = list.get(i);
            if (i != 0) {
                builder.append("/");
            }
            builder.append(string);
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AssetKey && name.equals(((AssetKey) other).name);
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public String toString(){
        return name;
    }
}
