package com.lyk.jsbridge.facein;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.lyk.jsbridge.R;
import com.lyk.jsbridge.TecentHttpUtil;
import com.lyk.jsbridge.common.FileUtil;
import com.lyk.jsbridge.common.ImageUtil;
import com.lyk.jsbridge.common.LoadingDialog;
import com.lyk.jsbridge.common.YTServerAPI;
import com.lyk.jsbridge.entity.IdentifyResult;
import com.lyk.jsbridge.youtu.sign.BitMapUtils;
import com.lyk.jsbridge.youtuyundemo.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by qingliang on 16/9/14.
 */
public class CardVideoActivity extends Activity {
    private static final String LOG_TAG = YTServerAPI.class.getName();
    private CameraPreview mCameraPreview;
    private Button mPicButton;
    private ImageView mIdCardFist;
    private ImageView mIdCardSecond;
    private ImageView mIndicatorLeft;
    private ImageView mIndicatorRight;
    private TextView mWarningText;
    private TextView mErrorText;

    private Bitmap mFaceBitmap;
    private int nCurrentCartType;
    private YTServerAPI mServer;
    private LoadingDialog mLoading;
    private Handler mHandler;
    private String mName;
    private String mIdCard;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cardvideo);
        mCameraPreview = (CameraPreview) findViewById(R.id.camPreview);
        mPicButton = (Button) findViewById(R.id.startPic);
        mIdCardFist = (ImageView) findViewById(R.id.idCaradFirst);
        mIdCardSecond = (ImageView) findViewById(R.id.idCaradSecond);
        mIndicatorLeft = (ImageView) findViewById(R.id.indicatorLeft);
        mIndicatorRight = (ImageView) findViewById(R.id.indicatorRight);
        mWarningText = (TextView) findViewById(R.id.warningText);
        mErrorText = (TextView) findViewById(R.id.errorText);


        mPicButton.setEnabled(true);
        nCurrentCartType = 0;
        mCameraPreview.setCameraFrontBack(CameraPreview.CameraBack);
        initUI();
        mServer = new YTServerAPI();
        mLoading = new LoadingDialog(this);
        mHandler = new Handler();

        mCameraPreview.setOnTakePicCallBack(new CameraPreview.OnTakePicCallBack() {
            @Override
            public void onPictureTaken(byte[] data) {
                mFaceBitmap = byteToBitmap(data);
                uploadImage();
                //save pics to SDCard
                FileUtil.saveBitmap(mFaceBitmap);
            }
        });
        mPicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraPreview.takePicture();
            }
        });

        mServer.setRequestListener(new YTServerAPI.OnRequestListener() {
            @Override
            public void onSuccess(int statusCode, final String responseBody) {
                Log.d(LOG_TAG, responseBody);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mLoading.dismiss();
                            mPicButton.setEnabled(true);
                            JSONObject jsonObject = new JSONObject(responseBody.toString());
                            int errorCode = jsonObject.getInt("errorcode");
                            if (errorCode == 0) {
                                if (nCurrentCartType == 0) {
                                    mName = jsonObject.getString("name");
                                    mIdCard = jsonObject.getString("id");
                                    frontSuccess();
                                    nCurrentCartType = 1;
                                }else{
                                    backSuccess();
                                }

                            }else {
                                if (nCurrentCartType == 0) {
                                    frontFail();
                                }else{
                                    backFail();
                                }

                            }

                        }catch (JSONException e){

                        }

                    }
                });
            }

            @Override
            public void onFailure(int statusCode) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLoading.dismiss();
                        mPicButton.setEnabled(true);
                        if (nCurrentCartType == 0) {
                            frontFail();
                        }else{
                            backFail();
                        }
                    }
                });


            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraPreview.onResume();
    }

    @Override
    public void onPause() {
        mCameraPreview.onPause();

        super.onPause();
    }

    @Override
    public void onDestroy(){
        mCameraPreview.releaseRes();
        if (mFaceBitmap != null && !mFaceBitmap.isRecycled()){
            mFaceBitmap.recycle();
        }

        super.onDestroy();
    }

    private void initUI(){
        mIdCardFist.setVisibility(View.VISIBLE);
        mIdCardSecond.setVisibility(View.INVISIBLE);

        mIndicatorLeft.setImageResource(R.drawable.indicator_nor);
        mIndicatorRight.setImageResource(R.drawable.indicator_nor);
        mWarningText.setText("将身份证头像面放入框内");
    }


    private void uploadImage(){
        //上传的图片大小不能超过2M，请开发者自行处理
        mLoading.setText("身份证识别中");
        mLoading.show();
        mPicButton.setEnabled(false);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    mServer.idCardOcr(ImageUtil.ratio(mFaceBitmap,240,120), nCurrentCartType);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (KeyManagementException e) {
//                    e.printStackTrace();
//                } catch (NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }).start();
        TecentHttpUtil.uploadIdCard(BitMapUtils.bitmapToBase64(mFaceBitmap), nCurrentCartType+"", new TecentHttpUtil.SimpleCallBack() {
            @Override
            public void Succ(String res) {
                IdentifyResult result = new Gson().fromJson(res, IdentifyResult.class);
                if(result != null){
                    if(result.getErrorcode() == 0){
                        // 识别成功
//                        showDialogInfo(result);
                        int errorCode = result.getErrorcode();
                        if (errorCode == 0) {
                            if (nCurrentCartType == 0) {
                                mName = result.getName();
                                mIdCard = result.getId();
                                frontSuccess();
                                nCurrentCartType = 1;
                            }else{
                                backSuccess();
                            }

                        }else {
                            if (nCurrentCartType == 0) {
                                frontFail();
                            }else{
                                backFail();
                            }

                        }
                    }else {
//                        Toast.makeText(MainActivity.this, result.getErrormsg(), Toast.LENGTH_SHORT).show();
                                /*switch (result.getErrorcode()){
                                    case -7001:
                                        Toast.makeText(MainActivity.this, "未检测到身份证，请对准边框(请避免拍摄时倾角和旋转角过大、摄像头)", Toast.LENGTH_SHORT).show();
                                        break;
                                    case -7002:
                                        Toast.makeText(MainActivity.this, "请使用第二代身份证件进行扫描", Toast.LENGTH_SHORT).show();
                                        break;
                                    case -7003:
                                        Toast.makeText(MainActivity.this, "不是身份证正面照片(请使用带证件照的一面进行扫描)", Toast.LENGTH_SHORT).show();
                                        break;
                                    case -7004:
                                        Toast.makeText(MainActivity.this, "不是身份证反面照片(请使用身份证反面进行扫描)", Toast.LENGTH_SHORT).show();
                                        break;
                                    case -7005:
                                        Toast.makeText(MainActivity.this, "确保扫描证件图像清晰", Toast.LENGTH_SHORT).show();
                                        break;
                                    case -7006:
                                        Toast.makeText(MainActivity.this, "请避开灯光直射在证件表面", Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        Toast.makeText(MainActivity.this, "识别失败，请稍后重试", Toast.LENGTH_SHORT).show();
                                        break;
                                }*/
                    }
                }
            }

            @Override
            public void error() {

            }
        });

    }

    private void frontSuccess(){
        mIdCardFist.setVisibility(View.INVISIBLE);
        mIdCardSecond.setVisibility(View.VISIBLE);

        mIndicatorLeft.setImageResource(R.drawable.indicator_correct);
        mIndicatorRight.setImageResource(R.drawable.indicator_nor);
        mWarningText.setText("请拍摄身份证国徽面");
        mErrorText.setText("");

    }
    private void frontFail(){
        mIdCardFist.setVisibility(View.VISIBLE);
        mIdCardSecond.setVisibility(View.INVISIBLE);

        mIndicatorLeft.setImageResource(R.drawable.indicator_wrong);
        mErrorText.setText("OCR识别失败, 请重试");

    }
    private void backSuccess(){
        mIdCardFist.setVisibility(View.INVISIBLE);
        mIdCardSecond.setVisibility(View.VISIBLE);

        mIndicatorLeft.setImageResource(R.drawable.indicator_correct);
        mIndicatorRight.setImageResource(R.drawable.indicator_correct);

        mErrorText.setText("");
        mCameraPreview.releaseRes();
        Intent intent = new Intent(this, CardResultActivity.class);
        intent.putExtra("name", mName);
        intent.putExtra("idCard", mIdCard);
        startActivity(intent);

    }
    private void backFail(){
        mIdCardFist.setVisibility(View.INVISIBLE);
        mIdCardSecond.setVisibility(View.VISIBLE);

        mIndicatorLeft.setImageResource(R.drawable.indicator_correct);
        mIndicatorRight.setImageResource(R.drawable.indicator_wrong);
        mErrorText.setText("OCR识别失败, 请重试");
    }


    public static Bitmap byteToBitmap(byte[] imgByte) {
        InputStream input = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;   //上传的图片大小不能超过2M，请开发者自行处理
        input = new ByteArrayInputStream(imgByte);
        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
                input, null, options));
        bitmap = (Bitmap) softRef.get();
        bitmap = ImageUtil.getRotateBitmap(bitmap, 90.0f);
        if (imgByte != null) {
            imgByte = null;
        }

        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}
