package agent;

/**
 * @author khoihd
 *
 */
public interface DCOP_INFO {
//	public static final boolean FINISHED = true;
//	public static final boolean NOT_FINSHED = false;
	public static final boolean WAITING_FOR_MSG = true;
//	public static final int C_DPOP = 0;
//	public static final int LS_SDPOP = 1;
//	public static final int LS_RAND = 2;
//	public static final int FORWARD = 3;
//	public static final int BACKWARD = 4;
//	public static final int MULTI_CDPOP = 5;
//	public static final int SDPOP = 6;
//	public static final int REACT = 7;
//	public static final int HYBRID = 8;
	public static final int JESP = 9;
	public static final int GOA = 10;
	
	public static final String algTypes[] = {"C_DPOP", "LS_SDPOP", "LS_RAND", "FORWARD",
										"BACKWARD", "MULTI_CDPOP", "SDPOP", "REACT", "HYBRID"};
	
	//for creating switching cost table
	public static final boolean FORWARD_BOOL = true;
	public static final boolean BACKWARD_BOOL = false;
	
    public static final int MAX_ITERATION = 30;
    
    public static final String switchYes = "yes";
    public static final String switchNo = "no";
    
    public static final int CONSTANT = 0;
    public static final int LINEAR = 1;
    public static final int QUAD = 2;
    public static final int EXP_2 = 3;
    public static final int EXP_3 = 4;
    
    public static final int stableTimeStep = 40;
    public static final int domainSize = 3;
}
