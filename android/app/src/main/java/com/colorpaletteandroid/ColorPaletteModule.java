package com.colorpaletteandroid;
import java.io.IOException;
import java.util.Arrays;
import java.io.InputStream;
import java.lang.Runtime;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.util.Log;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.content.Context;
import android.graphics.Color;

/**
 * Android Native module to generate a color palette from an image. 
 */
public class ColorPaletteModule extends ReactContextBaseJavaModule implements PaletteCallback{

    //used for all Context related things, ReactContext/ReactApplicationContext extend Context, 
    //so they can be used by type casting c as well
    Context c;

    ColorPaletteModule(ReactApplicationContext context){
        super(context);
        c = context;
    }

    @Override
    public String getName(){
        return "ColorPaletteModule";
    }

    /**
     * Loads a bitmap of the image, scales it down, and passes it to GenColorPalette
     * @param uri   Image uri
     */
    @ReactMethod
    public void getColorPalette(String uri){
        try{
            //just get width and height without actually loading the bitmap into memory
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream in = c.getContentResolver().openInputStream(Uri.parse(uri));
            BitmapFactory.decodeStream(in,null,options);
            in.close();

            //calculate down-scaled width/height
            int newWidth,newHeight,maxVal;
            int width = options.outWidth; 
            int height = options.outHeight;
            float aspectRatio = width / (float) height;
            

            if(Runtime.getRuntime().availableProcessors() > 4)
                maxVal = 1000;
            else
                maxVal = 500;

            if(width > height){
                newWidth = maxVal;
                newHeight = Math.round(maxVal/aspectRatio);
            }
            else{
                newHeight = maxVal;
                newWidth = Math.round(maxVal * aspectRatio);
            }
            //calculate ratio by which image will be downscaled
            int downScale = calculateInSampleSize(width, height, newWidth, newHeight);

            //load downscaled bitmap
            options.inSampleSize = downScale;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig=Bitmap.Config.ARGB_8888;
            in = c.getContentResolver().openInputStream(Uri.parse(uri));
            Bitmap resized = BitmapFactory.decodeStream(in,null,options);
            in.close();
            
            newWidth = resized.getWidth();
            newHeight = resized.getHeight();
            int[] pixels = new int[newWidth * newHeight];
            resized.getPixels(pixels,0,newWidth,0,0,newWidth,newHeight);
            
            //generate palette
            GenColorPalette g = new GenColorPalette(pixels, this);
            g.genPalette();
        }
        catch(IOException io){
            log("IO ERROR: " + io.toString());
            this.onError(io);
        }
        catch(Exception e){
            log("GENCOLORPALETTE ERROR: " + e.toString());
            this.onError(e);
        }
    }

    /*Interface Methods - Are called from the threads, and send data to JS */

    /**
     * Called by the worker threads after a palette has been generated
     * @param palette   array containing the colors generated from the image
     * @param Final     if true, this is the lowest-cost solution
     */
    public void onPaletteGen(int[] palette, boolean Final){
        WritableArray paletteArr = new WritableNativeArray();
        for(int i = 0; i < palette.length; i++){
            WritableMap map = Arguments.createMap();
            map.putInt("r",Color.red(palette[i]));
            map.putInt("g",Color.green(palette[i]));
            map.putInt("b",Color.blue(palette[i]));
            paletteArr.pushMap(map);
        }

        WritableMap resMap = Arguments.createMap();
        resMap.putArray("palette",paletteArr);
        resMap.putBoolean("final",Final);

        ((ReactContext)c).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("paletteGen", resMap);
    }

    /**
     * Called by the worker threads in case of any Exception
     * @param e     the exception thrown
     */
    public void onError(Exception e){
        WritableMap map = Arguments.createMap();
        map.putString("msg",e.toString());

        ((ReactContext)c).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("error", map);
    }

    /**
     * calculate the "ratio" by which image is scaled down by
     * @see https://developer.android.com/topic/performance/graphics/load-bitmap
     * @param imgWidth      image width
     * @param imgHeight     image height
     * @param reqWidth      downscaled image width
     * @param reqHeight     downscaled image height
     * @return int          ratio by which image is scaled down
     */
    private int calculateInSampleSize(int imgWidth, int imgHeight, int reqWidth, int reqHeight) {
        int inSampleSize = 1;

        if (imgHeight > reqHeight || imgWidth > reqWidth) {

            final int halfHeight = imgHeight / 2;
            final int halfWidth = imgWidth / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * log method
     * @param s  string to log
     */
    private void log(String s){
        Log.d("ColorPaletteAndroid",s);
    }
}