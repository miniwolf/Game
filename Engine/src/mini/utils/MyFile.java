package mini.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MyFile {
    private static final String FILE_SEPARATOR = "/";

    private String path;
    private String name;

    public MyFile(String path) {
        this.path = FILE_SEPARATOR + path;
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

    @Override
    public String toString() {
        return getPath();
    }

    public InputStream getInputStream() {
        try {
            return new FileInputStream(Paths.get("out/production/" + path).toFile());
        } catch (FileNotFoundException e) {
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
}
