#!/bin/bash
alg=(C_DPOP LS_SDPOP LS_RAND FORWARD BACKWARD SDPOP REACT HYBRID)
dynamics=(FINITE_HORIZON INFINITE_HORIZON ONLINE)

# java -jar build/ND-DCOP-1.0-jar-with-dependencies.jar $LS_RAND rep_0_d5.dzn 10 15 0.9 FINITE_HORIZON 0.3
topology="random"
jar_file="target/ND-DCOP-1.0-jar-with-dependencies.jar"

decisionDomain=3
randomDomain=3

clear
killall -9 java
# for algorithm in LS_RAND LS_SDPOP FORWARD BACKWARD
for algorithm in C_DPOP
do
  for decision in 10
  do
    for random in 5
    do
      for instance in 0
      do
        folder="random_x"$decision"_y"$random"_dx"$decisionDomain"_dy"$randomDomain
        input_file=$folder"/instance_"$instance"_x"$decision"_y"$random"_dx"$decisionDomain"_dy"$randomDomain".dzn"
        for horizon in 1
        do
          for switching_cost in 1000
          do
            for discount_factor in 0.7
            do
              for dynamic_type in FINITE_HORIZON
              do
                for heuristic_weight in 0
                do
                  java -jar $jar_file $algorithm $input_file $horizon $switching_cost $discount_factor $dynamic_type $heuristic_weight
                  killall -9 java
                  sleep 1s
                done
              done
            done
          done
        done
      done
    done
  done
done
