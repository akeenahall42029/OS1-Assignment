#!/bin/bash

# Define input arrays
PATRONS_LIST=(10 20 30 40 50 60 75 100)
SEED_LIST=(40 45 55 60 65 65 100 100)
Q_VALUES=(45 50 55 65 75 85 90 95)
ALGORITHMS=(0 1 2) # 0=FCFS, 1=SJF, 2=RR
S=1

# Check for matching list lengths
if [ ${#PATRONS_LIST[@]} -ne ${#SEED_LIST[@]} ]; then
  echo "Error: PATRONS_LIST and SEED_LIST must have the same length."
  exit 1
fi

# Run simulation for each combo
for ((i=0; i<${#PATRONS_LIST[@]}; i++)); do
  PATRONS=${PATRONS_LIST[$i]}
  SEED=${SEED_LIST[$i]}

  for Q in "${Q_VALUES[@]}"; do
    for ALG in "${ALGORITHMS[@]}"; do
      echo "Running: Patrons=$PATRONS, Alg=$ALG, S=$S, Q=$Q, Seed=$SEED"
      java -cp bin barScheduling.SchedulingSimulation "$PATRONS" "$ALG" "$S" "$Q" "$SEED"
    done
  done
done
