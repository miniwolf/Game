package mini.editor.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public interface SimpleFileVisitor extends FileVisitor<Path> {
    @Override
    default FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    default FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        visit(file, attrs);
        return FileVisitResult.CONTINUE;
    }

    @Override
    default FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    default FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    /**
     * Visit the file
     *
     * @param file  the file.
     * @param attrs the attributes of the file.
     */
    void visit(Path file, BasicFileAttributes attrs);
}
