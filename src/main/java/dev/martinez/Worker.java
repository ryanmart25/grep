package dev.martinez;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;

public class Worker implements Runnable {
    // a simple PatternMatching thread. Does not publish new work to the pending work queue.
    private final ArrayList<Path> paths;
    private final PathMatcher include;
    public Worker(ArrayList<Path> paths, PathMatcher regex) {
        this.paths = paths;
        this.include = regex;
    }
    @Override
    public void run() {
        // iterates through the list of paths it has received and attempts to match them to the include pattern 'include'.
        // processed paths are added to the "seen" collection.
        for(Path candidate : paths){
            if (Orchestrator.seen.contains(candidate)) // skip over paths we have already processed.
                continue;
            if(include.matches(candidate)){
                Orchestrator.matched.add(candidate);
            }
            Orchestrator.seen.add(candidate);
        }
    }
}
