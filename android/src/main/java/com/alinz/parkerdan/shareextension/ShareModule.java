package com.alinz.parkerdan.shareextension;

import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.util.List;

public class ShareModule extends ReactContextBaseJavaModule {


    public ShareModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ReactNativeShareExtension";
    }

    @ReactMethod
    public void close() {
        getCurrentActivity().finish();
    }

    @ReactMethod
    public void data(Promise promise) {
        promise.resolve(processIntent());
    }

    private WritableMap processIntent() {
        WritableMap map = Arguments.createMap();

        String value = "";
        String type = "";
        String action = "";

        Activity currentActivity = getCurrentActivity();

        if (currentActivity != null) {
            Intent intent = currentActivity.getIntent();
            action = intent.getAction();
            type = intent.getType();
            if (type == null) {
                type = "";
            }
            if (Intent.ACTION_SEND.equals(action)) {
                if (isTypeText(type)) {
                    value = intent.getStringExtra(Intent.EXTRA_TEXT);
                    type = "TEXT";
                } else if (isTypeImage(type) || isTypeVideo(type)) {
                    Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    String filePath = RealPathUtil.getRealPathFromURI(currentActivity, uri);
                    value = filePath != null ? "file://" + RealPathUtil.getRealPathFromURI(currentActivity, uri) : uri.toString();
                    type = isTypeImage(type) ? "IMAGE" : "VIDEO";
                }
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                final List<Uri> uriList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                WritableArray writableArray = Arguments.createArray();
                for (Uri uri : uriList) {
                    String filePath = RealPathUtil.getRealPathFromURI(currentActivity, uri);
                    writableArray.pushString(filePath != null ? "file://" + RealPathUtil.getRealPathFromURI(currentActivity, uri) : uri.toString());
                }
                map.putArray("list", writableArray);
                type = isTypeImage(type) ? "IMAGE" : isTypeVideo(type) ? "VIDEO" : "MIXED";
            } else {
                value = "";
            }
        } else {
            value = "";
            type = "";
        }

        map.putString("type", type);
        map.putString("value", value);

        return map;
    }

    private boolean isTypeText(String type) {
        return "text/plain".equals(type);
    }

    private boolean isTypeImage(String type) {
        return "image/*".equals(type) || "image/jpeg".equals(type) || "image/png".equals(type) || "image/jpg".equals(type);
    }

    private boolean isTypeVideo(String type) {
        return "video/*".equals(type) || "video/mov".equals(type) || "video/mp4".equals(type) || "video/quicktime".equals(type);
    }
}
