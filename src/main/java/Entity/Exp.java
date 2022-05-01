package Entity;

public class Exp {
    public double thresholdRatio;
    public String algorithm;
    public double k; //k batch;
    public double partialGreedy; // partial solution from greedy;
    public int locNumber;
    public int userNumber;
    public double cost;
    public double time;
    public int expTimes;

    public Exp(){this.expTimes = 1;}

    public void addExp(Exp exp) {
        if (this.thresholdRatio != exp.thresholdRatio ||
                !this.algorithm.contains(exp.algorithm) ||
                this.k != exp.k ||
                this.partialGreedy != exp.partialGreedy ||
                this.locNumber != exp.locNumber
        ) {
            System.out.println("Error, Different exp!");
            return;
        }
        this.cost += exp.cost;
        this.time += exp.time;
        this.expTimes++;
    }

}
