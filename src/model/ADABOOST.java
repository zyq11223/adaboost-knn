package model;


import javafx.util.Pair;
import model.thread_center.ThreadPoolCenter;

import java.util.*;
import java.util.concurrent.*;

public class ADABOOST {

    private ArrayList<KNN> classifiers;
    private PriorityQueue<KNN> priorityKNN;
    private ArrayList<FinalModel> FinalModel;
    private Tuple[] tuples;
    private static volatile int index;
    private volatile double overallErrorRate = 0.0;
    private volatile int countTrue = 0;


    private ArrayList<Pair<Tuple, Tuple>> finalResultsForTesting;
    private ArrayList<Pair<Tuple, Tuple>> finalResultsForTraining;

    public ADABOOST(ArrayList<KNN> classifiers, Tuple[] tuples) {
        this.classifiers = classifiers;
        this.tuples = tuples;
        this.priorityKNN = new PriorityQueue<>(classifiers.size(), Comparator.comparingDouble(KNN::getErrorRate));
        this.FinalModel = new ArrayList<>();
        this.finalResultsForTesting = new ArrayList<>();
        this.finalResultsForTraining = new ArrayList<>();
    }

    public void buildModel2() {

//        System.out.println("printing initial weights:");
        for (int i = 0; i < SetStarter.getTrainingSet().length; i++) {
            SetStarter.getTrainingSet()[i].setWeight(1.0 / (double) SetStarter.getTrainingSet().length);
        }
        try {
            for (int i = 0; i < classifiers.size(); i++) {


//                System.out.println("step " + i);
                //System.out.println(Arrays.stream(tuples).mapToDouble(Tuple::getWeight).sum());
                KNN lowestErrorClassifier = runClassifiers(priorityKNN, classifiers);

                double E = lowestErrorClassifier.getErrorRate();
                double alpha = (1 - E) / (E);
                // double alpha = 0.5 * Math.log((1 - E) / (E));
//                System.out.println(lowestErrorClassifier.getNum());

                initNewWeights(lowestErrorClassifier);

                lowestErrorClassifier.setAlpha(alpha);

                FinalModel.add(new FinalModel(lowestErrorClassifier, lowestErrorClassifier.getAlpha()));

                setOverallErrorRate(0.0);

                finalResultsForTraining.clear();
                for (int j = 0; j < tuples.length; j++) {
                    setOverallErrorRate(getOverallErrorRate() + checkModelValidity(tuples[j], finalResultsForTraining));
                }

                if ((1 - ((overallErrorRate / (double) tuples.length))) == 0.0 || priorityKNN.stream().allMatch(e -> e.getErrorRate() > 0.5)) {
                    break;
                }
            }

//            System.out.println(1 - ((overallErrorRate / (double) tuples.length)));
//            System.out.println(getFinalModel());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * setting up weights for next step.
     *
     * @param lowestErrorClassifier
     */
    private void initNewWeights(KNN lowestErrorClassifier) {
        Arrays.stream(SetStarter.getTrainingSet()).forEach(t ->
                {
                    if (t.getIsCorrectlyClassified()[lowestErrorClassifier.getNum()]) {
                        t.setWeight(0.5 * (t.getWeight() / (1 - lowestErrorClassifier.getErrorRate())));
                    } else {
                        t.setWeight(0.5 * (t.getWeight() / (lowestErrorClassifier.getErrorRate())));
                    }
                }
        );
    }

    /**
     * run classifiers on training set, and return the classifier with the lowest error rate;
     * multithreaded; will run the classifiers on the trainng set in parallel.
     *
     * @param priorityKNN
     * @param classifiers
     * @return
     */
    private KNN runClassifiers(PriorityQueue<KNN> priorityKNN, List<KNN> classifiers) {
        priorityKNN.clear();
        for (KNN k : classifiers) {
            k.prepareForNextStep();
        }

        index = 0;
        for (int j = 0; j < classifiers.size(); j++) {
            CompletableFuture<?>[] futures = initWorkers(classifiers.get(index), tuples).stream()
                    .map(task -> CompletableFuture.runAsync(task, ThreadPoolCenter.getExecutor()))
                    .toArray(CompletableFuture[]::new);


            CompletableFuture.allOf(futures).join();
//                    for (int k = 0; k < tuples.length; k++) {
//                        classifiers.get(index).init(tuples, tuples[k]);
//
//                    }
            priorityKNN.add(classifiers.get(index));
            index++;
        }


        return priorityKNN.peek();
    }


    public synchronized int checkModelValidity(Tuple t, List<Pair<Tuple, Tuple>> finalResults) {


        double[] finalSums = new double[4];
        Arrays.setAll(finalSums, e -> 1.0);
        for (FinalModel finalModel : FinalModel) {
            finalSums[finalModel.knn.init(tuples, t)] *= finalModel.alpha;
        }


        double max = 0;
        int index = 0;
        for (int i = 1; i < finalSums.length; i++) {
            if (finalSums[i] > max) {
                max = finalSums[i];
                index = i;
            }
        }

        finalResults.add(new Pair<>(t, t.createResultClone(t.getDataVector(), index)));

        if (t.getClassNum() != index) {
            // System.out.println("got it wrong");
            return 0;
        } else {
            // System.out.println("got it correct");
            return 1;
        }

    }


    public double runOnTestingSet() throws InterruptedException {

        ArrayList<Runnable> runnables = new ArrayList<>();
        setCountTrue(0);
//        runnables.add(() -> {
//            for (int i = 0; i < 100; i++) {
//                setCountTrue(getCountTrue() + checkModelValidity(SetStarter.getTestingSet()[i]));
//            }
//        });
        runnables.add(() -> {
//            for (int i = 0; i < SetStarter.getTestingSet().length; i++) {
//                setCountTrue(getCountTrue() + checkModelValidity(SetStarter.getTestingSet()[i]));
//            }
        });
        finalResultsForTesting.clear();
        for (int i = 0; i < SetStarter.getTestingSet().length; i++) {
            setCountTrue(getCountTrue() + checkModelValidity(SetStarter.getTestingSet()[i], finalResultsForTesting));
        }

//        CompletableFuture<?>[] futures = runnables.stream()
//                .map(task -> CompletableFuture.runAsync(task, ThreadPoolCenter.getExecutor()))
//                .toArray(CompletableFuture[]::new);
//
//
//        CompletableFuture.allOf(futures).join();

        return getCountTrue() / (double) SetStarter.getTestingSet().length;

    }

    public synchronized int getCountTrue() {
        return countTrue;
    }

    public synchronized void setCountTrue(int countTrue) {
        this.countTrue = countTrue;
    }

    public static synchronized int getIndex() {
        return index;
    }

    public ArrayList<KNN> getClassifiers() {
        return classifiers;
    }

    public PriorityQueue<KNN> getPriorityKNN() {
        return priorityKNN;
    }

    public ArrayList<FinalModel> getFinalModel() {
        return FinalModel;
    }

    public Tuple[] getTuples() {
        return tuples;
    }

    public synchronized double getOverallErrorRate() {
        return overallErrorRate;
    }

    public synchronized void setOverallErrorRate(double overallErrorRate) {
        this.overallErrorRate = overallErrorRate;
    }


    private ArrayList<Runnable> initWorkers(KNN knn, Tuple[] set) {
        ArrayList<Runnable> runnables = new ArrayList<>();
        int start = 0;
        int finish = 1;
        while (finish <= tuples.length) {
            runnables.add(RunnableFactory.create(start, finish, knn, set));

            if (finish == tuples.length)
                break;

            start = finish;
            finish += 1;
            if (finish > tuples.length) {
                finish = tuples.length;
            }

        }

        return runnables;
    }


    private class FinalModel {
        private KNN knn;
        private double alpha;

        public FinalModel(KNN knn, double alpha) {
            this.knn = knn;
            this.alpha = alpha;
        }

        @Override
        public String toString() {
            return "FinalModel{" +
                    "knn=" + knn.getNum() +
                    "k=" + this.knn.getK_size() +
                    ", alpha=" + alpha +
                    '}';
        }
    }

    public StringBuilder getFinalReport() {
        StringBuilder report = new StringBuilder();

        report.append("Final Report:");
        report.append(System.getProperty("line.separator"));
        report.append("Training data size: " + SetStarter.getTrainingSet().length)
                .append(System.getProperty("line.separator"));
        report.append("Final Model: ").append(System.getProperty("line.separator")).append(getFinalModel().toString());
        report.append(System.getProperty("line.separator"));
        report.append("Number of correctly classified training data class 1:").append(System.getProperty("line.separator"));
        report.append(finalResultsForTraining.stream().filter(e -> e.getValue().getClassNum() == 1 && e.getKey().getClassNum() == e.getValue().getClassNum()).count());
        report.append(System.getProperty("line.separator"));
        report.append("Number of incorrectly classified training data class 1:").append(System.getProperty("line.separator"));
        report.append(finalResultsForTraining.stream().filter(e -> e.getValue().getClassNum() == 1 && e.getKey().getClassNum() != e.getValue().getClassNum()).count());


        report.append(System.getProperty("line.separator"));
        report.append("Number of correctly classified training data class 2:").append(System.getProperty("line.separator"));
        report.append(finalResultsForTraining.stream().filter(e -> e.getValue().getClassNum() == 2 && e.getKey().getClassNum() == e.getValue().getClassNum()).count());
        report.append(System.getProperty("line.separator"));
        report.append("Number of incorrectly classified training data class 2:").append(System.getProperty("line.separator"));
        report.append(finalResultsForTraining.stream().filter(e -> e.getValue().getClassNum() == 2 && e.getKey().getClassNum() != e.getValue().getClassNum()).count());

        report.append(System.getProperty("line.separator"));
        report.append("Training Error Rate: " + (1 - getOverallErrorRate() / (double) SetStarter.getTrainingSet().length));

        report.append(System.getProperty("line.separator"));
        report.append("Testing: ");
        report.append(System.getProperty("line.separator"));
        report.append("testing data size: " + SetStarter.getTestingSet().length)
                .append(System.getProperty("line.separator"));
        report.append("Number of correctly classified testing data class 1:").append(System.getProperty("line.separator"));
        report.append(finalResultsForTesting.stream().filter(e -> e.getValue().getClassNum() == 1 && e.getKey().getClassNum() == e.getValue().getClassNum()).count());
        report.append(System.getProperty("line.separator"));
        report.append("Number of incorrectly classified testing data class 1:").append(System.getProperty("line.separator"));
        report.append(finalResultsForTesting.stream().filter(e -> e.getValue().getClassNum() == 1 && e.getKey().getClassNum() != e.getValue().getClassNum()).count());


        report.append(System.getProperty("line.separator"));
        report.append("Number of correctly classified testing data class 2:").append(System.getProperty("line.separator"));
        report.append(finalResultsForTesting.stream().filter(e -> e.getValue().getClassNum() == 2 && e.getKey().getClassNum() == e.getValue().getClassNum()).count());
        report.append(System.getProperty("line.separator"));
        report.append("Number of incorrectly classified testing data class 2:").append(System.getProperty("line.separator"));
        report.append(finalResultsForTesting.stream().filter(e -> e.getValue().getClassNum() == 2 && e.getKey().getClassNum() != e.getValue().getClassNum()).count());

        report.append(System.getProperty("line.separator"));
        report.append("Testing Error Rate: " + (1 - countTrue / (double) SetStarter.getTestingSet().length));


        return report;
    }

    public ArrayList<Pair<Tuple, Tuple>> getFinalResultsForTesting() {
        return finalResultsForTesting;
    }

    public ArrayList<Pair<Tuple, Tuple>> getFinalResultsForTraining() {
        return finalResultsForTraining;
    }

    private static class RunnableFactory {

        private RunnableFactory() {
        }


        public static Runnable create(int start, int finish, KNN knn, Tuple[] set) {

            return () -> {
                for (int k = start; k < finish; k++) {
                    knn.init(set, set[k]);

                }
            };

        }


    }


}
