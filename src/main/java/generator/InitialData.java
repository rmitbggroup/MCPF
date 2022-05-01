package generator;

import Setting.Setting;
import fileIO.MyFileReader;
import fileIO.MyFileWriter;

import java.util.Random;

public class InitialData {

    private int n, mt; // location number/user number/period number/ m*t
    private int[] cVector;  // this is the cost vector for MIP
    private double[][] aMatrix;   // this is the matrix for the constraints
    private double[][] pMatrix;   // this is the matrix for the constraints - probability
    private double[] minReqList;
    private String fineName;

    public InitialData() {
        checkFileExist(0);
    }

    public boolean readData(String fileName) {
        MyFileReader myFileReader = new MyFileReader();
        if (!myFileReader.initializeReader(fileName)) {
            this.fineName = "Null";
            return false;
        }
        this.fineName = fileName;
        String nextLine = myFileReader.getNextLine();
        String[] contentList;

        //read parameters
        if (nextLine != null) {
            contentList = nextLine.split(",");
            n = Integer.parseInt(contentList[1]);
        } else {
            return false;
        }

        myFileReader.getNextLine(); // jump a line

        nextLine = myFileReader.getNextLine();
        if (nextLine != null) {
            contentList = nextLine.split(",");
            n = Integer.parseInt(contentList[0]);
            mt = Integer.parseInt(contentList[1]);
        } else {
            return false;
        }

        cVector = new int[mt]; //vector[]  vertical vector
        if (Setting.PROB_MOD)
            pMatrix = new double[n][mt];
        else
            aMatrix = new double[n][mt]; // matrix[i][j]  i-th is row, j-th column

        //read cost vector
        nextLine = myFileReader.getNextLine();
        contentList = nextLine.split(",");

        for (int i = 0; i < mt; i++) {
            cVector[i] = Integer.parseInt(contentList[i]) + 1;
        }

        //read meeting matrix
        for (int i = 0; i < n; i++) {
            nextLine = myFileReader.getNextLine();
            contentList = nextLine.split(",");
            for (int j = 0; j < mt; j++) {
                if (contentList[j] != null)
                    if (Setting.PROB_MOD)
                        pMatrix[i][j] = Double.parseDouble(contentList[j]);
                    else
                        aMatrix[i][j] = Integer.parseInt(contentList[j]);
                else {
                    System.out.println("InitialData - Wrong dataset!");
                    return false;
                }

            }
        }

        generateReqList();

        System.out.println("InitialData - Finished");
        return true;
    }

    private void generateReqList() {
        if (!Setting.RANDOM_COVER)
            return;

        minReqList = new double[n];
        Random random = new Random();
        double req;

        for (int i = 0; i < minReqList.length; i++) {
            req = 0.3;
            minReqList[i] = req;
        }
    }

    public double[] getMinReqList() {
        return minReqList;
    }

    public int getN() {
        return n;
    }

    public int[] getCVector() {
        return cVector;
    }

    public double[][] getPMatrix() {
        return pMatrix;
    }

    public int getMt() {
        return mt;
    }


    private boolean checkFileExist(int index) {
        String fileName;
        MyFileWriter myFileWriter = new MyFileWriter();

        if (Setting.PROB_MOD)
            Setting.fineName = "TestDataP";
        else
            Setting.fineName = "TestDataA";
        fileName = Setting.fineName + " " + index + ".txt";

        if (myFileWriter.checkFileExist(fileName, false)) {
            return true;
        }
        return false;
    }

    public String getFineName() {
        return fineName;
    }
}
