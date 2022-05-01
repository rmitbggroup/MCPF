package Setting;

public class Setting {

    public static final boolean RANDOM_COVER = false; // random covering likelihood

    public static final boolean PROB_MOD = true; // switch matrix mode, meeting time /  probability
    public static String fineName;

    public static double PRICE_POWER = 0.8; // price = sum A ^ PRICE_POWER

    public static int system = -1; // system : 0 window, 1 mac, 2 linux

    public static double REQUEST_THRESHOLD = 0.5; // at east half of travels should be covered.

    public static String pathDirection;


    //parameters for k-batch greedy
    public static boolean partialGreedy = false; // use GreedyPrecise to generate a partial solution
    public static double partialSolveRatio = .8; // partial solution from GreedyPrecise
    public static int theta = 0; // the maximum size of sub-opt solution
}
