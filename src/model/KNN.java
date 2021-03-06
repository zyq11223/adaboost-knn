package model;

import javafx.util.Pair;

import java.util.*;

public class KNN {
    private static int id;
    private int num;

    private Pair<Boolean,Double> isInFinalModel;

    /**
     * this array will hold the indexes of the k neighbors
     */
//    private Tuple[] k;

    /**
     * this list will hold the distances, the index of the element will be the same index in the trainingSet
     */
    //private Tuple[] distances;

//    private double[][] distancesPair;

    private double alpha;

    private int k_size;

    private int[] classes;

    private double[] weights;

//    private int countCurrect;

    private volatile double errorRate;

    public static int counter = 0;


    public KNN(int k, int numofClasses, double[] weights) {
        this.num = id;
        id++;
        //this.k = new Tuple[k];
        k_size = k;
        /**
         * so we don't have to use 0
         */
        classes = new int[numofClasses + 1];

        this.weights = weights;
        isInFinalModel = new Pair<>(false,0.0);

    }

    /**
     * dereferencing the object
     * @return
     */
    public KNN getClone(){
        KNN knn = new KNN(this.k_size, classes.length-1, weights);
        knn.setNum(this.getNum());
        knn.setIsInFinalModel(new Pair<>(true, 0.0));
        return knn;
    }

    public void inc(){
        this.counter++;
    }

    /**
     * starting the KNN, returns the class of the K nearest neighbors
     * algorithm:
     *  1- calculate new distances from newObservation
     *      1.1- keep K tuples in a maxHeap of size K, that will always hold the K closest, for maximum efficiency
     *  2- get maximum classes between the K neighbors
     * @param set
     * @param newObservation
     * @return
     */
    public int init(Tuple[] set, Tuple newObservation) {
        inc();
        PriorityQueue<TupleDistance> distances = new PriorityQueue<>(k_size, Comparator.reverseOrder());
        int[] classes = new int[this.classes.length];
        Tuple[] k = new Tuple[k_size];

        initDistances(set, newObservation, distances);

        getKnns(distances, k);

        int Y = maxClassInNeighborhood(classes, k);

        calculateError(newObservation, Y);

        return Y;
    }

//    public int initWithLearning(Tuple[] set, Tuple newObservation) {
//        //inc();
//        PriorityQueue<TupleDistance> distances = new PriorityQueue<>(k_size, Comparator.reverseOrder());
//        int[] classes = new int[this.classes.length];
//        Tuple[] k = new Tuple[k_size];
//
//        initDistances(set, newObservation, distances);
//
//        getKnns(distances, k);
//
//        int Y = maxClassInNeighborhood(classes, k);
//
//        calculateError(newObservation, Y);
//
//        return Y;
//    }

    /**
     * getting the closest neighbors from the queue
     *
     * @param distances
     * @param k
     */
    private void getKnns(PriorityQueue<TupleDistance> distances, Tuple[] k) {
        for (int i = 0; i < k.length; i++) {
            Tuple t = distances.poll().tuple;
            k[i] = t;
        }
    }

    /**
     * setting up weighted distances, using priority queue to get the k nearest neighbors.
     *
     * @param set
     * @param newObservation
     * @param distances
     */
    private void initDistances(Tuple[] set, Tuple newObservation, PriorityQueue<TupleDistance> distances) {
        for (int o = 0; o < set.length; o++) {
            Tuple old = set[o];
            if (!old.equals(newObservation)) {
                double sum = 0.0;
                for (int i = 0; i < old.getDataVector().length; i++) {
                    if (old.equals(newObservation)) {
                        continue;
                    } else {


                        sum += (old.getDataVector()[i] - newObservation.getDataVector()[i]) * (old.getDataVector()[i] - newObservation.getDataVector()[i]) * weights[i];


                    }
                }
                /**
                 * setting the distance according to the weight of the sample
                 */

                double curDis = (Math.sqrt(sum));
                insertPriorityK(distances, curDis, old);
            }
        }
    }

    /**
     * setting the tuple status if it was classified correctly by the current classifier
     * and calculating error rate
     *
     * @param newObservation
     * @param correctClass
     */
    private void calculateError(Tuple newObservation, int correctClass) {
        if (newObservation.getClassNum() != correctClass) {
            setErrorRate(getErrorRate() + newObservation.getWeight());
            newObservation.getIsCorrectlyClassified()[num] = false;
        } else {
            newObservation.getIsCorrectlyClassified()[num] = true;
        }
        if(getIsInFinalModel().getKey().booleanValue()){
            setIsInFinalModel(new Pair<>(true, getErrorRate()));
//            System.out.println(this.getNum()+" "+this.getIsInFinalModel().getValue().doubleValue());
        }
    }

    /**
     * counting and returning the highest count of class in k neighbors
     *
     * @param classes
     * @return
     */
    private int maxClassInNeighborhood(int[] classes, Tuple[] kNN) {
        for (int i = 0; i < kNN.length; i++) {
            classes[kNN[i].getClassNum()]++;
        }
        int max = 0;
        int index = 0;
        for (int i = 1; i < classes.length; i++) {
            if (classes[i] > max) {

                max = classes[i];
                index = i;
            }
        }
        return index;
    }

    /**
     * Priority Queue of KNN tuple, we keep a maximum priority queue, to get minimum K distances in O(n) time in one pass.
     * building the heap is O(k)
     *
     * @param distances
     * @param distance
     * @param old
     */
    private void insertPriorityK(PriorityQueue<TupleDistance> distances, double distance, Tuple old) {
        double curDis = distance;
        if (distances.isEmpty()) {
            distances.add(new TupleDistance(old, curDis));
        } else {
            if (distances.size() < k_size) {
                distances.add(new TupleDistance(old, curDis));
            }
            if (distances.size() == k_size) {
                if (distances.peek().distance > curDis) {
                    distances.poll();
                    distances.add(new TupleDistance(old, curDis));
                }
            }

        }
    }

    public static void resetId() {
        id = 0;
    }

    public void prepareForNextStep() {
        errorRate = 0;
    }

    public int getNum() {
        return num;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KNN)) return false;

        KNN knn = (KNN) o;

        return k_size == knn.k_size;
    }


//    public int getCountCurrect() {
//        return countCurrect;
//    }


    public void setNum(int num) {
        this.num = num;
    }

    public synchronized double getErrorRate() {
        return errorRate;
    }

    public synchronized void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setK_size(int k_size) {
        this.k_size = k_size;
    }

    @Override
    public int hashCode() {
        return k_size;
    }


    @Override
    public String toString() {
        return "KNN{" +
                ", alpha=" + alpha +
                ", k_size=" + k_size +
                ", classes=" + Arrays.toString(classes) +
                ", errorRate=" + errorRate +
                '}';
    }

    private class TupleDistance implements Comparable<TupleDistance> {
        public Tuple tuple;
        public double distance;

        public TupleDistance(Tuple tuple, double distance) {
            this.tuple = tuple;
            this.distance = distance;
        }


        @Override
        public int compareTo(TupleDistance o) {
            return Double.compare(this.distance, o.distance);
        }

        @Override
        public String toString() {
            return "TupleDistance{" +
                    ", distance=" + distance +
                    '}';
        }
    }


    public Pair<Boolean, Double> getIsInFinalModel() {
        return isInFinalModel;
    }

    public void setIsInFinalModel(Pair<Boolean, Double> isInFinalModel) {
        this.isInFinalModel = isInFinalModel;
    }

    public int getK_size() {
        return k_size;
    }

    //**********not used**********//

//    public void preprocessing() {
//        distancesPair = new double
//                [SetStarter.getTrainingSet().length + SetStarter.getTestingSet().length]
//                [SetStarter.getTrainingSet().length + SetStarter.getTestingSet().length];
//
//        initilizeDistances(SetStarter.getTrainingSet(), distancesPair);
//        initilizeDistances(SetStarter.getTestingSet(), distancesPair);
//    }
//
//
//    public void initilizeDistances(Tuple[] set, double[][] distancesPair) {
//        for (Tuple old : set) {
//            for (Tuple newObservation : set) {
//                if (!old.equals(newObservation)) {
//                    double sum = 0.0;
//                    for (int i = 0; i < old.getDataVector().length; i++) {
//                        if (old.equals(newObservation)) {
//                            continue;
//                        } else {
//
//
//                            sum += (old.getDataVector()[i] - newObservation.getDataVector()[i]) * (old.getDataVector()[i] - newObservation.getDataVector()[i]) * weights[i];
//
//
//                        }
//                    }
//                    distancesPair[old.getNum()][newObservation.getNum()] = Math.sqrt(sum);
//                }
//            }
//        }
//    }


}
