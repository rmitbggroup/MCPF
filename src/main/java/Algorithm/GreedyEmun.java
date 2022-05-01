package Algorithm;

import Algorithm.BB.BranchAndBound;
import Algorithm.BB.ComputeBoundFast;
import Algorithm.Greedy.GreedyPrecise;
import Entity.Exp;
import Setting.Setting;
import generator.InitialData;

import java.util.HashSet;


/**
 * Algorithm 2: Fusion Solution -  k-selection greedy
 * <p>
 * This greedy is for the new problem where the goal is to achieve the fair covering.
 * A cover is a fair covering if and only if the covering ratio of every user reach a pre-defined threshold
 * <p>
 * In this version, the A matrix will become to a probability matrix, where each element is the probability of
 * a person appearing at a location.
 * <p>
 * In each iteration, the branch-and-bound solution will find the optimal k-size solution
 */
public class GreedyEmun {

    private final int MT, N; // location number/user number/period number
    //    private List<Integer> pickedList; // the final result;
    private BranchAndBound branchAndBound;
    private ComputeBoundFast computeBoundFast;
    private GreedyPrecise greedyPrecise;
    private HashSet<Integer> canSet = new HashSet<>();
    private String fileName;


    public GreedyEmun(InitialData initialData, int theta) {
        System.out.println("*****************k-Batch Class Start*****************");
        System.out.println("Batch size " + theta);
        if (Setting.partialGreedy)
            System.out.println("Based on Partial Greedy Solution - Ratio " + Setting.partialSolveRatio);
        Setting.theta = theta;
        this.MT = initialData.getMt();
        this.N = initialData.getN();
        for (int i = 0; i < MT; i++) {
            canSet.add(i);
        }
        if (!initialData.getFineName().equals("Null"))
            fileName = initialData.getFineName();
        branchAndBound = new BranchAndBound(initialData);
        computeBoundFast = new ComputeBoundFast(initialData);
        if (Setting.partialGreedy) {
            greedyPrecise = new GreedyPrecise(initialData);
        }
    }

    public Exp solve() {
        int maxNum;
        long startTime = System.currentTimeMillis();
        boolean isSolved = false;
        double cost = Double.MAX_VALUE;
        int finalTerm = 0;
        HashSet<Integer> solutionSetPossible = new HashSet<>();
        HashSet<Integer> solutionSet = new HashSet<>();
        HashSet<Integer> canSet = new HashSet<>(this.canSet);

        if (Setting.partialGreedy) {
            greedyPrecise.getPartialResult();
            solutionSet.addAll(greedyPrecise.getPickedList());
            canSet.removeAll(solutionSet);
        } else {
            Setting.partialSolveRatio = 0.0;
        }

        do {
            maxNum = solutionSet.size() + Setting.theta + finalTerm;
            branchAndBound.solve(canSet, solutionSet, maxNum);

            if (branchAndBound.getFinalResultSet().size() > 0) {
                if (finalTerm == 0) {
                    System.out.println("Find OPT, increase maxMum");
                    System.out.println("Can " + canSet.size() + "   Solu " + solutionSet.size() + "   Cost " + branchAndBound.getCost());
                    finalTerm = 2;
                }
                if (branchAndBound.getCost() < cost) {
                    solutionSetPossible = branchAndBound.getFinalResultSet();
                    cost = branchAndBound.getCost();
                    System.out.println("Update Final OPT : cost " + cost);
                }
                continue;
            }

            solutionSet = branchAndBound.getResult();

            canSet = new HashSet<>(this.canSet);
            canSet.removeAll(solutionSet);
            isSolved = branchAndBound.isFinished(solutionSet);
        } while (!isSolved);

        if (branchAndBound.getCost() < cost) {
            cost = branchAndBound.getCost();
        } else {
            solutionSet = solutionSetPossible;
        }
        System.out.println("Request meet ratio: " + Setting.REQUEST_THRESHOLD);
        System.out.println("Total picked " + solutionSet.size());
        System.out.println("Total cost " + cost);
        long endTime = System.currentTimeMillis();
        long time = (endTime - startTime);
        System.out.println("Time cost " + time + " milliseconds");

        System.out.println("-------------------------------------------------------------");
        System.out.println("*****************GreedyEmun Class Finished*****************");
        return recordData(solutionSet.size(), cost, time, isSolved);
    }

    private Exp recordData(int size, double cost, long time, boolean isSolved) {
        Exp exp = new Exp();
        exp.thresholdRatio = Setting.REQUEST_THRESHOLD;
        exp.algorithm = "Multi-Selection";
        exp.cost = cost;
        exp.time = time;
        exp.locNumber = this.MT;
        exp.userNumber = this.N;
        exp.k = Setting.theta;
        exp.partialGreedy = Setting.partialSolveRatio;
        return exp;
    }
}
