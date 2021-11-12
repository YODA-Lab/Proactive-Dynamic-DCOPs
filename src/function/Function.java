package function;

import java.io.Serializable;
import java.util.Map;

/**
 * @author khoihd
 *
 */
public abstract class Function implements Serializable {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public enum FunctionType {QUADRATIC, CUBIC}
  
  /**
   * It could be Quadratic or Cubic
   */
  public FunctionType functionType;
  
  public abstract Function evaluate(Map<String, Double> variableValueMap);
}
