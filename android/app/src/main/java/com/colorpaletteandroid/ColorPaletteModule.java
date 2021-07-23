package com.colorpaletteandroid;
import java.io.IOException;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Promise;

import android.util.Log;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.content.Context;

public class ColorPaletteModule extends ReactContextBaseJavaModule{
    Context c;

    ColorPaletteModule(ReactApplicationContext context){
        super(context);
        
        //use c for all context related things, ReactApplicationContext is a subclass of Context
        c = context;
    }

    @Override
    public String getName(){
        return "ColorPaletteModule";
    }

    @ReactMethod
    public void getColorPalette(String uri, Promise promise){
        try{
            ImageDecoder.Source source = ImageDecoder.createSource(c.getContentResolver(),Uri.parse(uri));
            Bitmap bitmap = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true);
            Log.d("ColorPaletteModule", "WIDTH: " + bitmap.getWidth());
            Log.d("ColorPaletteModule", "HEIGHT: " + bitmap.getHeight());
            
            int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];

            bitmap.getPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());

            GenColorPalette g = new GenColorPalette(pixels);
            g.genPalette(promise);
            
            
        }
        catch(IOException io){
            Log.d("ColorPaletteModule", "ERROR: " + io.toString());
            promise.reject(io.toString());
        }
        catch(Error e){
            Log.d("ColorPaletteModule", "ERROR: " + e.toString());
            promise.reject(e.toString());
        }
    }
}