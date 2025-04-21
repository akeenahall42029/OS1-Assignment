#!/bin/bash

#Test 1 parameters
PATRONS=10 
q=5
s=5
seed=40 #randomly picked randomizer number 

echo "Running test 1 with:" 
echo "- Patrons: $PATRONS"
echo "-Quantum (q): $Q" 
echo "-Context Switch (s): $s"
echo "Seed (randomizer) $seed" 
echo "-------------------------" 

#Test FCFS (sched = 0)
echo "Testing FCFS..." 
java -cp ../bin barScheduling.SchedulingSimmulation $PATRONS 0 0 $s $seed > ../test_results/FCFS_results.txt
#doesn't need a q value

#Test SJF (sched = 1)
echo "Testing SJF..."
java -cp ../bin barScheduling.SchedulingSimmulation $PATRONS 1 0 $s $seed > ../test_results/SJF_results.txt
#doesn't need a q value

#Test RR (sched = 2)
echo "Testing RR..."
java -cp ../bin barScheduling.SchedulingSimmulation $PATRONS 2 5 $s $seed > ../test_results/RR_results.txt

echo "------------------------"
echo "Tests completed. Results saved" 
