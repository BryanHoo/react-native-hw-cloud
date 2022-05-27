package com.reactnativehwcloud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.obs.services.exception.ObsException;
import com.obs.services.model.CompleteMultipartUploadResult;
import com.obs.services.model.UploadFileRequest;

import java.io.IOException;

@ReactModule(name = HwCloudModule.NAME)
public class HwCloudModule extends ReactContextBaseJavaModule {
    public static final String NAME = "HwCloud";
    private static ObsClient obsClient;
    public static ReactContext mContext;

    public HwCloudModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
    }

    @ReactMethod
    public void upload(ReadableMap params, Promise promise) throws IOException {
      String endPoint = params.getString("endPoint");
      String ak = params.getString("ak");
      String sk = params.getString("sk");
      String token = params.getString("token");
      String bucketName = params.getString("bucketName");
      String objectName = params.getString("objectName");
      String filePath = params.getString("filePath");
      Integer socketTimeout = params.hasKey("socketTimeout") ? params.getInt("socketTimeout") : 60 * 60 * 1000;
      Integer connectionTimeout = params.hasKey("connectionTimeout") ? params.getInt("connectionTimeout") : 60 * 60 * 1000;
      // 分段最大并发数
      Integer taskNum = params.hasKey("taskNum") ? params.getInt("taskNum") : 5;
      // 分段大小 1M
      Integer partSize = params.hasKey("partSize") ? params.getInt("partSize") : 1024 * 1024;
      // 是否开启断点续传
      Boolean checkpoint = params.hasKey("checkpoint") ? params.getBoolean("checkpoint") : true;

      ObsConfiguration config = new ObsConfiguration();
      config.setSocketTimeout(socketTimeout);
      config.setConnectionTimeout(connectionTimeout);
      config.setEndPoint(endPoint);

      obsClient = new ObsClient(ak, sk, token, config);
      UploadFileRequest request = new UploadFileRequest(bucketName, objectName);

      request.setUploadFile(filePath);
      request.setTaskNum(taskNum);
      request.setPartSize(partSize);
      request.setEnableCheckpoint(checkpoint);
      // 发送上传进度
      request.setProgressListener(status -> {
        WritableMap data = Arguments.createMap();
        data.putInt("percentage", status.getTransferPercentage());
        sendEvent(mContext, "getPercentage", data);
      });
      try{
        // 进行断点续传上传
        CompleteMultipartUploadResult result = obsClient.uploadFile(request);
        promise.resolve(result);
      }catch (ObsException e) {
        // 发生异常时可再次调用断点续传上传接口进行重新上传
        promise.reject(e);
      }
      obsClient.close();
    }

    public static native Object nativeUpload(ReadableMap params);
}
