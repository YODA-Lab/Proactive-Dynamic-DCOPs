#!/bin/bash
dcop_algorithm_list="EC_DPOP AC_DPOP CAC_DPOP DPOP CONTINUOUS_DSA HYBRID_MAXSUM RANDOMIZE"
dcop_algorithm_list="EC_DPOP AC_DPOP CAC_DPOP DPOP CONTINUOUS_DSA HYBRID_MAXSUM"
pddcop_algorithm_list="FORWARD BACKWARD"

# java -jar build/ND-DCOP-1.0-jar-with-dependencies.jar $LS_RAND rep_0_d5.dzn 10 15 0.9 FINITE_HORIZON 0.3
topology="random_tree" # random_graph, random_tree, meeting
jar_file="target/ND-DCOP-1.0-jar-with-dependencies.jar"
dx=100
dy=100
rLearningIteration=100
gradientIteration=100
numberOfPoints=5
dcopType=CONTINUOUS

clear
killall -9 java
for pddcop_algorithm in $pddcop_algorithm_list
# for pddcop_algorithm in FORWARD
do
  for dcop_algorithm in DPOP
  do
    for decision in 10
    do
      for random in 10
      # for random in "$(($decision/5))"
      do
        for horizon in 10
        do
          # for switching_cost in `seq 0 2 10`
	        for switching_cost in 0
          do
            for discount_factor in 0.9
            do
              for dynamic_type in FINITE_HORIZON
              do
                if [ $pddcop_algorithm = "LS_SDPOP" ] && [ "$dcop_algorithm" = "DPOP" ]; then
                  heuristic_weight=0.6
                else
                  heuristic_weight=0.0
                fi
                for instance in {0..0}
                do
                  folder=$topology"_x"$decision"_y"$random"_dx"$dx"_dy"$dy
                  input_file=$folder"/instance_"$instance"_x"$decision"_y"$random"_dx"$dx"_dy"$dy".dzn"
                  java -jar $jar_file $pddcop_algorithm $dcop_algorithm $input_file $horizon $switching_cost $discount_factor $dynamic_type $heuristic_weight $rLearningIteration $gradientIteration $numberOfPoints $dcopType
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
