package Function;

public class Sort {

    public static void heapSort2(int[] indexVector, int[] pcVector) {
        for (int i = pcVector.length / 2; i >= 0; i--) {
            heapAdjust(indexVector, pcVector, i, pcVector.length);
        }
        for (int i = pcVector.length - 1; i >= 0; i--) {
            swap(indexVector, pcVector, 0, i);
            heapAdjust(indexVector, pcVector, 0, i);
        }
    }

    public static void heapSort(int[] indexVector, double[] pcVector) {
        for (int i = pcVector.length / 2; i >= 0; i--) {
            heapAdjust(indexVector, pcVector, i, pcVector.length);
        }
        for (int i = pcVector.length - 1; i >= 0; i--) {
            swap(indexVector, pcVector, 0, i);
            heapAdjust(indexVector, pcVector, 0, i);
        }
    }

    private static void heapAdjust(int[] indexVector, double[] pcVector, int s, int n) {
        int i;
        int tmp1 = indexVector[s];
        double tep2 = pcVector[s];

        for (i = 2 * s; i < n; i *= 2) {
            if (i < n - 1 && pcVector[i] < pcVector[i + 1])
                i++;
            if (tep2 >= pcVector[i])
                break;

            pcVector[s] = pcVector[i];
            indexVector[s] = indexVector[i];
            s = i;
        }
        pcVector[s] = tep2;
        indexVector[s] = tmp1;
    }

    private static void heapAdjust(int[] indexVector, int[] pcVector, int s, int n) {
        int i;
        int tmp1 = indexVector[s];
        int tep2 = pcVector[s];

        for (i = 2 * s; i < n; i *= 2) {
            if (i < n - 1 && pcVector[i] < pcVector[i + 1])
                i++;
            if (tep2 >= pcVector[i])
                break;

            pcVector[s] = pcVector[i];
            indexVector[s] = indexVector[i];
            s = i;
        }
        pcVector[s] = tep2;
        indexVector[s] = tmp1;
    }

    public static void swap(int[] indexVector, double[] pcVector, int index1, int index2) {
        int tmp1 = indexVector[index1];
        double tem2 = pcVector[index1];
        indexVector[index1] = indexVector[index2];
        pcVector[index1] = pcVector[index2];
        indexVector[index2] = tmp1;
        pcVector[index2] = tem2;
    }

    public static void swap(int[] indexVector, int[] pcVector, int index1, int index2) {
        int tmp1 = indexVector[index1];
        int tem2 = pcVector[index1];
        indexVector[index1] = indexVector[index2];
        pcVector[index1] = pcVector[index2];
        indexVector[index2] = tmp1;
        pcVector[index2] = tem2;
    }
}

