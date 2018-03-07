package com.lyk.jsbridge;

import android.util.Log;

import com.lyk.jsbridge.youtu.sign.YoutuSign;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;


/**
 * Created by shenshao on 2016/10/19.
 * 网络请求工具类
 */
public class TecentHttpUtil {

    public static void uploadIdCard(String bitmap, String card_type, final SimpleCallBack callback) {
        StringBuffer mySign = new StringBuffer("");
        YoutuSign.appSign(Constant.AppID, Constant.SecretID, Constant.SecretKey,
                System.currentTimeMillis() / 1000 + Constant.EXPIRED_SECONDS,
                Constant.QQNumber, mySign);
        RequestParams params = new RequestParams("http://api.youtu.qq.com/youtu/ocrapi/idcardocr");
        params.setAsJsonContent(true);
        params.addHeader("accept", "*/*");
        params.addHeader("Host", "api.youtu.qq.com");
        params.addHeader("user-agent", "youtu-java-sdk");
        params.addHeader("Authorization", mySign.toString());
        params.addHeader("Content-Type", "text/json");
        params.addParameter("card_type", Integer.valueOf(card_type));
        params.addBodyParameter("image", bitmap);
        params.addBodyParameter("app_id", Constant.AppID);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {Log.d("onSuccess",result);
                callback.Succ(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.d("onError",ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.d("onCancelled", cex.getMessage());
            }

            @Override
            public void onFinished() {

            }
        });

    }
    public static void uploaddriver(String bitmap, String card_type, final SimpleCallBack callback) {
        StringBuffer mySign = new StringBuffer("");
        YoutuSign.appSign(Constant.AppID, Constant.SecretID, Constant.SecretKey,
                System.currentTimeMillis() / 1000 + Constant.EXPIRED_SECONDS,
                Constant.QQNumber, mySign);
        RequestParams params = new RequestParams("https://api.youtu.qq.com/youtu/ocrapi/driverlicenseocr");
        params.setAsJsonContent(true);
        params.addHeader("accept", "*/*");
        params.addHeader("Host", "api.youtu.qq.com");
        params.addHeader("user-agent", "youtu-java-sdk");
        params.addHeader("Authorization", mySign.toString());
        params.addHeader("Content-Type", "text/json");
        params.addParameter("card_type", Integer.valueOf(card_type));
        params.addBodyParameter("image", bitmap);
        params.addBodyParameter("app_id", Constant.AppID);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d("onSuccess",result);
                callback.Succ(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.d("onError",ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.d("onCancelled", cex.getMessage());
            }

            @Override
            public void onFinished() {

            }
        });

    }
    public static void uploadBank(String bitmap, String card_type, final SimpleCallBack callback) {
        StringBuffer mySign = new StringBuffer("");
        YoutuSign.appSign(Constant.AppID, Constant.SecretID, Constant.SecretKey,
                System.currentTimeMillis() / 1000 + Constant.EXPIRED_SECONDS,
                Constant.QQNumber, mySign);
        RequestParams params = new RequestParams("https://api.youtu.qq.com/youtu/ocrapi/creditcardocr");
        params.setAsJsonContent(true);
        params.addHeader("accept", "*/*");
        params.addHeader("Host", "api.youtu.qq.com");
        params.addHeader("user-agent", "youtu-java-sdk");
        params.addHeader("Authorization", mySign.toString());
        params.addHeader("Content-Type", "text/json");
        params.addParameter("card_type", Integer.valueOf(card_type));
        params.addBodyParameter("image", bitmap);
        params.addBodyParameter("app_id", Constant.AppID);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d("onSuccess",result);
                callback.Succ(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.d("onError",ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.d("onCancelled", cex.getMessage());
            }

            @Override
            public void onFinished() {

            }
        });

    }
    public static void uploadvideo(File video, String card_type, final SimpleCallBack callback) {
        StringBuffer mySign = new StringBuffer("");
//        YoutuSign.appSign(Constant.AppID, Constant.SecretID, Constant.SecretKey,
//                System.currentTimeMillis() / 1000 + Constant.EXPIRED_SECONDS,
//                Constant.QQNumber, mySign);
        RequestParams params = new RequestParams("http://xinxiangche2099.rmbboxs.cn/api/user/saveFile");
        params.setAsJsonContent(true);
        params.addHeader("accept", "*/*");
        params.addHeader("token", "W4tUlGD0zC/ikvUQi+9O5bN8HHMGpDg4zDwEiGHBkrgqtrgTCv/ucFhQYUzu268oeJ+ZawTaYOA=");
//        params.addHeader("Host", "api.youtu.qq.com");
        params.addHeader("user-agent", "");
        params.addHeader("Authorization", mySign.toString());
        params.addHeader("Content-Type", "text/json");
        params.addParameter("fileType", card_type);
        params.addBodyParameter("file", video);
        params.addBodyParameter("app_id", Constant.AppID);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d("onSuccess",result);
                callback.Succ(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.d("onError",ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.d("onCancelled", cex.getMessage());
            }

            @Override
            public void onFinished() {

            }
        });

    }
    public interface SimpleCallBack {
        void Succ(String result);

        void error();
    }


}
