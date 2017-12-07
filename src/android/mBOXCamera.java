package mirimmedialab.co.kr.mboxcamera;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.Context;

/**
 * This class echoes a string called from JavaScript.
 */
public class mBOXCamera extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Context context = cordova.getActivity().getApplicationContext();
        if (action.equals("openCamera")) {
             for(int i=0; i<args.length(); i++){

                JSONObject obj = args.getJSONObject(i);
                String imgURL = obj.getString("imgURL");
                this.openCamera(context, imgURL);
                break;
            }   
             
            return true;
        }
        return false;
    }

   private void openCamera(Context context, String imgURL) {
        Intent intent = new Intent(context, mirimmedialab.co.kr.mboxcamera.cls.MainActivity.class);
        intent.putExtra("imgURL", imgURL);
        this.cordova.getActivity().startActivity(intent);
    }
}
