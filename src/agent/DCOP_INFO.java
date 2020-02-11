package agent;

/**
 * @author khoihd
 *
 */
public interface DCOP_INFO {

  public static final String algTypes[] = { "C_DPOP", "LS_SDPOP", "LS_RAND", "FORWARD", "BACKWARD", "MULTI_CDPOP", "SDPOP",
      "REACT", "HYBRID" };

  // for creating switching cost table
  public static final boolean FORWARD_BOOL = true;
  public static final boolean BACKWARD_BOOL = false;

  public static final int MAX_ITERATION = 30;

  public static final String switchYes = "yes";
  public static final String switchNo = "no";

  public static final int MARKOV_CONVERGENCE_TIME_STEP = 40;
}
