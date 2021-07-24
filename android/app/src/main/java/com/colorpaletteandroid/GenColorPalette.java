package com.colorpaletteandroid;

import com.facebook.react.bridge.Promise;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;



public class GenColorPalette{
    //no of clusters(ie. no of colors in the palette)   
    private static int CLUSTERS = 5;
    //pixel array
    private int[] pixels;
    //final palette that is returned
    private int[] paletteColors = new int[CLUSTERS];
    
    //for managing threads
    private final Object lock = new Object();
    private int iterationsDone = 0;
    private ExecutorService eService = Executors.newFixedThreadPool(4);
    

    GenColorPalette(int[] pixels){
        this.pixels = pixels;
        Arrays.sort(this.pixels);
    }
    
    /**
   * Generates colour palette from the pixel array
   * @param p The promise that is fired when colour palette has been generated 
   */
    public void genPalette(Promise p){
        if(this.pixels == null || this.pixels.length == 0) {
            p.reject("Error: Pixels not set");
            return;
        }
        

        for(int i = 0; i < 10; i++){
            eService.execute(new Runnable(){
                int[] meds = new int[CLUSTERS];
                int[] clusterSizes = new int[CLUSTERS];
                int[] clusterIndex = new int[pixels.length];
                int[] diffToMed = new int[pixels.length];
                @Override
                public void run() {
                    try{
                        Arrays.fill(clusterIndex, -1);
                        Arrays.fill(diffToMed,Integer.MAX_VALUE);
                        
                        setInitMedoids(meds, pixels);
                        
                        assignCluster(pixels, clusterIndex, meds, diffToMed,clusterSizes);      
                        
                        synchronized(lock){
                            iterationsDone += 1;
                        }
                    }
                    catch(Exception e){
                        Log.d("ColorPaletteModule", "Exception: " + e.getStackTrace()[0].getLineNumber() + " :: " + e.toString());
                    }
                }
            });    
        }
    }

    /**
   * chooses initial medoids using the k-means++ algorithm
   * @param medArr      array of medoids that will be filled with initial medoids 
   * @param pixelArr    pixel array
   */
    private void setInitMedoids(int[] medArr, int[] pixelArr){
        /*
        Random r = new Random();
        for(int i = 0; i < medArr.length; i++){
            medArr[i] = r.nextInt(length);
        }
        */
        medArr[0] = new Random().nextInt(pixelArr.length);
        int curr = 1;

        while(curr < medArr.length){
            double[] dists = new double[pixelArr.length];
    
            double totalDist = 0;
            for(int i = 0; i < pixelArr.length; i++){
                boolean centered = false;
                double minDist = Double.MAX_VALUE;
                for(int j = 0; j < curr;j++){
                    if(medArr[j] == i) {
                        centered = true;
                        break;
                    }
                    
                    double sqDist = Math.pow(pixelArr[medArr[j]] - pixelArr[i],2);
                    if(minDist > sqDist) minDist = sqDist;
                }
                if(centered) continue;

                totalDist += minDist;
                dists[i] = minDist;
    
                

            }
    
            DistributedRandomNumberGenerator gen = new DistributedRandomNumberGenerator(dists.length);
            for(int k = 0; k < dists.length; k++){
                if(dists[k] == 0) continue;
                gen.addNumber(k, dists[k]/totalDist);
            }

            medArr[curr] = gen.getDistributedRandomNumber();
            curr++;
        }


    }


    /**
   * puts every pixel in the cluster whose medoid is closest to that pixel 
   * @param pixelArr        pixel array
   * @param clusterArr      (same size as pixelArr) i'th element has the index(of an element in medoidArr) of the cluster 
   *                        that the i'th pixel in pixelArr belongs to
   * @param medoidArr       (size CLUSTERS) i'th element has the index(of an element in pixelArr) thats the medoid of the i'th cluster
   * @param diffArr         (same size as pixelArr) i'th element is the absolute difference between the i'th element in pixelArr 
   *                        and the medoid of the cluster it belongs to
   * @param clusterSizes    i'th element is the size of the i'th cluster
   * @return boolean        returns true if 1 or more pixel has been moved to a different cluster
   */
    private boolean assignCluster(int[] pixelArr, int[] clusterArr, int[] medoidArr, int[] diffArr, int[] clusterSizes){
        boolean change = false;
        for(int i = 0; i < pixelArr.length; i++){
            for(int j = 0; j < medoidArr.length; j++){
                int medVal = pixelArr[medoidArr[j]];
                if(Math.abs(medVal - pixelArr[i]) < diffArr[i]){
                    diffArr[i] = Math.abs(medVal - pixelArr[i]);
                    int oldCluster = clusterArr[i];
                    clusterArr[i] = j;
                    if(oldCluster != -1) clusterSizes[oldCluster] -= 1;
                    clusterSizes[j] += 1;
                    change = true;
                }
            }
        }
        return change;
    }


    private void calcMedoids(int[] pixelArr, int[] medoidArr, int[] clusterSizes){

    }

    /**
   * simple log method 
   * @param s String to log
   */
    private void log(String s){
        Log.d("ColorPaletteModule", s);
    }
    
    

}