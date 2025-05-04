#!/bin/bash

#Test 1 parameters
PATRONS_LIST=(10 20 30 40 50 60 75 100 )
q_LIST=(5 6 10 15  30 50 75 5)
s_LIST=(2 3 4  5  6  10  25  5 )
SEED_LIST=(40 45 55 60 65 65 100 100) #randomly picked randomizer number

echo "=== Recompiling Project ==="
javac -d bin src/barScheduling/*.java
if [ $? -ne 0 ]; then
    echo "Compilation failed! Fix errors before running tests."
    exit 1
fi


for i in "${!PATRONS_LIST[@]}"; do
  PATRONS=${PATRONS_LIST[$i]}
  Q=${q_LIST[$i]}
  S=${s_LIST[$i]}
  SEED=${SEED_LIST[$i]}

  echo "Running tests for P=$PATRONS | CONTEXT SWITCH (S)=$S | QUANTUM (Q) = $Q | Seed=$SEED"

  # FCFS (sched=0)
  java -cp bin barScheduling.SchedulingSimulation $PATRONS 0 $S 0  $SEED > "test_results/FCFS_results.txt"

  # SJF (sched=1)
  java -cp bin barScheduling.SchedulingSimulation $PATRONS 1 $S 0 $SEED > "test_results/SJF_results.txt"

  # RR (sched=2)
  java -cp bin barScheduling.SchedulingSimulation $PATRONS 2 $S $Q $SEED > "test_results/RR_results.txt"

  echo "Done with P=$PATRONS"
done

echo  "All tests completed."