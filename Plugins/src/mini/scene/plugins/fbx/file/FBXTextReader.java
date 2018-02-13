package mini.scene.plugins.fbx.file;

import mini.asset.AssetInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FBXTextReader {
    private final AssetInfo info;
    private final FBXFile file;
    private int level = 0;

    public FBXTextReader(AssetInfo info, FBXFile file) {
        this.info = info;
        this.file = file;
    }

    public FBXFile readFBX() throws IOException {
        try (InputStream in = info.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith(";")) {
                    continue;
                }

                file.addElement(readFBXElement(line, reader));
            }

            in.close();
        }
        return file;
    }

    private FBXElement readFBXElement(String description, BufferedReader reader)
            throws IOException {
        boolean hasChildren = false;
        if (description.contains("{")) {
            hasChildren = true;
        }
        String[] split = description.split(":");
        if (split.length == 1) {
            FBXElement fbxElement = new FBXElement(0);
            fbxElement.setName(trim(split[0]));
            return fbxElement;
        }
        String trimmedDescription = String.join(":", Stream.of(description.split("[:{]"))
                                                           .skip(1).collect(Collectors.toList()))
                                          .trim();
        FBXElement fbxElement;
        if (trimmedDescription.matches("(\"[^,]+\")|[0-9]+")) {
            fbxElement = new FBXElement(1);
            fbxElement.addProperty(cleanType(trimmedDescription, trimmedDescription));
            fbxElement.addPropertyType(findType(trimmedDescription), 0); // TODO: Figure this out
        } else {
            String[] properties = trimmedDescription.split(",");
            int count = properties.length == 1 ? 0 : properties.length;
            fbxElement = new FBXElement(count);
            for (int i = 0; i < count; i++) {
                String property = properties[i];
                fbxElement.addProperty(cleanType(property, trimmedDescription));
                fbxElement.addPropertyType(findType(property), i); // TODO: Figure this out
            }
        }

        fbxElement.setName(trim(split[0]));

        if (hasChildren) {
            String line;
            // TODO: maybe a bit over-excessive to assume bad standard with { and } in comments
            while (!(line = reader.readLine()).contains("}")) {
                if (line.isEmpty() || line.contains(";")) {
                    continue;
                }
                fbxElement.addChild(readFBXElement(line, reader));
            }
        }

        return fbxElement;
    }

    private Object cleanType(String string, String trimmedDescription) {
        if (string.contains("\"")) {
            String[] split = string.split("\"");
            if (split.length == 1) {
                return "";
            } else {
                return split[1];
            }
        }
        if (trimmedDescription.contains("Vector3D") || trimmedDescription.contains("double")
            || string.contains(".")) {
            return Double.parseDouble(string);
        }
        if (trimmedDescription.contains("bool")) {
            return Integer.parseInt(string);
        }
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Long.parseLong(string);
        } catch (NumberFormatException ignored) {
        }
        System.err.println("Don't know: " + trimmedDescription);
        return string;
    }

    private String trim(String string) {
        while (string.startsWith("\t")) {
            string = string.substring(1, string.length());
        }
        return string.trim();
    }

    private char findType(String property) {
        if (property.contains("\"")) {
            return 'S';
        } else {
            try {
                Byte.parseByte(property);
                return 'C';
            } catch (NumberFormatException ignored) {
            }
            try {
                Short.parseShort(property);
                return 'Y';
            } catch (NumberFormatException ignored) {
            }
            try {
                Integer.parseInt(property);
                return 'I';
            } catch (NumberFormatException ignored) {
            }
            try {
                Float.parseFloat(property);
                return 'F';
            } catch (NumberFormatException ignored) {
            }
            try {
                Double.parseDouble(property);
                return 'D';
            } catch (NumberFormatException ignored) {
            }
            try {
                Long.parseLong(property);
                return 'L';
            } catch (NumberFormatException ignored) {
            }
        }
        throw new UnsupportedOperationException("Unknown data property");
    }
}