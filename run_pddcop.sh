#!/bin/bash
alg=(C_DPOP LS_SDPOP LS_RAND FORWARD BACKWARD SDPOP REACT HYBRID)
dynamics=(FINITE_HORIZON INFINITE_HORIZON ONLINE)

# java -jar build/ND-DCOP-1.0-jar-with-dependencies.jar $LS_RAND rep_0_d5.dzn 10 15 0.9 FINITE_HORIZON 0.3
topology="random"
jar_file="target/ND-DCOP-1.0-jar-with-dependencies.jar"
dx=5
dy=5

clear
killall -9 java
# for pddcop_algorithm in BACKWARD FORWARD LS_SDPOP
# for pddcop_algorithm in HYBRID FORWARD REACT
# for pddcop_algorithm in REACT
for pddcop_algorithm in HYBRID
do
  for dcop_algorithm in DPOP
  do
    # for decision in `seq 50 5 50`
    for decision in 10
    do
      for random in 10
      # for random in "$(($decision/4))"
      do
        for horizon in 10
        do
          # for switching_cost in `seq 0 10 100`
          for switching_cost in 0
          do
            for discount_factor in 1.0
            do
              for dynamic_type in ONLINE
              # for dynamic_type in FINITE_HORIZON
              do
                if [ $pddcop_algorithm = "LS_SDPOP" ] && [ "$dcop_algorithm" = "DPOP" ]; then
                  heuristic_weight=0.6
                else
                  heuristic_weight=0.0
                fi
                for instance in {0..9}
                do
                  folder="random_x"$decision"_y"$random"_dx"$dx"_dy"$dy
                  input_file=$folder"/instance_"$instance"_x"$decision"_y"$random"_dx"$dx"_dy"$dy".dzn"
                  java -jar $jar_file $pddcop_algorithm $dcop_algorithm $input_file $horizon $switching_cost $discount_factor $dynamic_type $heuristic_weight
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
