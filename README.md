# PD-DCOP

The source code for PD-DCOP model is on `master` branch. The source code for DC-DCOP model is on `continuous` branch.

## 1. To compile:
mvn install

## 2. To run:
java -jar $jar_file $pddcop_algorithm $dcop_algorithm $input_file $horizon $switching_cost $discount_factor $dynamic_type $heuristic_weight

Check out `public void readArguments()` in `src/agent/AgentPDDCOP.java` for more information about the arguments.%