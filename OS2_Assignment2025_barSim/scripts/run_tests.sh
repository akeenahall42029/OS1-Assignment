#!/bin/bash

# Test parameters
PATRONS_LIST=(10 20 30 40 50 60 75 100)
q_LIST=(10 15 20 25 30 35 40 45)
s_LIST=(1 2 3 4 5 6 7 8)
SEED_LIST=(40 45 55 60 65 65 100 100)

echo "=== Recompiling Project ==="
javac -d bin src/barScheduling/*.java
if [ $? -ne 0 ]; then
    echo "Compilation failed! Fix errors before running tests."
    exit 1
fi

# Loop over each patron count
for i in "${!PATRONS_LIST[@]}"; do
  PATRONS=${PATRONS_LIST[$i]}
  SEED=${SEED_LIST[$i]}

  echo ">> Testing P=$PATRONS | SEED=$SEED"

  # Loop over each context switch value
  for S in "${s_LIST[@]}"; do
    echo "  > Context Switch (S) = $S"

    # FCFS (sched=0)
    java -cp bin barScheduling.SchedulingSimulation $PATRONS 0 $S 0 $SEED > "test_results/FCFS_results.txt"

    # SJF (sched=1)
    java -cp bin barScheduling.SchedulingSimulation $PATRONS 1 $S 0 $SEED > "test_results/SJF_results.txt"

    # RR (sched=2) → loop over quantum values
    for Q in "${q_LIST[@]}"; do
      echo "    → RR test for quantum $Q"
      java -cp bin barScheduling.SchedulingSimulation $PATRONS 2 $S $Q $SEED > "test_results/RR_results.txt"
    done
  done

  echo "Done with all S and Q values for P=$PATRONS"
done

echo "=== All tests completed ==="
