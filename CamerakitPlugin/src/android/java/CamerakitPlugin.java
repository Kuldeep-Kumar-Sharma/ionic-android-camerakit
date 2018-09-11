package com.kuldeep.camerakitplugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * This class echoes a string called from JavaScript.
 */
public class CamerakitPlugin extends CordovaPlugin {
    String imagepath="";
    String base64="";
    private CallbackContext callback = null;
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Context context = cordova.getActivity().getApplicationContext();
        callback = callbackContext;
        if(action.equals("CameraActivity")) {
            String message = args.getString(0);
            this.openNewActivity(message,context);
            return true;
        }
        return false;
    }

    private void openNewActivity(String name,Context context) {
        this.cordova.setActivityResultCallback (this);
        Intent intent = new Intent(context, com.kuldeep.camerakitplugin.CameraActivity.class);
        intent.putExtra("actionbarname",name);
        this.cordova.getActivity().startActivityForResult(intent,99);
    }

    private void echo(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

            if(requestCode == 99){
                if(intent == null){




                }else {
                    imagepath = intent.getExtras().get("imagepath").toString();
                    File image = new File(imagepath);
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    Bitmap bitmapextracted = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmapextracted.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    String resulted = imagepath + "," + encoded;
                    this.echo(resulted, callback);
                }
            }


    }


}
