package zexception;

public class FunctionException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = -731809793907265063L;

  public FunctionException(){
      super();
  }

  public FunctionException(String message){
      super(message);
  }
}
