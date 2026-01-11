package dev.martinez;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;

public class MatchAndCountWorker extends MatchAndPrintWorker implements Runnable{
    public MatchAndCountWorker(ArrayList<Path> paths, PathMatcher regex) {
        super(paths, regex);
    }

    // a thread that matches paths and tallies occurrences. Does not add to a list of "matched" paths.
    @Override
    public void run() {
        for(Path candidate : paths){
            if (Orchestrator.seen.contains(candidate)) // skip over paths we have already processed.
                continue;
            if(include.matches(candidate)){
                Orchestrator.addOccurrence();
            }
            Orchestrator.seen.add(candidate);
        }
    }
}
