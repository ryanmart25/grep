package dev.martinez;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Orchestrator implements Callable<Integer>{
    private final int num_cores = Runtime.getRuntime().availableProcessors();
    private final Path base;
    private final String include_pattern;
    private final String exclude_pattern;
    private static int occurrences;
    public static volatile LinkedBlockingQueue<Path> seen = new LinkedBlockingQueue<>();
    public static volatile LinkedBlockingQueue<Path> matched = new LinkedBlockingQueue<>();
    public static volatile LinkedBlockingQueue<Path> pendingWork = new LinkedBlockingQueue<>();
    private final ExecutorService pool= Executors.newFixedThreadPool(num_cores);
    private boolean SWITCH_ON_PrintOrCount; // true when user wants to print matched paths, false when they
    // just want a count of occurrences.
    public Orchestrator(Path base, String include_pattern, String excludePattern, boolean SWITCH_FOR_PrintOrCount) {
        this.SWITCH_ON_PrintOrCount = SWITCH_FOR_PrintOrCount;
        this.base = base;
        this.include_pattern = include_pattern;
        exclude_pattern = excludePattern;
    }
    public static synchronized void addOccurrence(){
        occurrences++;
    }
    public static synchronized int getOccurrences(){
        return occurrences;
    }
    private void matchAndPrint(PathMatcher include_pattern) throws Exception{
        while(!pendingWork.isEmpty()) {
            // fire off worker threads.
            ArrayList<Path> submittedWork = new ArrayList<>(100);
            pendingWork.drainTo(submittedWork, 100);
            pool.submit(new MatchAndPrintWorker(submittedWork, include_pattern));
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
    }
    private void matchAndTally(PathMatcher include_pattern) throws Exception{
        while(!pendingWork.isEmpty()) {
            // fire off worker threads.
            ArrayList<Path> submittedWork = new ArrayList<>(100);
            pendingWork.drainTo(submittedWork, 100);
            pool.submit(new MatchAndCountWorker(submittedWork, include_pattern));
        }
        // the work queue has been emptied.
        // Worker threads are unable to publish new pending work, so it's safe to shut down the pool.
        pool.shutdown();
        if(!pool.awaitTermination(2, TimeUnit.MINUTES)){
            // This timeout feels way too long, but I don't know how to intelligently decide what it should be.
            pool.shutdownNow();
        }
        System.out.println(getOccurrences());
    }
    @Override
    public Integer call() throws Exception {

        PathMatcher include = FileSystems.getDefault().getPathMatcher("glob:"+ include_pattern);
        PathMatcher exclude = null;
        // the path matcher default to using "glob style" expressions, NOT regex.

        if(exclude_pattern.isEmpty()){ // exclude pattern existence check happens here. I wanted it to be separate from
            // the logic of walking the file tree.
            Files.walkFileTree(base, new NoExcludeFileVisitor());
        }else{
            exclude = FileSystems.getDefault().getPathMatcher("glob:"+exclude_pattern);
            Files.walkFileTree(base, new HasExcludeFileVisitor(exclude));
        }
        if (SWITCH_ON_PrintOrCount)
            matchAndPrint(include);
        else
            matchAndTally(include);

        return 0;
    }
}
