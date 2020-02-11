package behavior;

/**
 * @author khoihd
 *
 */
public interface MESSAGE_TYPE {
	public static final int DPOP_UTIL = 0;
	public static final int DPOP_VALUE = 1;
	public static final int PROPAGATE_DPOP_VALUE = 2;
	public static final int SWICHING_COST = 3;
	public static final int LS_IMPROVE = 4;
	public static final int LS_VALUE = 5;
	public static final int LS_UTIL = 6;
	public static final int RAND_VALUE = 7;
	public static final int LS_ITERATION_DONE = 8;
	public static final int PSEUDOTREE = 9;
	public static final int INFO = 10;
	public static final int INIT_LS_UTIL = 11;
	public static final String msgTypes[] = {"DPOP_UTIL", "DPOP_VALUE", "PROPAGATE_DPOP_VALUE", "SWICHING_COST",
										"LS_IMPROVE", "LS_VALUE", "LS_UTIL", "RAND_VALUE", "LS_ITERATION_DONE",
										"PSEUDOTREE" , "INFO", "INIT_LS_UTIL"};
}
