package model;

import java.util.ArrayList;
import java.util.Arrays;

public class Tuple{

    private static int id = 0;
    private int num;
    private double[] dataVector;
    private boolean[] isCorrectlyClassified;
    private int classNum;
    private double weight;


    /**
     * distance from current new observation.
     */
//    private double distance;
//
//    private ArrayList<Double> distances;



    public Tuple(int dim) {
        dataVector = new double[dim];
    }

    public Tuple(double[] dataVector, int classNum) {
        this.num = id;
        id++;
        this.dataVector = dataVector;
        this.classNum = classNum;
        this.isCorrectlyClassified = new boolean[SetStarter.getWeakClassifiers().length];
    }

    public double[] getDataVector() {
        return dataVector;
    }

    public void addPoint(double p) {
        this.dataVector[this.dataVector.length - 1] = p;
    }

    public int getClassNum() {
        return classNum;
    }

    public void setClassNum(int classNum) {
        this.classNum = classNum;
    }


    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean[] getIsCorrectlyClassified() {
        return isCorrectlyClassified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;

        Tuple tuple = (Tuple) o;

        return Arrays.equals(dataVector, tuple.dataVector);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(dataVector);
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "dataVector=" + Arrays.toString(dataVector) +
                ", isCorrectlyClassified=" + isCorrectlyClassified +
                ", classNum=" + classNum +
                ", weight=" + weight +
                '}';
    }

    public int getNum() {
        return num;
    }

    public static void resetId(){
        id=0;
    }

}
