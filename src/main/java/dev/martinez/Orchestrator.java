package dev.martinez;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class Orchestrator implements Callable<Integer>{
    private final int num_cores = Runtime.getRuntime().availableProcessors();
    private final Path base;
    private final String include_pattern;
    private final String exclude_pattern;
    private int occurrences;
    public static volatile CopyOnWriteArrayList<Path> seen = new CopyOnWriteArrayList<>();
    public static volatile CopyOnWriteArrayList<Path> matched = new CopyOnWriteArrayList<>();
    public static volatile LinkedBlockingQueue<Path> pendingWork = new LinkedBlockingQueue<>();
    private final ExecutorService pool= Executors.newFixedThreadPool(num_cores);
    public Orchestrator(Path base, String include_pattern, String excludePattern) {
        this.base = base;
        this.include_pattern = include_pattern;
        exclude_pattern = excludePattern;
    }

    @Override
    public Integer call() throws Exception {

        Files.walkFileTree(base, new SimpleFileVisitor<>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
                if(!exclude_pattern.isEmpty() && Pattern.matches(exclude_pattern, dir.toString())){
                    return FileVisitResult.SKIP_SUBTREE;
                }
                else{
                    return FileVisitResult.CONTINUE;
                }
            }
            @Override
            public FileVisitResult visitFile(Path dir, BasicFileAttributes attrs) throws IOException{

                if(!exclude_pattern.isEmpty() &&Pattern.matches(exclude_pattern, dir.toString())){
                    return FileVisitResult.SKIP_SUBTREE;
                }else{

                    pendingWork.add(dir);
                    return FileVisitResult.CONTINUE;
                }
            }
            @Override
            public FileVisitResult visitFileFailed(Path dir, IOException e){
                if (e != null){
                    if (e instanceof AccessDeniedException){
                        System.err.println("Encountered folder we don't have access to. ");
                        return FileVisitResult.CONTINUE;
                    }else{
                        return FileVisitResult.TERMINATE;
                    }
                }
                return FileVisitResult.CONTINUE;
            }

        });
        System.out.println("Finished iterating through folder.");
        System.out.println("Created a pool with " + num_cores+ "workers");
        ArrayList<Future> futures = new ArrayList<>();
        while(!pendingWork.isEmpty()) {
            ArrayList<Path> submittedWork = new ArrayList<>(20);
            pendingWork.drainTo(submittedWork, 20);
            futures.add(pool.submit(new Worker(submittedWork, base, include_pattern)));
        }
        for (Future f : futures){
            if(f.get() != null){
                System.out.println("A task is still running");
            }
        }
        System.out.println("Work done!");
        if(!pool.awaitTermination(1, TimeUnit.MINUTES)){
            System.err.println("Forcefully quitting worker threads. Took too long.");
        }
        if(matched.isEmpty()){
            System.out.println("Nothing added!");
        }
        for(Path p : matched){
            System.out.println("grep: " + p);
        }
        return 0;
    }
}
