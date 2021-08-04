package com.colorpaletteandroid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.Runtime;

import android.util.Log;



public class GenColorPalette{
    //no of clusters(ie. no of colors in the palette)   
    private static int CLUSTERS = 10;
    //pixel array
    private int[] pixels;
    //final palette that is returned
    private int[] paletteColors = new int[CLUSTERS];
    
    //current minimum cost
    private double minCost = Double.MAX_VALUE;

    //for managing threads
    private final Object lock = new Object();
    private int iter = 0;
    private ExecutorService eService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    //interface to send data from the worker threads to main thread
    private PaletteCallback paletteCallback;

    GenColorPalette(int[] pixels, PaletteCallback paletteCallback){
        this.pixels = pixels;
        Arrays.sort(this.pixels);
        this.paletteCallback = paletteCallback;
    }
    
    /**
   * Generates colour palette from the pixel array
   */
    public void genPalette(){
        if(this.pixels == null || this.pixels.length == 0) {
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
                        //each cluster has at least 1 point(the medoid itself)
                        Arrays.fill(clusterSizes, 1);           
                        
                        double cost = 0;
                        for(int j = 0; j < 100; j++){
                            double[] changed = assignCluster(pixels, clusterIndex, meds, diffToMed, clusterSizes);      
                            if(changed[0] == 1){
                                cost = changed[1];
                                break;
                            }
                            calcMedoids(pixels, clusterIndex, meds, clusterSizes);
                            Arrays.fill(clusterSizes, 1);
                        }
                    
                        synchronized(lock){
                            iter += 1;
                            if(cost < minCost){
                                minCost = cost;
                                for(int i = 0; i < meds.length; i++){
                                    paletteColors[i] = pixels[meds[i]];
                                }
                                paletteCallback.onPaletteGen(paletteColors, false);
                            }
                            //final callback is the one with the minimum cost clusters
                            if(iter == 10) paletteCallback.onPaletteGen(paletteColors, true);
                        }
                    }
                    catch(Exception e){
                        Log.d("ColorPaletteModule", "Exception: " + e.getStackTrace()[0].getLineNumber() + " :: " + e.toString());
                        paletteCallback.onError(e);
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
   * puts every pixel in the cluster whose medoid is closest to that pixel, 
   * also calculates and returns the total cost(ie. sum of differences between pixels and their medoids) 
   * of the entire pixel array
   * @param pixelArr        pixel array
   * @param clusterArr      (same size as pixelArr) i'th element has the index(of an element in medoidArr) of the cluster 
   *                        that the i'th pixel in pixelArr belongs to
   * @param medoidArr       (size CLUSTERS) i'th element has the index(of an element in pixelArr) thats the medoid of the i'th cluster
   * @param diffArr         (same size as pixelArr) i'th element is the absolute difference between the i'th element in pixelArr 
   *                        and the medoid of the cluster it belongs to
   * @param clusterSizes    (size CLUSTERS) i'th element is the size of the i'th cluster
   * @return double[]       (size 2) returns whether any pixels have changed clusters, 
   *                        and total cost 
   */
    private double[] assignCluster(int[] pixelArr, int[] clusterArr, int[] medoidArr, int[] diffArr, int[] clusterSizes){
        boolean change = false;
        double totalCost = 0;;
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
            totalCost += diffArr[i];
        }
        double[] res = new double[]{change ? 1 : 0,totalCost}; 
        return res;
    }

    /**
     * calculates new medoids for each cluster
     * @param pixelArr      pixel array
     * @param clusterArr    (same size as pixelArr) i'th element has the index(of an element in medoidArr) of the cluster 
     *                      that the i'th pixel in pixelArr belongs to
     * @param medoidArr     (size CLUSTERS) i'th element has the index(of an element in pixelArr) thats the medoid of the i'th cluster
     * @param clusterSizes  (size CLUSTERS) i'th element is the size of the i'th cluster
     */
    private void calcMedoids(int[] pixelArr, int[]clusterArr, int[] medoidArr, int[] clusterSizes){
        int[] clustCounter = new int[CLUSTERS];
        boolean[] medsCalc = new boolean[CLUSTERS];
        int clustDone = 0;
        for(int i = 0; i < pixelArr.length; i++){
            
            int cId = clusterArr[i];
            if(medsCalc[cId]) continue;

            clustCounter[cId] += 1;
            int mid = Math.round(clusterSizes[cId]/2);
            
            if(clustCounter[cId] >= mid){
                medoidArr[cId] = i;
                medsCalc[cId] = true;
                clustDone += 1;
            }

            if(clustDone >= medoidArr.length) break;
        }
    }
    

}