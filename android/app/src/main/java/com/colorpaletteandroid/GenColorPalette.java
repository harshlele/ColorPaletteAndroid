package com.colorpaletteandroid;

import com.facebook.react.bridge.Promise;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;


public class GenColorPalette{

    private static int clusterSize = 5;

    private int[] pixels;
    private int[] paletteColors = new int[clusterSize];
    private final Object lock = new Object();
    private int iterationsDone = 0;
    private int i = 0;

    private ExecutorService eService = Executors.newFixedThreadPool(4);
    

    GenColorPalette(int[] pixels){
        this.pixels = pixels;
    }
    
    public void genPalette(Promise p){
        if(this.pixels == null || this.pixels.length == 0) {
            p.reject("Error: Pixels not set");
            return;
        }
        

        for(i = 0; i < 10; i++){
            eService.execute(new Runnable(){
                final int it = i;
                int[] clusters = new int[pixels.length];

                @Override
                public void run() {
                    try{
                        
                        synchronized(lock){
                            iterationsDone += 1;
                            
                        }
                    }
                    catch(InterruptedException ie){
                        Log.d("ColorPaletteModule", "InterruptedException: " + ie.toString());
                    }
                    catch(Exception e){
                        Log.d("ColorPaletteModule", "Exception: " + e.toString());
                    }
                }
            });    
        }
    }


    
    

}