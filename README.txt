# Welcome to **Sarah the Zombie Barman**
## Sarah the Zombie Barman Simulation
### Author: Akeena Hall
This project is a CPU scheduling simulation that tests the efficiency of FCFS, SJF, and RR.
Using standard scheduling metrics such as average waiting time, average turnaround time, average response time, throughput, and CPU utilization,
it determines which algorithm is best for Sarah to use to ensure high customer satisfaction!


## Configuration Steps

Please set your running project to S2_Assignment2025_barSim\OS2_Assignment2025_barSim. If not run from this location
the logger will fail to write! This also applies when using "make run" or shell scripts.

## Using the MakeFile

Once in the appropriate directory, simply run make run ARGS="30 1 5 30 in your terminal for one simulation walkthrough!

## Running the Shell Scripts

This project contains two helpful, robust shell scripts to help with algorithm analysis. Both tests produce large data samples,
with   ```
          run_Tests.sh
         ``` producing 642 simulation results, and
           ```
              run_qTests.sh
             ``` producing 196. Both take at least
15 minutes to run.
1. Make the script executable
    - chmod +x scripts/run_qTests.sh
2. Run using the command
    - ./scripts/run_qTests.sh OR
    - ./scripts/run_tests.sh

## Have a Happy, Happy Hour!

##Thank you!
