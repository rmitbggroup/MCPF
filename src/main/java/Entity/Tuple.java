package Entity;


import java.util.HashSet;

public class Tuple implements Comparable {

    private HashSet<Integer> solutionSet;
    private HashSet<Integer> solutionSetCurrent; // selected location in this batch
    private HashSet<Integer> canSet; // candidate set
    private double preInfCount; // previous infCount from solutionSet
    private double upperInf;
    private double lowerInf;
    private double preCost;
    private int maxNum;
    private int size;

    public HashSet<Integer> getSolutionSet() {
        return solutionSet;
    }

    public double getUpperInf() {
        return upperInf;
    }

    public void setUpperInf(double upperInf) {
        this.upperInf = upperInf;
    }

    public void setLowerInf(double lowerInf) {
        this.lowerInf = lowerInf;
    }

    public Tuple(HashSet<Integer> solutionSet, HashSet<Integer> solutionSetCurrent, HashSet<Integer> canSet, Tuple tupleParent) {
        this.solutionSet = solutionSet;
        this.solutionSetCurrent = solutionSetCurrent;
        this.canSet = canSet;
        this.maxNum = tupleParent.maxNum;
        this.lowerInf = tupleParent.lowerInf;
        this.upperInf = tupleParent.upperInf;
        this.size = this.solutionSet.size() + this.solutionSetCurrent.size();
    }

    public Tuple(HashSet<Integer> solutionSet, HashSet<Integer> canSet, double upperInf, int maxNum) {
        this.solutionSet = solutionSet;
        this.solutionSetCurrent = new HashSet<>();
        this.canSet = canSet;
        this.upperInf = upperInf;
        this.maxNum = maxNum;
        this.size = this.solutionSet.size();
    }


    public HashSet<Integer> getSolutionSetCurrent() {
        return solutionSetCurrent;
    }

    public HashSet<Integer> getCanSet() {
        return canSet;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public double getPreInfCount() {
        return preInfCount;
    }

    public void setPreInfCount(double preInfCount) {
        this.preInfCount = preInfCount;
    }

    public double getPreCost() {
        return preCost;
    }

    public void setPreCost(double preCost) {
        this.preCost = preCost;
    }

    public int getSize() {
        return size;
    }

    public boolean splitable() {
        return size < maxNum;
    }


    @Override
    public int compareTo(Object o) {
        Tuple tuple = (Tuple) o;
        if (tuple.lowerInf > this.lowerInf)
            return 1;
        else if (tuple.lowerInf < this.lowerInf)
            return -1;
        else {
//            if (tuple.solutionSet.size() + tuple.getSolutionSetCurrent().size() > this.solutionSet.size() + this.solutionSetCurrent.size())
            if (tuple.preCost >= this.preCost)
                return 1;
            else
                return -1;
        }
    }
}
