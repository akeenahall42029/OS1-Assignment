package barScheduling;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MetricsLogger {
    private int noPatron;
    private int sched;
    private int s;
    private int q;
    private long seed;
    private Patron [] patrons;
    private Barman barman;
    private ConcurrentHashMap<String, String> simulationParams = new ConcurrentHashMap(); // may not need

    MetricsLogger(int noPatrons, int sched, int s, int q, long seed, Barman barman, Patron [] patrons) {
        this.noPatron = noPatrons;
        this.sched = sched;
        this.s = s;
        this.q = q;
        this.seed = seed;
        this.barman = barman;
        this.patrons = patrons;
    }

    /** Calculates average wait time across all patrons' drinks (average wait time of
     * all cpu bursts)
     * @return Average wait time in milliseconds
     * **/
    public double calculateAverageWaitTime(){
        long totalWaitTime = 0;
        for(Patron patron : patrons){
            totalWaitTime += patron.getTotalWaitingTime();
        }
        return (double) totalWaitTime / barman.getCompletedOrders();
    }

    private String getAlgName(){
        return switch (sched){
            case 0 -> "FCFS";
            case 1 -> "SJF";
            case 2 -> "RR";
            default -> "Unknown";
        };
    }

    public void writeToCSV(String filename) throws IOException {
        boolean fileExists = java.nio.file.Files.exists(java.nio.file.Paths.get(filename));

        try (FileWriter writer = new FileWriter(filename, true)) {
            // Write header if new file
            if (!fileExists) {
                writer.write("Algorithm,# of Patrons,Quantum (q),Context Switch (s),Seed," +
                        "Response Time (ms),Throughput (drinks/sec),Average Waiting Time (ms)," +
                        "Turnaround Time (ms),CPU Utilization (%),Completed Orders\n");
            }

            // Write data row
            writer.write(String.format("%s,%d,%d,%d,%d,%d,%.2f,%.2f,%d,%.2f,%d\n",
                    getAlgName(),
                    noPatron,
                    q,
                    s,
                    seed,
                    barman.getResponseTime(),
                    barman.getThroughput(),
                    calculateAverageWaitTime(),
                   // barman.getTurnaroundTime(), --> currently implementing
                    barman.getCPUUtilization(),
                    barman.getCompletedOrders()));
        }
    }


}
