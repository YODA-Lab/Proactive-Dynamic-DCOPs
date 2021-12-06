package ztest;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import function.Interval;
import function.multivariate.MultivariateQuadFunction;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import table.RowDouble;
import table.TableDouble;

class TestFunction {
//  @Test
//  void testHybridDPOP() {
//    double minBound = 1;
//    double maxBound = 100;
//    Interval globalInterval = new Interval(minBound, maxBound);
//    
//    /*
//     * Create two tables
//     * Create a functions
//     * Do simple interpolation (weighted nearest neighbors)
//     * Generate more points (think about this, do later)
//     * 
//     */
//    PiecewiseMultivariateQuadFunction f12_pw = new PiecewiseMultivariateQuadFunction();
//    MultivariateQuadFunction f12 = new MultivariateQuadFunction();
//    f12.getCoefficients().put("1", "1", 1.0);
//    f12.getCoefficients().put("1", "", 1.0);    
//    f12.getCoefficients().put("2", "2", 4.0);
//    f12.getCoefficients().put("2", "", 2.0);
//    f12.getCoefficients().put("1", "2", 2.0);
//    f12.getCoefficients().put("", "", 3.0);
//    Map<String, Interval> intervalMap12 = new HashMap<>();
//    intervalMap12.put("1", globalInterval);
//    intervalMap12.put("2", globalInterval);
//    f12_pw.addToFunctionMapWithInterval(f12, intervalMap12, false);
//    f12.setOwner("2");
//    
//    PiecewiseMultivariateQuadFunction f13_pw = new PiecewiseMultivariateQuadFunction();
//    MultivariateQuadFunction f13 = new MultivariateQuadFunction();
//    f13.getCoefficients().put("1", "1", 1.0);
//    f13.getCoefficients().put("1", "", 1.0);    
//    f13.getCoefficients().put("3", "3", 9.0);
//    f13.getCoefficients().put("3", "", 3.0);
//    f13.getCoefficients().put("1", "3", 3.0);
//    f13.getCoefficients().put("", "", 4.0);
//    Map<String, Interval> intervalMap13 = new HashMap<>();
//    intervalMap13.put("1", globalInterval);
//    intervalMap13.put("3", globalInterval);
//    f13_pw.addToFunctionMapWithInterval(f13, intervalMap13, false);
//    f13.setOwner("3");
//    
//    PiecewiseMultivariateQuadFunction f23_pw = new PiecewiseMultivariateQuadFunction();
//    MultivariateQuadFunction f23 = new MultivariateQuadFunction();
//    f23.getCoefficients().put("2", "2", 4.0);
//    f23.getCoefficients().put("2", "", 2.0);    
//    f23.getCoefficients().put("3", "3", 9.0);
//    f23.getCoefficients().put("3", "", 3.0);
//    f23.getCoefficients().put("2", "3", 6.0);
//    f23.getCoefficients().put("", "", 5.0);
//    f23.setOwner("3");
//
//    Map<String, Interval> intervalMap23 = new HashMap<>();
//    intervalMap23.put("2", globalInterval);
//    intervalMap23.put("3", globalInterval);
//    f23_pw.addToFunctionMapWithInterval(f23, intervalMap23, false);
//      
//    PiecewiseMultivariateQuadFunction f24_pw = new PiecewiseMultivariateQuadFunction();
//    MultivariateQuadFunction f24 = new MultivariateQuadFunction();
//    f24.getCoefficients().put("2", "2", 4.0);
//    f24.getCoefficients().put("2", "", 2.0);    
//    f24.getCoefficients().put("4", "4", 16.0);
//    f24.getCoefficients().put("4", "", 4.0);
//    f24.getCoefficients().put("2", "4", 8.0);
//    f24.getCoefficients().put("", "", 6.0);
//    f24.setOwner("4");
//    Map<String, Interval> intervalMap24 = new HashMap<>();
//    intervalMap24.put("2", globalInterval);
//    intervalMap24.put("4", globalInterval);
//    f24_pw.addToFunctionMapWithInterval(f24, intervalMap24, false);
//
//    
//    ///////////////////////////////////////////////////////////////////////////////////////
//    // AGENT 3 DO THE GRADIENT ON X1 AND X2 AT THE SAME TIME
////    v1=4.103543, v2=4.675046, max=291.253351
////    v1=3.909459, v2=2.226753, max=250.243753
////    v1=1.623590, v2=3.471825, max=251.765419
//    
////    v2=5.394787, max=373.998955
////    v2=2.209659, max=198.151543
////    v2=3.802223, max=281.002727
//    
//    List<String> label12 = new ArrayList<>();
//    label12.add("1"); label12.add("2");
//    Table util32 = new Table(label12);
//    List<Double> values = new ArrayList<>();
//    values.add(4.103543);
//    values.add(4.675046);
//    util32.addRow(new Row(values, 291.253351));
//    values.clear();
//    values.add(3.909459);
//    values.add(2.226753);
//    util32.addRow(new Row(values, 250.243753));
//    values.clear();
//    values.add(1.623590);
//    values.add(3.471825);
//    util32.addRow(new Row(values, 251.765419));
//    values.clear();
//    
//    List<String> label2 = new ArrayList<>();
//    label2.add("2");
//    Table util42 = new Table(label2);
//    values.add(5.394787);
//    util42.addRow(new Row(values, 373.998955));
//    values.clear();
//    values.add(2.209659);
//    util42.addRow(new Row(values, 198.151543));
//    values.clear();
//    values.add(3.802223);
//    util42.addRow(new Row(values, 281.002727));
//    values.clear();
//    
////    values.add(5.394787);
//    values.add(2.209659);
//    values.add(3.802223);
//
//    util32.interpolate32(values);
//    values.clear();
//    out.println("Util 3=>2 after interpolation");
//    out.println(util32);
//    
//    values.add(4.675046);
//    values.add(2.226753);
//    values.add(3.471825);
// 
//    util42.interpolate42(values);
//    out.println("Util 4=>2 after interpolation");
//    out.println(util42);
//    
//    Table joinedTable = DPOP_UTIL.joinTable(util32, util42);
//    out.println("Joined table");
//    out.println(joinedTable);
//    
//    for (Row row : joinedTable.getRowSet()) {
//      Map<String, Double> valueMap = new HashMap<>();
//      List<Double> valueList = row.getValueList();
//      valueMap.put("1", valueList.get(0));
//      valueMap.put("2", valueList.get(1));
//      row.setUtility(row.getUtility() + f12.evaluateToValueGivenValueMap(valueMap));
//    }
//    
//    out.println("Adding f12 to the joined table");
//    out.println(joinedTable);
//    
//    // Test adding functions after evaluating
//    PiecewiseMultivariateQuadFunction newPw23 = new PiecewiseMultivariateQuadFunction();
//    newPw23.addToFunctionMapWithIntervalSet(f23_pw.getTheFirstFunction().evaluate("3", 9), f23_pw.getTheFirstIntervalSet());
//    
//    PiecewiseMultivariateQuadFunction newPw24 = new PiecewiseMultivariateQuadFunction();
//    newPw24.addToFunctionMapWithIntervalSet(f24_pw.getTheFirstFunction().evaluate("4", 9), f24_pw.getTheFirstIntervalSet());
//    
//    out.println(f23_pw);
//    out.println(f24_pw);
//    out.println(newPw23);
//    out.println(newPw24);
//    out.println(newPw23.addPiecewiseFunction(newPw24));
//  }
  
  //@Test
//  void testUnaryInterpolatingFunction() {
//    List<String> unaryLabel = new ArrayList<>();
//    unaryLabel.add("1");
//    
//    Table unaryTable = new Table(unaryLabel);
//
//    unaryTable.addRow(new Row(new double[] {1}, 1.0));
//    unaryTable.addRow(new Row(new double[] {2}, 2.0));
//    unaryTable.addRow(new Row(new double[] {3}, 3.0));
//    
//    List<Double> interpolatedPoint = new ArrayList<>();
//    interpolatedPoint.add(4.0);
//    
//    List<Double> interpolatedPointInTable = new ArrayList<>();
//    interpolatedPointInTable.add(3.0);
//    
//    Row interpolatedRow = unaryTable.inverseWeightedInterpolation(interpolatedPoint);
//    Row interpolatedRowNull =  unaryTable.inverseWeightedInterpolation(interpolatedPointInTable);
//    
//    Row rowToCompare = new Row(new double[] {4}, 26.0/11);
//    
//    assert (interpolatedRow.equals(rowToCompare));
//    assert (interpolatedRowNull == null);
//  }
  
//  @Test
//  void testBinaryInterpolatingFunction() {
//    List<String> unaryLabel = new ArrayList<>();
//    unaryLabel.add("1");
//    unaryLabel.add("2");
//    
//    Table unaryTable = new Table(unaryLabel);
//
//    unaryTable.addRow(new Row(new double[] {1, 2}, 1.0));
//    unaryTable.addRow(new Row(new double[] {2, 3}, 2.0));
//    unaryTable.addRow(new Row(new double[] {3, 4}, 3.0));
//    
//    List<Double> interpolatedPoint = new ArrayList<>();
//    interpolatedPoint.add(4.0);
//    interpolatedPoint.add(5.0);
//    
//    List<Double> interpolatedPointNull = new ArrayList<>();
//    interpolatedPointNull.add(3.0);
//    interpolatedPointNull.add(4.0);
//    
//    Row interpolatedRowNull = unaryTable.inverseWeightedInterpolation(interpolatedPointNull);
//    
//    double utilityToCompare = Math.pow(18, -0.5) + Math.pow(8, -0.5) * 2 + Math.pow(2, -0.5) * 3;
//    utilityToCompare = utilityToCompare / (Math.pow(18, -0.5) + Math.pow(8, -0.5) + Math.pow(2, -0.5));
//       
//    assert (interpolatedRowNull == null);
//    
//    // The assertion is true. This line of code is commented due to precision in comparing Double
////    Row interpolatedRow = unaryTable.inverseWeightedInterpolation(interpolatedPoint);
////    Row rowToCompare = new Row(new double[] {4, 5}, utilityToCompare);
////    assert (interpolatedRow.equals(rowToCompare));
//  }
  
  @Test
  void testAnalyticalProjectDPOP() {
    String owner = "1";
    
    PiecewiseMultivariateQuadFunction f = new PiecewiseMultivariateQuadFunction();
    MultivariateQuadFunction f1 = new MultivariateQuadFunction(owner);

    // -2x_1^2 + 4x_1 + 2x_2^2 + x_2 + 7x_1x_2 - 10
    f1.getCoefficients().put("1", "1", -2.0);
    f1.getCoefficients().put("1", "", 4.0);    
    f1.getCoefficients().put("2", "2", 2.0);
    f1.getCoefficients().put("2", "", 1.0);
    f1.getCoefficients().put("2", "1", 7.0);
    f1.getCoefficients().put("", "", -10.0);    

    Interval int_1 = new Interval(-5, 5);
    Interval int_2 = new Interval(-10, 10);
    
    Map<String, Interval> intervalMap = new HashMap<>();

    intervalMap.put("1", int_1);
    intervalMap.put("2", int_2);
    
    f.addToFunctionMapWithInterval(f1, intervalMap, false);
    f.setOwner(owner);
    
    out.println("BEFORE projecting:\n" + f);
    
    MultivariateQuadFunction critF = f1.getUnaryFunctionAtCriticalPoint(intervalMap);
    MultivariateQuadFunction LB_function = f1.evaluateBinaryFunctionX1(intervalMap.get(f1.getOwner()).getLowerBound(), intervalMap);
    MultivariateQuadFunction UB_function = f1.evaluateBinaryFunctionX1(intervalMap.get(f1.getOwner()).getUpperBound(), intervalMap);
    
    out.println("CritF :\n" + critF);
    out.println("CritF interval: " + critF.getCritFuncIntervalMap());
    out.println("LB_F :\n" + LB_function);
    out.println("LB_F interval: " + LB_function.getCritFuncIntervalMap());
    out.println("UB_F :\n" + UB_function);
    out.println("UB_F interval: " + UB_function.getCritFuncIntervalMap());
    
    PiecewiseMultivariateQuadFunction projected = f1.analyticalProject(intervalMap);
    out.println("AFTER projecting:\n" + projected);
     
  }

//  Internal node combined function: 
//  [Coefficients = {={=-151.1875}, 2={2=4.0, =-35.875, 1=5.0}, 1={1=1.0, =-7.0}}, Intervals = {1=[-10.0, 10.0], 2=[-10.0, 0.0]} ]
//  [Coefficients = {={=-151.1875}, 2={2=4.0, =26.625, 1=5.0}, 1={1=1.0, =-7.0}}, Intervals = {1=[-10.0, 10.0], 2=[0.0, 10.0]} ]
//  @Test 
  void testApproxProjectDPOP() {
    String owner = "1";
    
    PiecewiseMultivariateQuadFunction f = new PiecewiseMultivariateQuadFunction();
    MultivariateQuadFunction f1 = new MultivariateQuadFunction(owner);

    f1.getCoefficients().put("1", "1", -2.0);
    f1.getCoefficients().put("2", "2", 2.0);
    f1.getCoefficients().put("3", "3", 1.0);
    
    f1.getCoefficients().put("2", "1", 7.0);
    f1.getCoefficients().put("2", "3", 8.0);
    f1.getCoefficients().put("3", "1", 9.0);
    
    f1.getCoefficients().put("1", "", 4.0);
    f1.getCoefficients().put("2", "", 1.0);
    f1.getCoefficients().put("3", "", 2.0);

    f1.getCoefficients().put("", "", -10.0); 
    

    Interval int_1 = new Interval(-5, 5);
    Interval int_2 = new Interval(-10, 10);
    Interval int_3 = new Interval(-8, 8);

    Map<String, Interval> intervalMap = new HashMap<>();
    
    intervalMap.put("1", int_1);
    intervalMap.put("2", int_2);
    intervalMap.put("3", int_3);
    
    f.addToFunctionMapWithInterval(f1, intervalMap, false);
    f.setOwner("1");
    
    System.out.println(f.approxProject(2, "1", 2, true));
  }
  
//  @Test
  void TestApproximateProject() {
  }
  
//  @Test
  void interpolate() {
    List<String> label = new ArrayList<>();
    label.add("x1");
    label.add("x4");
    TableDouble table = new TableDouble(label);
    
    List<Double> valueList = new ArrayList<>();
    valueList.add(2.2);
    valueList.add(0.4);
    table.addRow(new RowDouble(new ArrayList<>(valueList), 96.97));
    
    valueList.clear();
    valueList.add(-1.3);
    valueList.add(-1.4);
    table.addRow(new RowDouble(new ArrayList<>(valueList), 57.80));
    
    valueList.clear();
    valueList.add(2.1);
    valueList.add(1.8);
    table.addRow(new RowDouble(new ArrayList<>(valueList), 148.37));
    
    valueList.clear();
    valueList.add(-1.4);
    valueList.add(-0.3);
    table.addRow(new RowDouble(new ArrayList<>(valueList), 24.13));
    
    valueList.clear();
    valueList.add(1.5);
    valueList.add(0.5);
    table.addRow(new RowDouble(new ArrayList<>(valueList), 81.52));

    valueList.clear();
    valueList.add(-2.3);
    valueList.add(2.7);
    table.addRow(new RowDouble(new ArrayList<>(valueList), -93.22));
    
    System.out.println(table);
    
    Map<String, Double> valueMap = new HashMap<>();
    valueMap.put("x1", 2.2);
    
    Interval interval = new Interval(-100, 100);
    
    double[] argmax = table.maxArgmaxHybrid(valueMap, interval.getMidPointInHalfIntegerRanges());
    System.out.println(argmax[1]);
  }
  
//  @Test
  void TestAddingPiecewiseMultivariateQuadFunction() {
  }
  
//  @Test
  // Tested
//  void testCartesianProduct() {
//    PiecewiseMultivariateQuadFunction pwFunc1 = new PiecewiseMultivariateQuadFunction();
//    PiecewiseMultivariateQuadFunction pwFunc2 = new PiecewiseMultivariateQuadFunction();
//
//    MultivariateQuadFunction func11 = new MultivariateQuadFunction();
//    Map<String, Interval> interval1 = new HashMap<>();
//    for (int i = 0; i < 10; i++) {
//      interval1.put("x" + i, new Interval(10 * i, 10 * i + 9));
//    }
//    func11.setIntervals(interval1);
//    func11.setOwner("x1");
//    pwFunc1.addToFunctionMapWithInterval(func11);
//
//    MultivariateQuadFunction func21 = new MultivariateQuadFunction();
//    Map<String, Interval> interval2 = new HashMap<>();
//    for (int i = 3; i < 15; i++) {
//      interval2.put("x" + i, new Interval(10 * i + 3, 10 * i + 7));
//    }
//    func21.setIntervals(interval2);
//    func21.setOwner("x3");
//
//    pwFunc2.addToFunctionMapWithInterval(func21);
//  }
  
}