package model;

import java.util.Arrays;

public class Tuple implements Comparable<Tuple>{

    private double[] dataVector;
    private boolean isCorrectlyClassified;
    private int classNum;
    private double weight;


    /**
     * distance from current new observation.
     */
    private double distance;


    public Tuple(int dim) {
        dataVector = new double[dim];
    }

    public Tuple(double[] dataVector, int classNum) {
        this.dataVector = dataVector;
        this.classNum = classNum;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isCorrectlyClassified() {
        return isCorrectlyClassified;
    }

    public void setCorrectlyClassified(boolean correctlyClassified) {
        isCorrectlyClassified = correctlyClassified;
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
                ", distance=" + distance +
                '}';
    }

    @Override
    public int compareTo(Tuple o) {
        if(this.distance<o.distance)
            return -1;
        else if(o.distance<this.distance)
            return 1;
        return 0;
    }
}