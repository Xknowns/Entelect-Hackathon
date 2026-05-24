package za.co.entelect;

import java.nio.file.*;

/*
 * <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>
 */

public class Main {
    public static void main(String[] args) throws Exception {
        // Read input JSON
        String inputJson;
        if (args.length > 0) {
            inputJson = Files.readString(Path.of(args[0]));
        } else {
            inputJson = new String(System.in.readAllBytes());
        }
        
        // Parse level data
        JsonLoader.RaceData data = JsonLoader.loadLevel(inputJson);
        
        System.out.println("=== Entelect F1 Strategist ===");
        System.out.println("Track: " + data.track.name);
        System.out.println("Laps: " + data.config.laps);
        System.out.println("Segments: " + data.track.segments.size());
        System.out.println("Available tyres: " + data.tyres.size());
        
        // Optimize strategy
        Optimizer optimizer = new Optimizer(data);
        Strategy bestStrategy = optimizer.optimize();
        
        // Generate output
        String submission = JsonOutput.generateSubmission(bestStrategy, data.config);
        
        // Print to stdout (this is your submission)
        System.out.println("\n=== SUBMISSION ===");
        System.out.println(submission);
        
        // Also write to file
        Files.writeString(Path.of("submission.txt"), submission);
        System.out.println("\nSaved to submission.txt");
    }
}
