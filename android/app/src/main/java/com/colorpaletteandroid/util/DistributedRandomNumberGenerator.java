package com.colorpaletteandroid;
import java.util.*;
import android.util.Log;

/**
 * Class to generate random numbers according to a weighted probability distribution
 * @see based on https://stackoverflow.com/a/20329901
 */
public class DistributedRandomNumberGenerator {

    private double[] dist;
    private double distSum = 0;

    public DistributedRandomNumberGenerator(int length) {
        dist = new double[length];
    }

    public void addNumber(int value, double distribution) {
        if(dist[value] != 0.0d){
            distSum -= dist[value];
        }
        dist[value] = distribution;
        distSum += distribution;
    }

    public int getDistributedRandomNumber() {
        double rand = Math.random();
        double ratio = 1.0f / distSum;
        double tempDist = 0;
        for(int j = 0; j < dist.length; j++){
            tempDist += dist[j];
            if(rand/ratio <= tempDist){
                return j;
            }
        }
        return 0;
    }

}