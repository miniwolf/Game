package mini.editor.util;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public interface SimpleFolderVisitor extends SimpleFileVisitor {
    @Override
    default FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        visit(dir, attrs);
        return FileVisitResult.CONTINUE;
    }

    @Override
    default FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }
}
