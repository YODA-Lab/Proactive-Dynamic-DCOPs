#!/bin/bash
dcop_algorithm_full_list="EC_DPOP AC_DPOP CAC_DPOP DPOP CONTINUOUS_DSA HYBRID_MAXSUM RANDOMIZE"
dcop_algorithm_list="AC_DPOP CAC_DPOP CONTINUOUS_DSA HYBRID_MAXSUM"
pddcop_algorithm_list="FORWARD BACKWARD"

# java -jar build/ND-DCOP-1.0-jar-with-dependencies.jar $LS_RAND rep_0_d5.dzn 10 15 0.9 FINITE_HORIZON 0.3
topology="random_graph" # random_graph, random_tree, meeting
jar_file="target/ND-DCOP-1.0-jar-with-dependencies.jar"
dx=100
dy=100
discount_factor=0.9
gradientIteration=20
numberOfPoints=5
dcopType=CONTINUOUS

clear
killall -9 java
for pddcop_algorithm in $pddcop_algorithm_list
# for pddcop_algorithm in FORWARD
do
  for dcop_algorithm in $dcop_algorithm_list
  do
    for decision in 20
    do
      for random in 20
      # for random in "$(($decision/5))"
      do
        for horizon in 10
        do
	        for switching_cost in 1
          do
            for dynamic_type in FINITE_HORIZON
            do
              for instance in {0..9}
              do
                folder=$topology"_x"$decision"_y"$random"_dx"$dx"_dy"$dy
                input_file=$folder"/instance_"$instance"_x"$decision"_y"$random"_dx"$dx"_dy"$dy".dzn"
                java -jar $jar_file $pddcop_algorithm $dcop_algorithm $input_file $horizon $switching_cost $discount_factor $dynamic_type $gradientIteration $numberOfPoints $dcopType
                killall -9 java;sleep 1s
              done
            done
          done
        done
      done
    done
  done
done
