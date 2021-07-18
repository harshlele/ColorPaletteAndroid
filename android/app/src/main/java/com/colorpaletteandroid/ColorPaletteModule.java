package com.colorpaletteandroid;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Promise;

import android.util.Log;

public class ColorPaletteModule extends ReactContextBaseJavaModule{
    ColorPaletteModule(ReactApplicationContext context){
        super(context);
    }

    @Override
    public String getName(){
        return "ColorPaletteModule";
    }

    @ReactMethod
    public void getSize(ReadableArray a, Promise promise){
        Log.d("ColorPaletteModule","Arr Length: " + a.size());
        try{
            promise.resolve(a.size());
        }
        catch(Error e){
            promise.reject(e.toString());
        }
    }
}