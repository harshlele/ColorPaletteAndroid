package com.colorpaletteandroid;

import com.facebook.react.bridge.Promise;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;


public class GenColorPalette{

    private static int CLUSTER_SIZE = 5;

    private int[] pixels;
    private int[] paletteColors = new int[CLUSTER_SIZE];
    private final Object lock = new Object();
    private int iterationsDone = 0;
    private int i = 0;

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
        

        for(i = 0; i < 10; i++){
            eService.execute(new Runnable(){
                final int it = i;
                int[] meds = new int[CLUSTER_SIZE];
                int[] clusterIndex = new int[pixels.length];
                int[] diffToMed = new int[pixels.length];
                @Override
                public void run() {
                    try{
                        Arrays.fill(clusterIndex, -1);
                        Arrays.fill(diffToMed,Integer.MAX_VALUE);
                        setInitMedoids(meds, pixels.length);
                        
                        assignCluster(pixels, clusterIndex, meds, diffToMed);      
                        
                        log(Arrays.toString(meds));
                        synchronized(lock){
                            iterationsDone += 1;
                            log("IT: " + it);
                        }
                    }
                    catch(Exception e){
                        Log.d("ColorPaletteModule", "Exception: " + e.toString());
                    }
                }
            });    
        }
    }

    /**
   * chooses random indexs from the pixel array as initial medoids
   * @param medArr  array of medoids
   * @param numB    length of pixel array
   */
    private void setInitMedoids(int[] medArr, int length){
        Random r = new Random();
        for(int i = 0; i < medArr.length; i++){
            medArr[i] = r.nextInt(length);
        }
    }


    /**
   * puts every pixel in the cluster whose medoid is closest to that pixel 
   * @param pixelArr    pixel array
   * @param clusterArr  (same size as pixelArr) i'th element has the index(of an element in medoidArr) of the cluster 
   *                    that the i'th pixel in pixelArr belongs to
   * @param medoidArr   (sized CLUSTER_SIZE) i'th element has the index(of an element in pixelArr) thats the medoid of the i'th cluster
   * @param diffArr     (same size as pixelArr) i'th element is the difference between the i'th element in pixelArr 
   *                    and the medoid of the cluster it belongs to
   * @return boolean    returns true if 1 or more pixel has been moved to a different cluster
   */
    private boolean assignCluster(int[] pixelArr, int[] clusterArr, int[] medoidArr, int[] diffArr){
        boolean change = false;
        for(int i = 0; i < pixelArr.length; i++){
            for(int j = 0; j < medoidArr.length; j++){
                int medVal = pixelArr[medoidArr[j]];
                if(Math.abs(medVal - pixelArr[i]) < diffArr[i]){
                    diffArr[i] = Math.abs(medVal - pixelArr[i]);
                    clusterArr[i] = j;
                    change = true;
                }
            }
        }
        return change;
    }


    /**
   * simple log method 
   * @param s String to log
   */
    private void log(String s){
        Log.d("ColorPaletteModule", s);
    }
    
    

}