package dev.martinez;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;

public class NoExcludeFileVisitor extends SimpleFileVisitor<Path> {
    // extend and implement the FileVisitor class hooks so we can have a smoother code-flow.
    // ---
    // I wanted my "if we don't have an 'exclude' pattern, don't exclude any files during walkFileTree()" code branching
    // to be extremely simple, so I had the idea of creating two classes that handled the logic of "have" vs "have not"
    // separately.
    // I wanted the "if we don't have an exclude pattern do x else d y" logic to be seperate from the
    // file visit logic.
    // ---
    // IMO it makes the code a lot more readable, but it does suffer from spreading out the code a little bit. Worth IMO
    public NoExcludeFileVisitor(){}
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        if (exc != null){
            if (exc instanceof AccessDeniedException){
                System.err.println("Encountered folder we don't have access to. ");
                return FileVisitResult.CONTINUE;
            }else{
                return FileVisitResult.TERMINATE;
            }
        }
        return FileVisitResult.CONTINUE;
    }
}
