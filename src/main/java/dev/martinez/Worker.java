package dev.martinez;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Worker implements Runnable {
    private final ArrayList<Path> paths;
    private final String regex;
    public Worker(ArrayList<Path> paths, Path base, String regex) {
        this.paths = paths;
        this.regex = regex;
    }
    @Override
    public void run() {
        //System.out.println("Thread " + Thread.currentThread().threadId() + " is alive.");
        System.out.println("Thread [" + Thread.currentThread().threadId() + "] has work: ");
        for(Path candidate : paths){
            System.out.println(candidate);
            if (Orchestrator.seen.contains(candidate))
                continue;
            if(Pattern.matches(regex, candidate.toString())){
                System.out.println("Thread " + Thread.currentThread().threadId() +  " Found a match!");
                Orchestrator.matched.add(candidate);
            }
            Orchestrator.seen.add(candidate);
        }
        paths.clear();
        Orchestrator.pendingWork.drainTo(paths, 20);
    }
}
