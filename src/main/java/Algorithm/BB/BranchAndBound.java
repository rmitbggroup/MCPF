package Algorithm.BB;

import Entity.Tuple;
import generator.InitialData;

import java.util.*;

public class BranchAndBound {

    private int updateTime = 0;
    private double opt; // the highest inf/cost
    private HashSet<Integer> selectedSet;
    private Hashtable<Integer, Hashtable> abandonSet;
    private double costOPT, cost;
    private HashSet<Integer> finalResultSet;
    private PriorityQueue<Tuple> queue;                 // Max Heap H
    private ComputeBoundFast computeBound;

    public BranchAndBound(InitialData initialData) {
        computeBound = new ComputeBoundFast(initialData);
        this.costOPT = Double.MAX_VALUE;
    }

    private void initialize(HashSet<Integer> candidateSet, HashSet<Integer> selectedLocSet, int maxNum) {
        this.selectedSet = selectedLocSet;
        this.finalResultSet = new HashSet<>();
        this.opt = Double.MIN_VALUE; // upper bound, current minimum cost
        Tuple tuple = new Tuple(selectedLocSet, candidateSet, opt, maxNum);

        if (tuple.getSolutionSet().size() > 0) {
            tuple.setPreInfCount(computeBound.getPreInfCount(tuple.getSolutionSet()));
            tuple.setPreCost(computeBound.getCost());
            computeBound.updatePCVector(tuple);
        }

        this.queue = new PriorityQueue<>();
        this.queue.add(tuple);

    }

    public void solve(HashSet<Integer> candidateSet, HashSet<Integer> selectedLocSet, int maxNum) {
        this.updateTime = 0;
        Tuple tuple, tupleA;
        HashSet<Integer> selectedSetCurrent;
        abandonSet = new Hashtable<>();

        initialize(candidateSet, selectedLocSet, maxNum);
        long totalStartTime = System.currentTimeMillis(), totalEndTime;
        long startTime = System.currentTimeMillis(), endTime, time;
        int iter = 0;
        while (queue.size() > 0) {
            iter++;
            tuple = queue.poll();

            endTime = System.currentTimeMillis();
            time = (endTime - startTime);
            if (time / 1000 > 180) { //present details if the current iteration cost more than X seconds
                System.out.println("Iter (" + iter / 10000 + "0K ) Queue : " + queue.size());
                System.out.println("|S|Sc|C|All" + tuple.getSolutionSet().size() + " | "
                        + tuple.getSolutionSetCurrent().size() + " | " + tuple.getCanSet().size() + " | "
                        + (tuple.getSolutionSet().size() + tuple.getSolutionSetCurrent().size() + tuple.getCanSet().size()));
System.out.println("opt " + opt);
                System.out.println("Time " + time / 1000 + " s");
                startTime = System.currentTimeMillis();
            }

            if (tuple.splitable()) {

                candidateSet = tuple.getCanSet();

                for (int locID : candidateSet) {
                    //branch : with location-locIndex
                    HashSet<Integer> canList = new HashSet<>(candidateSet);
                    canList.remove(locID);
                    selectedSetCurrent = new HashSet<>(tuple.getSolutionSetCurrent());
                    selectedSetCurrent.add(locID);
                    tupleA = new Tuple(tuple.getSolutionSet(), selectedSetCurrent, canList, tuple);
                    tupleA.setPreCost(tuple.getPreCost());
                    tupleA.setPreInfCount(tuple.getPreInfCount());

                    computeBound.solve(tupleA, opt, costOPT);
                    checkResult(tupleA);
                }
            }
        }

        totalEndTime = System.currentTimeMillis();
        time = (totalEndTime - totalStartTime);
        if (time / 1000 > 180) {
            System.out.println("  -----  Iteration End  -----  ");
            System.out.println("Selected Num " + selectedSet.size());
            System.out.println("Time " + time / 1000.0 + " s");
            System.out.println("Possible Opt Cost " + this.costOPT);
        }
    }

    private void checkResult(Tuple tuple) {
        if (computeBound.getUpperBound() < opt) {
            return;
        }

        if (tuple.getSize() + 1 == tuple.getMaxNum()) {
            if (computeBound.getLowerBound() < opt) {
                return;
            }
        }

        // best possible result is better than opt
        tuple.setUpperInf(computeBound.getUpperBound());
        tuple.setLowerInf(computeBound.getLowerBound());
        queue.add(tuple);
        // current result is better than opt
        if (computeBound.getLowerBound() > opt) {
            updateTime++;
            opt = computeBound.getLowerBound();
            selectedSet = computeBound.getPickedList();
            cost = computeBound.getCost();
        } else if (computeBound.getLowerBound() == opt) {
            if (computeBound.getCost() < cost) {
                updateTime++;
                opt = computeBound.getLowerBound();
                selectedSet = computeBound.getPickedList();
                cost = computeBound.getCost();
            }
        }

        if (computeBound.isSolved())
            if (computeBound.getCost() < costOPT) {
                this.costOPT = computeBound.getCost();
                this.finalResultSet = computeBound.getPickedList();
                System.out.println("Update OPT : cost " + this.costOPT);
            }
    }

    public double getCost() {
        return this.cost;
    }


    public boolean isFinished(HashSet<Integer> solutionSet) {
        if (updateTime == 0)
            return true;
        return computeBound.checkConstraintNum(solutionSet);
    }

    public HashSet<Integer> getFinalResultSet() {
        return finalResultSet;
    }

    public HashSet<Integer> getResult() {
        return selectedSet;
    }

}
