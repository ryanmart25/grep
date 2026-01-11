package dev.martinez;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Orchestrator implements Callable<Integer>{
    private final int num_cores = Runtime.getRuntime().availableProcessors();
    private final Path base;
    private final String include_pattern;
    private final String exclude_pattern;
    private int occurrences;
    public static volatile LinkedBlockingQueue<Path> seen = new LinkedBlockingQueue<>();
    public static volatile LinkedBlockingQueue<Path> matched = new LinkedBlockingQueue<>();
    public static volatile LinkedBlockingQueue<Path> pendingWork = new LinkedBlockingQueue<>();
    private final ExecutorService pool= Executors.newFixedThreadPool(num_cores);
    public Orchestrator(Path base, String include_pattern, String excludePattern) {
        this.base = base;
        this.include_pattern = include_pattern;
        exclude_pattern = excludePattern;
    }

    @Override
    public Integer call() throws Exception {

        PathMatcher include = FileSystems.getDefault().getPathMatcher("glob:"+ include_pattern);
        PathMatcher exclude = null;
        if(!exclude_pattern.isEmpty()){ // exclude pattern existence check happens here. I wanted it to be separate from
            // the logic of walking the file tree.
            exclude = FileSystems.getDefault().getPathMatcher("glob:"+exclude_pattern);
            Files.walkFileTree(base, new HasExcludeFileVisitor(exclude));
        }else{
            Files.walkFileTree(base, new NoExcludeFileVisitor());
        }

        while(!pendingWork.isEmpty()) {
            // fire off worker threads.
            ArrayList<Path> submittedWork = new ArrayList<>(100);
            pendingWork.drainTo(submittedWork, 100);
            pool.submit(new Worker(submittedWork, include));
        }
        // the work queue has been emptied.
        // Worker threads are unable to publish new pending work, so it's safe to shut down the pool.
        pool.shutdown();
        if(!pool.awaitTermination(2, TimeUnit.MINUTES)){
            // This timeout feels way too long, but I don't know how to intelligently decide what it should be.
            pool.shutdownNow();
        }
        if(matched.isEmpty()){
            System.out.println("Nothing found!");
        }
        for(Path p : matched){
            System.out.println("fzf: " + p);
        }
        return 0;
    }
}
