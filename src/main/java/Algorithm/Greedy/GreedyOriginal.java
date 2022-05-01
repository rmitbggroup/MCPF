package Algorithm.Greedy;

import Algorithm.BB.ComputeBoundFast;
import Entity.Exp;
import Function.Sort;
import Setting.Setting;
import generator.InitialData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Algorithm 0: The original Greedy, use the probability to compute marginal gain directly
 */
public class GreedyOriginal {

    private final int MT, N; // location number/user number/period number
    private final int[] cVector; // cost vector
    private int[] indexVector;
    private double[] mVector; // meeting probability vector, each cell is Sum x ln (1-A) for a person
    private double[] pVector; //profit vector
    private double[] pcVector; //profit/cost vector
    private boolean[] finishVector; // whether a user is satisfied
    private double[][] pMatrix; // each row are the probabilities of a set of locations meeting one user
    private final double[][] pTMatrix; // the transpose of aMatrix, each row are the probabilities of the set of users meeting the location
    private int[][] idTMatrix; // store the user ids for pTMatrix
    private double[] minProVector; // the minimum probability covering of each user
    private List<Integer> pickedList; // the final result;
    private String fileName;
    private boolean isFinished;

    private ComputeBoundFast computeBoundFast;

    public GreedyOriginal(InitialData initialData) {
        System.out.println("*****************Greedy Original Probability Class Start*****************");
        this.MT = initialData.getMt();
        this.N = initialData.getN();

        this.mVector = new double[N];
        this.minProVector = new double[N];
        this.finishVector = new boolean[N];
        this.indexVector = new int[MT];
        this.pVector = new double[MT];
        this.pcVector = new double[MT];
        this.pMatrix = initialData.getPMatrix();
        this.pTMatrix = new double[MT][];
        this.idTMatrix = new int[MT][];
        this.cVector = initialData.getCVector();

        if (!initialData.getFineName().equals("Null"))
            fileName = initialData.getFineName();

        computeBoundFast = new ComputeBoundFast(initialData);
        initializing(initialData);
    }

    private void initializing(InitialData initialData) {
        pickedList = new ArrayList<>();

        if (Setting.RANDOM_COVER) {
            for (int i = 0; i < N; i++) {
                this.minProVector[i] = 1 - initialData.getMinReqList()[i];
                this.mVector[i] = 1;
                this.finishVector[i] = false;
            }
        } else {
            for (int i = 0; i < N; i++) {
                this.mVector[i] = 1;
                this.finishVector[i] = false;
                this.minProVector[i] = 1 - Setting.REQUEST_THRESHOLD;
            }
        }

        transposeMatrix();
        generatePCVector();
        Sort.heapSort(this.indexVector, this.pcVector);
    }

    public Exp solve() {
        long startTime = System.currentTimeMillis();

        System.out.println("Start Solving!");
        int locNum; // the real index in the matrix, location real id -> loc num in pVector, cVector and pcVector
        int pickedIndex = indexVector.length - 1; // the pick index in indexVector, location real id
        boolean isFinished = false;

        while (pickedIndex > 0 && !isFinished) {
            locNum = indexVector[pickedIndex];
            this.pVector[locNum] = getLocProfit(locNum);
            this.pcVector[pickedIndex] = pVector[locNum] / cVector[locNum];

            if (pcVector[pickedIndex] >= pcVector[pickedIndex - 1]) {
                pickedList.add(locNum);
//                System.out.println("Selected Loc" + pickedList.size());
                pick(locNum);
                pickedIndex--;
                isFinished = checkConstraint();

                HashSet<Integer> list = new HashSet<>(pickedList);
                if (pickedList.size() % 3 == 0 && pickedList.size() > 0) {
                    computeBoundFast.checkConstraintNum(list);
                }


            } else {
                orderSub(pickedIndex);
            }


        }
        if (!isFinished) { // select the last one
            locNum = indexVector[0];
            pickedList.add(locNum);
            pick(locNum);
        }

        this.isFinished = checkConstraint();
        if (!isFinished)
            System.out.println("Problem Unsolved!");
        else
            System.out.println("Problem Solved!");

        System.out.println("Request meet ratio: " + Setting.REQUEST_THRESHOLD);
        System.out.println("Total picked " + pickedList.size());
        System.out.println("Total cost " + totalCost());
        long endTime = System.currentTimeMillis();
        long time = (endTime - startTime);
        System.out.println("Time cost " + time + " milliseconds");
        System.out.println("-------------------------------------------------------------");
        System.out.println("*****************Greedy Precise Class Finished*****************");
        return recordData(pickedList.size(), totalCost(), time);
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

    private void updateLocConstraint(int locNum) {
        for (int user_id : idTMatrix[locNum]) {
            if (finishVector[user_id])
                continue;
            finishVector[user_id] = checkPersonConstraint(user_id);
        }
    }

    private boolean checkPersonConstraint(int usrIndex) {
        return (mVector[usrIndex] - minProVector[usrIndex] < -0.0001);
    }

    private Exp recordData(int size, double cost, long time) {
        Exp exp = new Exp();
        exp.thresholdRatio = Setting.REQUEST_THRESHOLD;
        exp.algorithm = "Greedy probability";
        exp.cost = cost;
        exp.time = time;
        exp.locNumber = this.MT;
        exp.userNumber = this.N;
        return exp;
    }

    private void orderSub(int pickedIndex) {
        while (pickedIndex > 0 && pcVector[pickedIndex] < pcVector[pickedIndex - 1]) {
            Sort.swap(indexVector, pcVector, pickedIndex, pickedIndex - 1);
            pickedIndex--;
        }
    }

    private void pick(int locNum) {
        double profit;
        int user_id;
        for (int user_index = 0; user_index < pTMatrix[locNum].length; user_index++) {
            profit = pTMatrix[locNum][user_index];
            user_id = idTMatrix[locNum][user_index];
            mVector[user_id] = mVector[user_id] * (1.0 - profit); //update the current meeting times for all users
        }
        updateLocConstraint(locNum);
    }

    /**
     * pTMatrix : transposed of the pMatrix matrix
     * Row :  a location covers a set of users
     * the size of each row of pTMatrix is not equal, each cell only records non-zero probability
     * new version !! : each cell is the marginal gain according to the probability
     */
    private void transposeMatrix() {
        System.out.println("Greedy Original - Transpose Matrix");
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
                    this.pTMatrix[i][index] = this.pMatrix[j][i];
                    this.idTMatrix[i][index] = j;
                    index++;
                }
            }
        }
    }

    // profit/cost vector
    private void generatePCVector() {
        for (int i = 0; i < MT; i++) {
            this.indexVector[i] = i; //location index
            this.pVector[i] = getLocProfit(i);
            if (cVector[i] != 0)
                this.pcVector[i] = pVector[i] / cVector[i]; //marginal profit
            else
                this.pcVector[i] = 0;
        }
    }

    /**
     * The profit from selecting this location
     *
     * @param locNum
     * @return
     */
    private double getLocProfit(int locNum) {
        double totalProfit = 0.0;
        int user_id;
        double value;
        // check user (user_index) in pTMatrix for a location (locNum)
        for (int user_index = 0; user_index < pTMatrix[locNum].length; user_index++) {
            user_id = idTMatrix[locNum][user_index];

            if (!finishVector[user_id]) {
                // the covering probability of this user after picked locNum
                value = mVector[user_id] * (1.0 - pTMatrix[locNum][user_index]);
                if (value <= minProVector[user_id])
                    totalProfit += mVector[user_id] - minProVector[user_id];
                else
                    totalProfit += mVector[user_id] - value;
            }
        }
        return totalProfit;
    }

    public double totalCost() {
        double cost = 0;
        for (int i : pickedList) {
            cost += cVector[i];
        }
        return cost;
    }

    public boolean getIsFinished() {
        return isFinished;
    }

    public List<Integer> getPickedList() {
        return pickedList;
    }
}
