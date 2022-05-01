import Algorithm.Greedy.GreedyHeuristic;
import Algorithm.Greedy.GreedyOriginal;
import Algorithm.Greedy.GreedyPrecise;
import Algorithm.GreedyEmun;
import Function.RecordExperiment;
import Setting.Setting;
import generator.InitialData;

public class Main {

    public static void main(String[] args) {

        int fileIndex;
        if (args.length > 0) {
            System.out.println("File index " + args[0]);
        }

        systemInfTest();


        InitialData initialData = new InitialData();
        RecordExperiment recordExperiment = new RecordExperiment();

        Setting.partialGreedy =  false;

        double startParRatio = 0.7;
        double endParRatio = 0.81;
        for (double parRatio = startParRatio; parRatio <= endParRatio; parRatio += 0.1) {
            parRatio = (double) Math.round(parRatio * 10) / 10;
            Setting.partialSolveRatio = parRatio;

            for (double ratio = 0.2; ratio <= .81; ratio += 0.1) {
                ratio = (double) Math.round(ratio * 100) / 100;
                Setting.REQUEST_THRESHOLD = ratio;
                fileIndex = 0;

                if (Setting.RANDOM_COVER)
                    Setting.REQUEST_THRESHOLD = 0;


                while (initialData.readData(Setting.fineName + " " + fileIndex + ".txt")) {

                    GreedyOriginal testFinishable = new GreedyOriginal(initialData);
                    testFinishable.solve();

                    if (!testFinishable.getIsFinished()) {
                        System.out.println("Problem is unable to solve!");
                        break;
                    } else
                        System.out.println("Problem is solvable!");


                    GreedyEmun greedyEmun3 = new GreedyEmun(initialData, 3);
                    recordExperiment.newExp(greedyEmun3.solve());

                    GreedyEmun greedyEmun2 = new GreedyEmun(initialData, 2);
                    recordExperiment.newExp(greedyEmun2.solve());

                    GreedyEmun greedyEmun1 = new GreedyEmun(initialData, 1);
                    recordExperiment.newExp(greedyEmun1.solve());

                    if (parRatio == startParRatio || !Setting.partialGreedy) {
//                        only run normal greedy at the first times
                        GreedyOriginal greedyOriginal = new GreedyOriginal(initialData);
                        recordExperiment.newExp(greedyOriginal.solve());

                        GreedyPrecise greedyPrecise = new GreedyPrecise(initialData);
                        recordExperiment.newExp(greedyPrecise.solve());

                        GreedyHeuristic greedyHeuristic = new GreedyHeuristic(initialData);
                        recordExperiment.newExp(greedyHeuristic.solve());
                    }


                    fileIndex++;

                }

                if (Setting.RANDOM_COVER)
                    break;

            }
            if (args.length == 0)
                recordExperiment.recordExp("0");
            else
                recordExperiment.recordExp(args[0]);

            if (!Setting.partialGreedy)
                break;
        }
    }


    public static void systemInfTest() {
        String OS = System.getProperty("os.name").toLowerCase();
        System.out.print("System Type - ");
        if (isWindows(OS)) {
            System.out.println("Windows CORE");
            Setting.system = 0;
        } else if (isMac(OS)) {
            System.out.println("MAC OS CORE");
            Setting.system = 1;
        } else if (isUnix(OS)) {
            System.out.println("LINUX CORE");
            Setting.system = 2;
        }

    }

    public static boolean isWindows(String name) {
        return name.contains("win");
    }

    public static boolean isMac(String name) {
        return name.contains("mac");
    }

    public static boolean isUnix(String name) {
        return (name.contains("nix") || name.contains("nux") || name.contains("aix"));
    }


}
