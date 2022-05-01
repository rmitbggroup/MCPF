package Algorithm.BB;

import Entity.Tuple;
import Function.Sort;
import Setting.Setting;
import generator.InitialData;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Compute bound
 * <p>
 * this is identical with the GreedyHeuristic.java with one different, this only select k location
 * <p>
 * In this version, the A matrix will become to a probability matrix, where each element is the probability of
 * a person appearing at a location.
 */
public class ComputeBoundFast {

    private boolean solved;
    private double infCount;
    private double cost;
    private double preInfCount, preCost;
    private double lowerBound, upperBound;
    private final int MT, N; // location number/user number
    private double[] mVector; // record the current number of meetings from all selected locations to each user
    private double[] mVectorCurrent; // record the current number of meetings from all selected locations
    private final int[] cVector;
    private int[] cVectorAsc;
    private int[] cVectorIndex;
    private final int[] indexVector;
    private final double[] pVector; //profit vector
    private final double[] pcVector; //profit/cost vector
    private final boolean[] finishVector; // whether a user is satisfied
    private final double[][] pMatrix; // each row are the probabilities of a set of locations meeting one user
    private final double[][] pTMatrix; // the transpose of aMatrix, each row are the probabilities of the set of users meeting the location
    private final int[][] idTMatrix; // store the user ids for pTMatrix
    private double[] minProVector; // the minimum probability covering of each user
    private HashSet<Integer> pickedList; // the final result;

    public ComputeBoundFast(InitialData initialData) {
        this.MT = initialData.getMt();
        this.N = initialData.getN();

        this.mVector = new double[N];
        this.mVectorCurrent = new double[N];
        this.minProVector = new double[N];
        this.finishVector = new boolean[N];
        this.indexVector = new int[MT];
        this.pVector = new double[MT];
        this.pcVector = new double[MT];
        this.pMatrix = initialData.getPMatrix();
        this.pTMatrix = new double[MT][];
        this.idTMatrix = new int[MT][];
        this.cVector = Arrays.copyOf(initialData.getCVector(), MT);
        this.cVectorAsc = Arrays.copyOf(initialData.getCVector(), MT);
        this.cVectorIndex = new int[MT];
        initializing(initialData);
    }

    private void initializing(InitialData initialData) {
        pickedList = new HashSet<>(MT);
        Arrays.fill(this.mVector, 0);
        Arrays.fill(this.mVectorCurrent, 0);
        Arrays.fill(this.finishVector, false);

        if (Setting.RANDOM_COVER) {
            for (int i = 0; i < N; i++) {
                this.minProVector[i] = prob2Gain(initialData.getMinReqList()[i]);
            }
        } else {
            for (int i = 0; i < N; i++) {
                this.minProVector[i] = prob2Gain(Setting.REQUEST_THRESHOLD);
            }
        }

        transposeMatrix();
        generateVector();
    }

    private void reset() {
        Arrays.fill(this.finishVector, false);
        pickedList = new HashSet<>(MT);
    }

    public void solve(Tuple tuple, double optGlobal, double optCost) {
        int selectNum;
        boolean isFinished = false;
        int k = tuple.getMaxNum();

        reset();

        double[] pVector = Arrays.copyOf(this.pVector, this.pVector.length);

        HashSet<Integer> canList = tuple.getCanSet();

        //add the previous solution selected from previous batches
        if (tuple.getSolutionSet().size() > 0) {
            this.mVector = Arrays.copyOf(this.mVectorCurrent, this.mVector.length);
            pickedList.addAll(tuple.getSolutionSet());
            preCost = tuple.getPreCost();
            cost = preCost;
            preInfCount = tuple.getPreInfCount();
        } else {
            this.cost = 0.0;
            this.preCost = 0.0;
            this.preInfCount = 0.0;
            Arrays.fill(this.mVector, 0);
        }

//      add the new loc selected in this k-batch
        for (int locID : tuple.getSolutionSetCurrent()) {
            pick(locID);
        }

//      jump if cost is already larger than OPT cost
        if (this.cost >= optCost) {
            upperBound = -1;
            return;
        }
        pickedList.addAll(tuple.getSolutionSetCurrent());
        selectNum = pickedList.size();

        if (selectNum == k) {
            //no need to update upperBound as there is no more loc to be selected
            updateLowerBound();
            upperBound = lowerBound;
            solved = checkPartialConstraintFast();
            return;
        }

        updateUpperBound(k);
        if (upperBound <= optGlobal) {
            upperBound = -1;
            return;
        }

        updateLocConstraint();

        int locID; // the real index in the matrix, location real id -> loc num in pVector, cVector and pcVector
        int indexOpt;
        double opt, currentCostEfficient;
        while (!isFinished && selectNum < k) {
            indexOpt = -1;
            opt = 0;

            for (int locIndex = indexVector.length - 1; locIndex >= 0; locIndex--) {
                locID = indexVector[locIndex];
                if (!canList.contains(locID) || pickedList.contains(locID))
                    continue;

                //jump if it is worse than opt before updating
                currentCostEfficient = (infCount - preInfCount + pVector[locID]) / (cost - preCost + cVector[locID]);
                if (currentCostEfficient <= opt)
                    continue;
                //update, and compare
                pVector[locID] = getLocProfit(locID);
                currentCostEfficient = (infCount - preInfCount + pVector[locID]) / (cost - preCost + cVector[locID]);
                if (currentCostEfficient > opt) {
                    opt = currentCostEfficient;
                    indexOpt = locID;
                }
            }
            selectNum++;
            pickedList.add(indexOpt);
            pick(indexOpt);
            infCount += pVector[indexOpt];
            updateLocConstraint(indexOpt);
            isFinished = checkConstraint();
        }

        //if add new loc, then update lower bound
        lowerBound = (infCount - preInfCount) / (cost - preCost);
        solved = isFinished;
    }

    private double getInfCount() {
        double infCount = 0.0;
        if (cost == 0.0)
            return 0.0;
        for (int userID = 0; userID < this.N; userID++) {
            if (mVector[userID] >= minProVector[userID])
                infCount += minProVector[userID];
            else
                infCount += mVector[userID];
        }
        return infCount;
    }

    public double getPreInfCount(HashSet<Integer> solutionSet) {
        this.cost = 0;
        Arrays.fill(this.finishVector, false);
        Arrays.fill(this.mVector, 0);
        pickedList = new HashSet<>(solutionSet);
        for (int locID : solutionSet) {
            pick(locID);
        }
        this.mVectorCurrent = Arrays.copyOf(this.mVector, this.mVector.length);
        return getInfCount();
    }

    public void updatePCVector(Tuple tuple) {
        HashSet<Integer> solutionSet = tuple.getSolutionSet();
        HashSet<Integer> removeSet = new HashSet<>();
        for (int i = 0; i < MT; i++) {
            this.indexVector[i] = i; //location index
            this.cVectorIndex[i] = i;
            if (!solutionSet.contains(i)) {
                this.pVector[i] = getLocProfit(i);
                this.pcVector[i] = pVector[i] / cVector[i]; //marginal profit
                if (this.pVector[i] < 0.000000001)
                    removeSet.add(i);
            } else {
                this.pVector[i] = 0.0;
                this.pcVector[i] = 0.0;
            }
        }
        tuple.getCanSet().removeAll(removeSet);
        Sort.heapSort(this.indexVector, this.pcVector);
    }

    private void updateLowerBound() {
        infCount = getInfCount();
        lowerBound = (infCount - preInfCount) / (cost - preCost);
    }

    //when |tuple.selected| is less than k, fill |selected| until k without considering marginal-gail diminishing
    private void updateUpperBound(int k) {
        int locID1 = 0, locID2;
        int locIndex;
        int maxSize = k - pickedList.size();
        int maxCostEffIndex = this.MT - 1;
        int maxCostIndex = this.MT - 1;
        double costAddition = 0.0;
        double infCountAddition = 0.0;
        double costEfficient = 0.0; // maximum cost efficient among all available loc
        double alpha = 0;

        infCount = getInfCount();//todo  partially update infCount to speedup

        while (maxSize > 0) {
            //find x with the largest cost-efficient
            for (locIndex = maxCostEffIndex; locIndex >= 0; locIndex--) {
                locID1 = indexVector[locIndex];
                if (pickedList.contains(locID1) || pVector[locID1] == 0) {
                    continue;
                }

                costEfficient = pVector[locID1];
                alpha = 1.0 / cVector[locID1];
                maxCostEffIndex = locIndex;
                break;
            }

            // find maxSize locs with highest cost
            for (locIndex = maxCostIndex; locIndex >= 0; locIndex--) {
                locID2 = cVectorIndex[locIndex];
                if (pickedList.contains(locID2) || pVector[locID2] == 0) {
                    continue;
                }
                alpha *= cVectorAsc[locIndex];
                costAddition += cVectorAsc[locIndex];
                infCountAddition += alpha * costEfficient;
                maxCostIndex--;
                maxSize--;
                break;
            }
        }
        upperBound = (infCount - preInfCount + infCountAddition) / (cost - preCost + costAddition);
    }

    /**
     * check if all constraints are satisfied
     *
     * @return return true if all satisfied
     */
    private boolean checkConstraint() {
        for (boolean isFinished : finishVector) {
            if (!isFinished)
                return false;
        }
        return true;
    }

    /**
     * check partial constraint, return if find any unsatisfied user
     *
     * @return return true if all satisfied
     */
    private boolean checkPartialConstraintFast() {
        for (int user_id = 0; user_id < finishVector.length; user_id++) {
            finishVector[user_id] = checkPersonConstraint(user_id);
            if (!finishVector[user_id])
                return false;
        }
        return true;
    }

    /**
     * check if all users covered by locNum are satisfied
     *
     * @param locNum update users' finish state who are covered by locNum
     */
    private void updateLocConstraint(int locNum) {
        for (int user_id : idTMatrix[locNum]) {
            if (finishVector[user_id])
                continue;
            finishVector[user_id] = checkPersonConstraint(user_id);
        }
    }

    /**
     * check if all users
     */
    private void updateLocConstraint() {
        for (int user_id = 0; user_id < finishVector.length; user_id++) {
            if (finishVector[user_id])
                continue;
            finishVector[user_id] = checkPersonConstraint(user_id);
        }
    }

    private boolean checkPersonConstraint(int user_id) {
        return mVector[user_id] >= minProVector[user_id];
    }

    private void pick(int locNum) {
        double profit;
        int user_id;
        this.cost += cVector[locNum];
        for (int user_index = 0; user_index < pTMatrix[locNum].length; user_index++) {
            profit = pTMatrix[locNum][user_index];
            user_id = idTMatrix[locNum][user_index];
            mVector[user_id] += profit; //update the current meeting times for all users
        }
    }

    /**
     * pTMatrix : transposed of the pMatrix matrix
     * Row :  a location covers a set of users
     * the size of each row of pTMatrix is not equal, each cell only records non-zero probability
     * new version !! : each cell is the marginal gain according to the probability
     */
    private void transposeMatrix() {
        System.out.println("Transpose Matrix");
        for (int i = 0; i < MT; i++) {
            int nonzero_count = 0;
            for (int j = 0; j < N; j++) {
                if (this.pMatrix[j][i] > 0)
                    nonzero_count++;
            }
            if (nonzero_count == 0)
                System.out.println("transposeMatrix error - zero count!");
            this.pTMatrix[i] = new double[nonzero_count];
            this.idTMatrix[i] = new int[nonzero_count];
            int index = 0;
            for (int j = 0; j < N; j++) {
                if (this.pMatrix[j][i] > 0) {
                    this.pTMatrix[i][index] = prob2Gain(this.pMatrix[j][i]);

                    this.idTMatrix[i][index] = j;
                    index++;
                }
            }
        }
    }

    /**
     * calculate the marginal gain according to the probability
     *
     * @param probability [0,1]
     * @return return ln-form
     */
    private double prob2Gain(double probability) {
        double a = (-1.0) * Math.log10(1.0 - probability);
        if (a <= 0)
            System.out.println("error");
        return (-1.0) * Math.log10(1.0 - probability);
    }


    // profit/cost vector
    private void generateVector() {
        for (int i = 0; i < MT; i++) {
            this.indexVector[i] = i; //location index
            this.cVectorIndex[i] = i;
            this.pVector[i] = Arrays.stream(pTMatrix[i]).parallel().sum();
            this.pcVector[i] = pVector[i] / cVector[i]; //marginal profit
        }
        Sort.heapSort2(this.cVectorIndex, this.cVectorAsc);
        Sort.heapSort(this.indexVector, this.pcVector);
    }

    /**
     * The profit from selecting this location
     *
     * @param locNum update locNum's total profit consisted profits from all overed users
     * @return return total profit
     */
    private double getLocProfit(int locNum) {
        double totalProfit = 0;
        int user_id;
        double locProf;
        for (int user_index = 0; user_index < pTMatrix[locNum].length; user_index++) {
            user_id = idTMatrix[locNum][user_index];
            locProf = pTMatrix[locNum][user_index];

            if (mVector[user_id] < minProVector[user_id]) {
                if (mVector[user_id] + locProf <= minProVector[user_id])
                    totalProfit += locProf;
                else
                    totalProfit += minProVector[user_id] - mVector[user_id];
            }
        }
        return totalProfit;

    }

    public double getUpperBound() {
        return upperBound;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public boolean isSolved() {
        return solved;
    }

    public HashSet<Integer> getPickedList() {
        return pickedList;
    }

    /**
     * This is for testing, print info for each step
     *
     * @param pickedList check whether pickedList is a feasible solution
     * @return return yes or no
     */
    public boolean checkConstraintNum(HashSet<Integer> pickedList) {
        Arrays.fill(finishVector, false);
        Arrays.fill(mVector, 0);
        this.cost = 0;
        int count = 0;

        for (int index : pickedList) {
            pick(index);
        }

        updateLocConstraint();
        updateLowerBound();

        for (boolean isFinished : finishVector) {
            if (isFinished)
                count++;
        }
        return count >= minProVector.length;
    }

    public double getCost() {
        return cost;
    }

}