#!/bin/bash
alg=(C_DPOP LS_SDPOP LS_RAND FORWARD BACKWARD SDPOP REACT HYBRID)
dynamics=(FINITE_HORIZON INFINITE_HORIZON ONLINE)

# java -jar build/ND-DCOP-1.0-jar-with-dependencies.jar $LS_RAND rep_0_d5.dzn 10 15 0.9 FINITE_HORIZON 0.3
topology="random"
jar_file="target/ND-DCOP-1.0-jar-with-dependencies.jar"
dx=3
dy=3

clear
killall -9 java
for algorithm in LS_SDPOP
do
  for decision in 12
  do
    for random in 3
    do
      for horizon in 5
      do
        for switching_cost in 50
        do
          for discount_factor in 0.9
          do
            for dynamic_type in FINITE_HORIZON
            do
              for heuristic_weight in $(seq 0.0 0.1 1.0)
              do
                for instance in {0..14}
                do
                  folder="random_x"$decision"_y"$random"_dx"$dx"_dy"$dy
                  input_file=$folder"/instance_"$instance"_x"$decision"_y"$random"_dx"$dx"_dy"$dy".dzn"
                  java -jar $jar_file $algorithm $input_file $horizon $switching_cost $discount_factor $dynamic_type $heuristic_weight
                  killall -9 java;sleep 1s
                done
              done
            done
          done
        done
      done
    done
  done
done
