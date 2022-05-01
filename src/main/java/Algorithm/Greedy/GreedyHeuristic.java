package Algorithm.Greedy;

import Entity.Exp;
import Function.Sort;
import Setting.Setting;
import generator.InitialData;

import java.util.ArrayList;
import java.util.List;

/**
 * Algorithm 1: Greedy Heuristic
 * <p>
 * This greedy is for the new problem where the goal is to achieve the fair covering.
 * A cover is a fair covering if and only if the covering ratio of every user reach a pre-defined threshold
 * <p>
 * In this version, the A matrix will become to a probability matrix, where each element is the probability of
 * a person appearing at a location.
 * <p>
 * Important: Generating P for each location
 * select P that cover the most number of user
 * This is the Algorithm 1 in paper
 */
public class GreedyHeuristic {

    private final int MT, N; // location number/user number/period number
    private int[] mVector; // record the current number of meetings from all selected locations to each user
    private final int[] cVector;
    private int[] indexVector;
    private double[] pVector; //profit vector
    private double[] pcVector; //profit/cost vector
    private boolean[] finishVector; // whether a user is satisfied
    private double[][] pMatrix; // each row are the probabilities of a set of locations meeting one user
    private double[][] pTMatrix; // the transpose of aMatrix, each row are the probabilities of the set of users meeting the location
    private final int[] minSerVector; // the minimum number of service of each user
    private double[] minProVector; // the minimum probability covering of each user
    private List<Integer> pickedList; // the final result;
    private String fileName;


    public GreedyHeuristic(InitialData initialData) {
        System.out.println("*****************Greedy Heuristic |P| Class Start*****************");
        this.MT = initialData.getMt();
        this.N = initialData.getN();

        this.mVector = new int[N];
        this.minProVector = new double[N];
        this.minSerVector = new int[N];
        this.finishVector = new boolean[N];
        this.indexVector = new int[MT];
        this.pVector = new double[MT];
        this.pcVector = new double[MT];
        this.pMatrix = initialData.getPMatrix();
        this.pTMatrix = new double[MT][N];
        this.cVector = initialData.getCVector();
        this.pickedList = new ArrayList<>();

        if (!initialData.getFineName().equals("Null"))
            fileName = initialData.getFineName();

        initializing(initialData);
    }

    private void initializing(InitialData initialData) {
        if (Setting.RANDOM_COVER) {
            for (int i = 0; i < N; i++) {
                this.mVector[i] = 0;
                this.finishVector[i] = false;
                this.minSerVector[i] = this.MT;
                this.minProVector[i] = initialData.getMinReqList()[i];
            }
        } else {
            for (int i = 0; i < N; i++) {
                this.mVector[i] = 0;
                this.finishVector[i] = false;
                this.minSerVector[i] = this.MT;
                this.minProVector[i] = Setting.REQUEST_THRESHOLD;
            }
        }

        //this.aMatrix = pd.getaMatrix();
        transposeMatrix();
        generatePCVector();
        Sort.heapSort(this.indexVector, this.pcVector);
    }

    public Exp solve() {
        long startTime = System.currentTimeMillis();

        System.out.println("Start Solving!");
        int locNum; // the real index in the matrix
        int pickedIndex = indexVector.length - 1; // the pick index in indexVector
        boolean isFinished = false;

        locNum = indexVector[pickedIndex];
        pickedList.add(locNum);
        pick(locNum);
        pickedIndex--;

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
            } else {
                orderSub(pickedIndex);
            }
        }
        if (!isFinished) { // select the last one
            locNum = indexVector[0];
            pickedList.add(locNum);
            pick(locNum);
        }

        if (!checkConstraint())
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


        System.out.println("*****************Greedy  Heuristic |P| Class Finished*****************");
        return recordData(pickedList.size(), totalCost(), time);
    }

    /**
     * check constraint for selected location
     */
    private void updateConstraint(List<Integer> pickedList, int loc_num) {
        for (int i = 0; i < this.N; i++) {
            if (finishVector[i])
                continue;
            if (pTMatrix[loc_num][i] == 0)
                continue;
            finishVector[i] = checkPersonConstraint(pickedList, i);
        }
    }

    private boolean checkConstraint() {
        for (int i = 0; i < this.N; i++) {
            if (!finishVector[i])
                return false;
        }
        return true;
    }


    private boolean checkPersonConstraint(List<Integer> pickedList, int usrIndex) {
        //pMtrix;  each row is a set of locations meeting one user
        double p = 1.0;
        double[] mVector = pMatrix[usrIndex];

        for (int i : pickedList) {
            p *= 1.0 - mVector[i];
        }
        p = 1.0 - p;
        if (p >= minProVector[usrIndex])
            return true;
        return false;

    }

    private Exp recordData(int size, double cost, long time) {
        Exp exp = new Exp();
        exp.thresholdRatio = Setting.REQUEST_THRESHOLD;
        exp.algorithm = "Greedy |P|";
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
        double[] row = pTMatrix[locNum];
        for (int i = 0; i < N; i++) {
            if (row[i] == 0)
                continue;
            mVector[i] += 1.0; //update the current meeting times for all users
        }
        updateConstraint(pickedList, locNum);
    }

    public void transposeMatrix() {
        System.out.println("BasicGreedy - Transpose Matrix");
        for (int i = 0; i < MT; i++) {
            for (int j = 0; j < N; j++) {
                this.pTMatrix[i][j] = this.pMatrix[j][i];
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
     * @param locIndex
     * @return
     */
    private double getLocProfit(int locIndex) {
        double[] row = pTMatrix[locIndex];
        double totalProfit = 0;
        for (int i = 0; i < N; i++) {
            if (row[i] > 0.0) {
                totalProfit += getMargProfit(mVector[i], (int) (mVector[i] + 1), i);
            }
        }
        return totalProfit;
    }

    /**
     * The number of meetings gains from this location
     *
     * @param currentT current meeting time
     * @param newT     new meeting time
     * @param userID   user ID
     * @return return the difference min((new - current), (minimum times - current))
     */
    private double getMargProfit(int currentT, int newT, int userID) {
        if (finishVector[userID])
            return 0;
        if (newT > minSerVector[userID])
            return minSerVector[userID] - currentT;
        else
            return newT - currentT;
    }

    public double totalCost() {
        double cost = 0;
        for (int i : pickedList) {
            cost += cVector[i];
        }
        return cost;
    }
}
