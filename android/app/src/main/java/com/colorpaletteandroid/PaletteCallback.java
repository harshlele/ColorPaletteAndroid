package com.colorpaletteandroid;

/**
 * Interface to send data from worker threads to main thread. 
 * onPaletteGen is called by the worker threads after a color palette has been generated
 * onError is called after an Exception 
 */
interface PaletteCallback{
    void onPaletteGen(int[] palette,int[] clusterSizes, boolean Final);
    void onError(Exception e);
}