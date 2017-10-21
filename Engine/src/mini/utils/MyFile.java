package mini.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MyFile {
    private static final String FILE_SEPARATOR = "/";

    private String path;
    private String name;

    public MyFile(String path) {
        this.path = path;
        String[] dirs = path.split(FILE_SEPARATOR);
        this.name = dirs[dirs.length - 1];
    }

    public MyFile(String... paths) {
        StringBuilder builder = new StringBuilder();
        for (String part : paths) {
            builder.append(FILE_SEPARATOR).append(part);
        }
        path = builder.toString().substring(1);
        String[] dirs = path.split(FILE_SEPARATOR);
        this.name = dirs[dirs.length - 1];
    }

    public MyFile(MyFile file, String subFile) {
        this.path = file.path + FILE_SEPARATOR + subFile;
        this.name = subFile;
    }

    public MyFile(MyFile file, String... subFiles) {
        StringBuilder newPath = new StringBuilder(file.path);
        for (String part : subFiles) {
            newPath.append(FILE_SEPARATOR).append(part);
        }
        this.path = newPath.toString();
        String[] dirs = path.split(FILE_SEPARATOR);
        this.name = dirs[dirs.length - 1];
    }

    public String getPath() {
        return path;
    }

    public String getDirectory() {
        return path.substring(0, path.indexOf(name));
    }

    public String getFullPathDirectory() {
        return ClassLoader.getSystemResource(getDirectory()).getPath();
    }

    @Override
    public String toString() {
        return getPath();
    }

    public InputStream getInputStream() {
        URL systemResource = ClassLoader.getSystemResource(this.path);
        try {
            Path path = Paths.get(systemResource.toURI());
            return new FileInputStream(path.toFile());
        } catch (NullPointerException e) {
            System.err.println("Could not load " + this.path);
        } catch (FileNotFoundException e) {
            System.out.println(Paths.get("/").toFile().getAbsolutePath());
            e.printStackTrace();
        } catch (URISyntaxException e) {
            System.out.println(systemResource.toString());
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getLines() throws IOException {
        return Files.lines(Paths.get("out/production/" + path)).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    // TODO: Implement constant time operation instead of this O(m) operation.
    // Initialise the value in the beginning as this value is immutable
    public String getExtension() {
        String[] fileType = path.split("\\.");
        return fileType[fileType.length - 1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MyFile myFile = (MyFile) o;
        return Objects.equals(path, myFile.path) &&
               Objects.equals(name, myFile.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(path, name);
    }
}
