package dev.martinez;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class HasExcludeFileVisitor extends SimpleFileVisitor<Path> {
    // extend and implement the FileVisitor class hooks so we can have a smoother code-flow.
    // ---
    // I wanted my "if we don't have an 'exclude' pattern, don't exclude any files during walkFileTree()" code branching
    // to be extremely simple, so I had the idea of creating two classes that handled the logic of "have" vs "have not"
    // separately.
    // I wanted the "if we don't have an exclude pattern do x else d y" logic to be seperate from the
    // file visit logic.
    // ---
    // IMO it makes the code a lot more readable, but it does suffer from spreading out the code a little bit. Worth IMO
    private final PathMatcher exclude_pattern;

    public HasExcludeFileVisitor(PathMatcher pattern) {
        this.exclude_pattern = pattern;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
        if(exclude_pattern.matches(dir)){
            return FileVisitResult.SKIP_SUBTREE;
        }
        else{
            return FileVisitResult.CONTINUE;
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
        if(exclude_pattern.matches(file)){
            return FileVisitResult.SKIP_SUBTREE;
        }
        else{
            Orchestrator.pendingWork.add(file);
            return FileVisitResult.CONTINUE;

        }
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc){

        switch(exc){
            case null -> {
                return FileVisitResult.CONTINUE;
            }
            case AccessDeniedException e -> {
                System.err.println("Couldn't access file: " + e.getFile());
                return FileVisitResult.CONTINUE;
            }
            default -> {
                return FileVisitResult.TERMINATE;
            }
        }
    }
}
