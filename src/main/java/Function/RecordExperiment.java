package Function;

import Entity.Exp;
import Setting.Setting;
import fileIO.MyFileWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

public class RecordExperiment {

    // greedy algorithm : alg / locNumber / thresholdRatio
    private Hashtable<String, Hashtable> expList1 = new Hashtable<>();

    // k-batch algorithm :  alg / locNumber / partialGreedy (0 if not) / thresholdRatio
    private Hashtable<String, Hashtable> expList2 = new Hashtable<>();

    public void newExp(Exp exp) {
        if (exp.algorithm.contains("Multi-Selection"))
            newExp2(exp);
        else
            newExp1(exp);
    }

    /**
     * Greedy exp experiment
     *
     * @param exp
     */
    private void newExp1(Exp exp) {
        // exp / alg / locNumber
        String algName = exp.algorithm;
        Hashtable<Integer, Hashtable> algList;  // same algorithm
        if (expList1.containsKey(algName))
            algList = expList1.get(algName);
        else {
            algList = new Hashtable<>();
            expList1.put(algName, algList);
        }

        // alg / locNumber / thresholdRatio
        int locNumber = exp.locNumber;
        Hashtable<Double, Exp> locNumberList;  // same location
        if (algList.containsKey(locNumber))
            locNumberList = algList.get(locNumber);
        else {
            locNumberList = new Hashtable<>();
            algList.put(locNumber, locNumberList);
        }

        //thresholdRatio
        double thresholdRatio = exp.thresholdRatio;
        Exp expNew;  // same threshold ratio
        if (locNumberList.containsKey(thresholdRatio)) {
            expNew = locNumberList.get(thresholdRatio);
            expNew.addExp(exp);
            locNumberList.put(thresholdRatio, expNew);
        } else {
            locNumberList.put(thresholdRatio, exp);
        }
    }

    /**
     * k-batch exp result
     *
     * @param exp
     */
    private void newExp2(Exp exp) {
        // exp / alg / locNumber
        String algName = exp.algorithm;
        if (exp.k > 0)
            algName = algName + "-" + exp.k;
        Hashtable<Integer, Hashtable> algList;  // same algorithm
        if (expList2.containsKey(algName))
            algList = expList2.get(algName);
        else {
            algList = new Hashtable<>();
            expList2.put(algName, algList);
        }

        // alg / locNumber / thresholdRatio
        int locNumber = exp.locNumber;
        Hashtable<Double, Hashtable> locNumberList; // same location
        if (algList.containsKey(locNumber))
            locNumberList = algList.get(locNumber);
        else {
            locNumberList = new Hashtable<>();
            algList.put(locNumber, locNumberList);
        }


        //Partial Greedy Ratio
        double partialGreedy = exp.partialGreedy;
        Hashtable<Double, Exp> partialGreedyList;  //same threshold ratio
        if (locNumberList.containsKey(partialGreedy))
            partialGreedyList = locNumberList.get(partialGreedy);
        else {
            partialGreedyList = new Hashtable<>();
            locNumberList.put(partialGreedy, partialGreedyList);
        }

        //thresholdRatio
        double thresholdRatio = exp.thresholdRatio;
        Exp expNew;  // same partialGreedy ratio
        if (partialGreedyList.containsKey(thresholdRatio)) {
            expNew = partialGreedyList.get(thresholdRatio);
            expNew.addExp(exp);
            partialGreedyList.put(thresholdRatio, expNew);
        } else {
            partialGreedyList.put(thresholdRatio, exp);
        }
    }

    public void recordExp(String fineIndex) {
        StringBuilder content = new StringBuilder();
        MyFileWriter myFileWriter = new MyFileWriter();
        String name = "Experiment Summary " + fineIndex;
        myFileWriter.recordData(name, true);

        if (expList1.size() > 0) {

            content.append("_____________________________________________________\r\n");
            content.append("_______    Normal Greedy Result Summary    __________\r\n");
            content.append("_____________________________________________________\r\n");

            ArrayList<String> greedyNameList = new ArrayList<>(expList1.keySet());

            for (String alg : greedyNameList) {
                content.append("_______    Algorithm    ").append(alg).append("    __________\r\n");
                Hashtable<Integer, Hashtable> algList = expList1.get(alg);
                ArrayList<Integer> locNumList = new ArrayList<>(algList.keySet());

                for (int locNum : locNumList) {
                    Exp exp = new Exp();
                    Hashtable<Double, Exp> thresholdList = algList.get(locNum);
                    ArrayList<Double> thresholds = new ArrayList<>(thresholdList.keySet());

                    Collections.sort(thresholds);
                    content.append("Ratio").append("\t");
                    for (Double threshold : thresholds) {
                        content.append(threshold).append("\t");
                    }
                    content.append("\r\n");

                    content.append("Cost").append("\t");
                    for (Double threshold : thresholds) {
                        exp = thresholdList.get(threshold);
                        exp.cost = (int) (exp.cost / exp.expTimes);
                        content.append(exp.cost).append("\t");
                    }
                    content.append("\r\n");

                    content.append("Time").append("\t");
                    for (Double threshold : thresholds) {
                        exp = thresholdList.get(threshold);
                        exp.time = (int) (exp.time / exp.expTimes);
                        content.append(exp.time).append("\t");
                    }
                    content.append("\r\n");

                    content.append("Loc Num : ").append(locNum).append("   || User Num : ").append(exp.userNumber).append("\r\n");
                    content.append("Cost Power : ").append(Setting.PRICE_POWER).append("\r\n");
                }
            }
        }


        if (expList2.size() > 0) {

            content.append("______________________________________________________________\r\n");
            content.append("_______  Multi-Selection  K-Batch Result Summary    __________\r\n");
            content.append("______________________________________________________________\r\n");

            ArrayList<String> greedyNameList = new ArrayList<>(expList2.keySet());

            for (String alg : greedyNameList) {
                content.append("_______    Algorithm    ").append(alg).append("    __________\r\n");
                Hashtable<Integer, Hashtable> algList = expList2.get(alg);
                ArrayList<Integer> locNumList = new ArrayList<>(algList.keySet());

                for (int locNum : locNumList) {
                    Hashtable<Double, Hashtable> partialRatioList = algList.get(locNum);
                    ArrayList<Double> partialRatios = new ArrayList<>(partialRatioList.keySet());

                    for (Double partialRatio : partialRatios) {
                        if (partialRatio == 0)
                            content.append("_______   No Partial Greedy Result    __________\r\n");
                        else
                            content.append("_______   Partial Greedy Ratio " + partialRatio + "   __________\r\n");
                        Hashtable<Double, Exp> thresholdList = partialRatioList.get(partialRatio);
                        ArrayList<Double> thresholds = new ArrayList<>(thresholdList.keySet());

                        Collections.sort(thresholds);
                        content.append("Ratio").append("\t");
                        for (Double threshold : thresholds) {
                            content.append(threshold).append("\t");
                        }
                        content.append("\r\n");

                        Exp exp = new Exp();
                        content.append("Cost").append("\t");
                        for (Double threshold : thresholds) {
                            exp = thresholdList.get(threshold);
                            exp.cost = (int) (exp.cost / exp.expTimes);
                            content.append(exp.cost).append("\t");
                        }
                        content.append("\r\n");

                        content.append("Time").append("\t");
                        for (Double threshold : thresholds) {
                            exp = thresholdList.get(threshold);
                            exp.time = (int)(exp.time / exp.expTimes) ;
                            content.append(exp.time).append("\t");
                        }
                        content.append("\r\n");

                        if (exp.k > 0)
                            content.append("Batch size : ").append(exp.k).append("\r\n");
                        content.append("Loc Num : ").append(locNum).append("   || User Num : ").append(exp.userNumber).append("\r\n");
                        content.append("Cost Power : ").append(Setting.PRICE_POWER).append("\r\n");
                    }
                }
            }
        }
        myFileWriter.writeToFile(content.toString());

        expList1 = new Hashtable<>();
        expList2 = new Hashtable<>();
    }
}
