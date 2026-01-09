package dev.martinez;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

// recursively searches through your file system starting from the directory that this command is being called from
@Command(name = "grep", mixinStandardHelpOptions = true, version = "grep 0.0.1",
        description = "searches through your file system following the provided pattern. Outputs results to STDOUT")
public class Grep implements Callable<Integer> {
    @Parameters(index = "0",description = "The pattern to search for.")
    private String pattern = "";
    @Option(names= {"-d", "--directory"}, description = "start search from given directory.")
    private String starting_directory = "";
    @Option(names = {"-e", "--exclude"}, description = "exclude directories that match the expression.")
    private String exclude = "";
    public static void main(String[] args) {
        int exit_code = new CommandLine(new Grep()).execute(args);
        System.exit(exit_code);
    }

    @Override
    public Integer call() throws Exception {
        try{
            Path base_path = Path.of("").toAbsolutePath().normalize();
            Pattern include_pattern = Pattern.compile(pattern);
            Pattern exclude_pattern;
            if (!exclude.equals("")){
                exclude_pattern= Pattern.compile(exclude);
            }

            if (starting_directory != null) {
                base_path= Path.of(starting_directory).toAbsolutePath().normalize();
            }
            var queen_bee = new Orchestrator(base_path, pattern, exclude);
            queen_bee.call();
            return 0;
        }catch (SecurityException e){
            System.err.println("Security exception: " + e);
            return -1;
        }catch(PatternSyntaxException e){
            System.err.println("Invalid regex pattern.");
            return -1;
        }
    }
}