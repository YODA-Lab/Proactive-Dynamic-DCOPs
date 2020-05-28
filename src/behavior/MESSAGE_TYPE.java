package behavior;

import java.util.HashMap;
import java.util.Map;

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
	public static final int PSEUDOTREE = 9;
	public static final int INFO = 10;
	public static final int INIT_LS_UTIL = 11;
	public static final int FINAL_UTIL = 12;
	public static final int FINAL_VALUE = 13;
	public static final int MGM_VALUE = 14;
	public static final int MGM_IMPROVE = 15;
	public static final int MGM_UTIL = 16;
	
	public static final Map<Integer, String> msgTypes = createMap();
	
	static Map<Integer, String> createMap() {
	    Map<Integer,String> myMap = new HashMap<>();
	    myMap.put(DPOP_UTIL, "DPOP_UTIL");
	    myMap.put(DPOP_VALUE, "DPOP_VALUE");
      myMap.put(PROPAGATE_DPOP_VALUE, "PROPAGATE_DPOP_VALUE");
      myMap.put(SWICHING_COST, "SWICHING_COST");
      myMap.put(LS_IMPROVE, "LS_IMPROVE");
      myMap.put(LS_VALUE, "LS_VALUE");
      myMap.put(LS_UTIL, "LS_UTIL");
      myMap.put(RAND_VALUE, "RAND_VALUE");
      myMap.put(PSEUDOTREE, "PSEUDOTREE");
      myMap.put(INFO, "INFO");
      myMap.put(INIT_LS_UTIL, "INIT_LS_UTIL");
      myMap.put(FINAL_UTIL, "FINAL_UTIL");
      myMap.put(FINAL_VALUE, "FINAL_VALUE");
      myMap.put(MGM_VALUE, "MGM_VALUE");
      myMap.put(MGM_IMPROVE, "MGM_IMPROVE");
      myMap.put(MGM_UTIL, "MGM_UTIL");
      
	    return myMap;
	}
}
