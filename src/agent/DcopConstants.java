package agent;

/**
 * @author khoihd
 *
 */
public interface DcopConstants {
  public static final boolean WAITING_FOR_MSG = true;
  public static final String RANDOM_PREFIX = "y";
  public static final int DEFAULT_BETA_SAMPLING_SEED = 1711;
  public static final int SAMPLING_ITERATION = 1000;
  
  public static enum SwitchingCostFunctionType {
    /**
     *  c(x1, x2) = (x1 - x2)^2
     */
    QUADRATIC
  }
  
  public static enum DcopType {
    DISCRETE,
    CONTINUOUS
  }

  public static enum PDDcopAlgorithm {
    C_DCOP, LS_SDPOP, LS_RAND, FORWARD, BACKWARD, SDPOP, REACT, HYBRID,
    // Decomposed Distributed R Learning (see MD-DCOPs paper)
    R_LEARNING,
    /**
     * Used to solve for the maximal-utility of DCOPs by realizing values of random
     * variables
     */
    BOUND_DPOP,
    GRADIENT
  }

  public static enum DcopAlgorithm {
    DPOP, MGM, MAXSUM, HYBRID_MAXSUM, CAC_MAXSUM, EC_DPOP, AC_DPOP, CAC_DPOP, DISCRETE_DSA, CONTINUOUS_DSA, RANDOMIZE,
    /**
     * Not sure why I implemented this algorithm. Not in use now
     */
    APPROX_DPOP
  }

  public static enum SwitchingType {
    CONSTANT, LINEAR, QUADRATIC, EXP_2, EXP_3
  }

  public static enum DynamicType {
    FINITE_HORIZON, INFINITE_HORIZON, ONLINE, STATIONARY
  }
  
  public static final int FUNC_TO_VAR_TO_SEND_OUT = 0;
  public static final int FUNC_TO_VAR_TO_STORE = 1;
  
  public static final boolean NOT_TO_OPTIMIZE_INTERVAL = false;
  public static final boolean TO_OPTIMIZE_INTERVAL = true;
  
  public static final boolean IS_CLUSTERING = true;
  public static final boolean NOT_CLUSTERING = false;
  
  public static final boolean ADD_MORE_POINTS = true;
  public static final boolean NOT_ADD_POINTS = false;
  
  public static final int MAX_ITERATION = 40;

  public static final int PSEUDOTREE = 0;
  public static final int PSEUDO_INFO = 1;
  public static final int DPOP_UTIL = 2;
  public static final int DPOP_VALUE = 3;
  public static final int VAR_TO_FUNC = 4;
  public static final int FUNC_TO_VAR = 5;
  public static final int PROPAGATE_VALUE = 6;
  public static final int UTILITY_TO_THE_ROOT = 7;
  public static final int MSG_COUNT_TO_THE_ROOT = 8;
  public static final int DSA_VALUE = 9;
  public static final String msgTypes[] = { "PSEUDOTREE", "PSEUDO_INFO", "DPOP_UTIL", "DPOP_VALUE", "VAR_TO_FUNC", "FUNC_TO_VAR",
      "PROPAGATE_VALUE", "UTILITY_TO_THE_ROOT", "MSG_COUNT_TO_THE_ROOT", "DSA_VALUE"};
  
  public static final int DONE_AT_LEAF = 1;
  public static final int DONE_AT_INTERNAL_NODE = 2;
  
  public static final String INPUT_FOLDER = "input_files";
  public static final double GRADIENT_SCALING_FACTOR = Math.pow(10, -3);

  public static final int MARKOV_CONVERGENCE_TIME_STEP = 40;
  public static final boolean RANDOM_TABLE = true;
  public static final boolean DECISION_TABLE = false;
  
  public static final double DSA_PROBABILITY = 0.6;
}
