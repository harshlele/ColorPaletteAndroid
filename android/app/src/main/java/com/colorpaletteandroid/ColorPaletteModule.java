package com.colorpaletteandroid;
import java.io.IOException;
import java.util.Arrays;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.util.Log;
import android.net.Uri;
import android.graphics.Bitmap;
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
     * @param uri   Image URI
     */
    @ReactMethod
    public void getColorPalette(String uri){
        try{
            ImageDecoder.Source source = ImageDecoder.createSource(c.getContentResolver(),Uri.parse(uri));
            Bitmap bitmap = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true);            
            int newWidth, newHeight, oldWidth = bitmap.getWidth(), oldHeight = bitmap.getHeight();
            float aspectRatio = oldWidth / (float) oldHeight;
            
            if(oldWidth > oldHeight){
                newWidth = 1000;
                newHeight = Math.round(1000/aspectRatio);
            }
            else{
                newHeight = 1000;
                newWidth = Math.round(1000 * aspectRatio);
            }

            Bitmap resized = Bitmap.createScaledBitmap(bitmap,newWidth,newHeight,true);
            int[] pixels = new int[newWidth * newHeight];

            resized.getPixels(pixels,0,newWidth,0,0,newWidth,newHeight);

            GenColorPalette g = new GenColorPalette(pixels, this);
            g.genPalette();
            
            
        }
        catch(IOException io){
            Log.d("ColorPaletteModule", "IO ERROR: " + io.toString());
            this.onError(io);
        }
        catch(Exception e){
            Log.d("ColorPaletteModule", "GENCOLORPALETTE ERROR: " + e.toString());
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
        Log.d("ColorPaletteModule", "ARR: " + Arrays.toString(palette));
        
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
        Log.d("ColorPaletteModule", "ERR: " + e.toString());
        WritableMap map = Arguments.createMap();
        map.putString("msg",e.toString());

        ((ReactContext)c).getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("error", map);

    }


}