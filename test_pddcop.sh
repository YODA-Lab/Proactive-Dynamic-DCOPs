#!/bin/bash
# alg=rdiff,cdiff,base


# java -jar build/ND-DCOP-1.0-jar-with-dependencies.jar $LS_RAND rep_0_d5.dzn 10 15 0.9 FINITE_HORIZON 0.3
topology="random"
jar_file="target/ND-DCOP-1.0-jar-with-dependencies.jar"

for algorithm in "LS_RAND"
do
  for decision in 5
  do
    for random in 4
    do
      for instance in 0
      do
        folder="random_x"$decision"_y"$random
        input_file=$folder"/instance_"$instance"_x"$decision"_y"$random".dzn"
        for horizon in 10
        do
          for switching_cost in 15
          do
            for discount_factor in 0.9
            do
              for dynamic_type in FINITE_HORIZON
              do
                for heuristic_weight in 0.3
                do
                  java -jar $jar_file $algorithm $input_file $horizon $switching_cost $discount_factor $dynamic_type $heuristic_weight
                done
              done
            done
          done
        done
      done
    done
  done
done
