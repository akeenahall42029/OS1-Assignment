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

    public double calculateAverageResponseTime(){
        long totalResponseTime = 0;
        for(Patron patron : patrons){
            totalResponseTime+= patron.getFirstDrinkResponseTime();
        }
        return (double) totalResponseTime / patrons.length;
    }


    public double[] calculateThroughputOverWindows(int windowCount) {
        long start = barman.getSimStartTime();
        long end = barman.getSimEndTime();
        long duration = end - start;

        if (duration <= 0) duration = 1; // Prevent division by zero

        long windowSize = duration / windowCount;
        int[] patronsPerWindow = new int[windowCount];

        // Count number of patrons that completed in each window
        for (Patron patron : patrons) {
            long completion = patron.getCompletetionTime();
            int index = (int)((completion - start) / windowSize);
            if (index >= windowCount) index = windowCount - 1; // Clamp to last window
            patronsPerWindow[index]++;
        }

        // Calculate throughput in patrons/sec per window
        double[] throughputs = new double[windowCount];
        for (int i = 0; i < windowCount; i++) {
            throughputs[i] = ((double) patronsPerWindow[i] / windowSize) * 1000; // patrons/sec
            System.out.printf("Window %d: %.3f patrons/sec (%d patrons)\n",
                    i + 1, throughputs[i], patronsPerWindow[i]);
        }

        return throughputs;
    }


    public double calculateAverageTurnaroundTime(){
        long totalTurnaround = 0;
        for (Patron patron : patrons) {
            totalTurnaround += patron.getTurnaroundTime();
        }
        return (double) totalTurnaround/patrons.length;
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
        double[] throughputs = calculateThroughputOverWindows(4);
        try (FileWriter writer = new FileWriter(filename, true)) {
            if (!fileExists) {
                writer.write("Algorithm,# of Patrons,Context Switch (s),Quantum (q),Seed," +
                        "Avg Response Time (ms),Avg Waiting Time (ms),Avg Turnaround Time (ms)," +
                        "Throughput_25% (patrons/sec),Throughput_50% (patrons/sec)," +
                        "Throughput_75% (patrons/sec),Throughput_100% (patrons/sec),CPU Utilization,Completed Orders\n");
            }

            writer.write(String.format("%s,%d,%d,%d,%d,%.2f,%.2f,%.2f,%.6f,%.6f,%.6f,%.6f,%.2f,%d\n",
                    getAlgName(),
                    noPatron,
                    s,
                    q,
                    seed,
                    calculateAverageResponseTime(),
                    calculateAverageWaitTime(),
                    calculateAverageTurnaroundTime(),
                    throughputs[0],
                    throughputs[1],
                    throughputs[2],
                    throughputs[3],
                    barman.getCPUUtilization(),
                    barman.getCompletedOrders()
            ));
        }
    }

    public void printMetrics() {
        double[] throughputs = calculateThroughputOverWindows(4);

        System.out.printf(
                "Algorithm: %s | # of Patrons: %d | Context Switch (s): %d | Quantum (q): %d | Seed: %d | " +
                        "Avg Response Time (ms): %.2f | Avg Wait Time (ms): %.2f | Avg Turnaround Time (ms): %.2f | " +
                        "TP_25%%: %.3f | TP_50%%: %.3f | TP_75%%: %.3f | TP_100%%: %.3f | " +
                        "CPU Util: %.2f%% | Completed Orders: %d\n",
                getAlgName(),
                noPatron,
                s,
                q,
                seed,
                calculateAverageResponseTime(),
                calculateAverageWaitTime(),
                calculateAverageTurnaroundTime(),
                throughputs[0],
                throughputs[1],
                throughputs[2],
                throughputs[3],
                barman.getCPUUtilization(),
                barman.getCompletedOrders()
        );
    }



}
