package com.lyk.jsbridge.youtuyundemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


import com.google.gson.Gson;
import com.lyk.jsbridge.*;
import com.lyk.jsbridge.common.Config;
import com.lyk.jsbridge.common.LoadingDialog;
import com.lyk.jsbridge.common.YTServerAPI;
import com.lyk.jsbridge.entity.IdentifyResult;
import com.lyk.jsbridge.facein.CardResultActivity;
import com.lyk.jsbridge.facein.CardVideoActivity;
import com.lyk.jsbridge.youtu.Youtu;
import com.lyk.jsbridge.youtu.sign.BitMapUtils;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/*
* demo展示如何调用优图开放平台API接口，网络请求返回的数据以log形式展示，请开发者用Android studio查看，
* 具体的接口协议请参考：http://open.youtu.qq.com。
*
* 请在Config.java里设置自己申请的 APP_ID, SECRET_ID, SECRET_KEY,否则网络请求签名验证会出错。
*
*
*人脸核身相关接口，需要申请权限接入，具体参考http://open.youtu.qq.com/welcome/service#/solution-facecheck
*人脸核身接口包括：
*	public JSONObject IdcardOcrVIP(Bitmap bitmap, int cardType) throws  IOException,
*			JSONException, KeyManagementException, NoSuchAlgorithmException;
*	public JSONObject IdcardNameVIP(String idNum, String idName) throws  IOException,
			JSONException, KeyManagementException, NoSuchAlgorithmException
*	public JSONObject IdcardNameVIP(String idNum, String idName) throws  IOException,
			JSONException, KeyManagementException, NoSuchAlgorithmException
*	public JSONObject FaceCompareVip(Bitmap bitmapA, Bitmap bitmapB) throws  IOException,
*			JSONException, KeyManagementException, NoSuchAlgorithmException
*	public JSONObject IdcardFaceCompare(Bitmap bitmap, String name, String idcard) throws  IOException,
*			JSONException, KeyManagementException, NoSuchAlgorithmException ;
*	public JSONObject LivegetFour() throws  IOException,
*			JSONException, KeyManagementException, NoSuchAlgorithmException;
*	public JSONObject LiveDetectFour(byte[] video, Bitmap bitmap, String validateData, boolean isCompare) throws  IOException,
*			JSONException, KeyManagementException, NoSuchAlgorithmException;
*	public JSONObject IdcardLiveDetectFour(byte[] video, String validateData, String name, String idcard) throws  IOException,
*			JSONException, KeyManagementException, NoSuchAlgorithmException;
*
*/

public class MainActivity extends Activity {
    private final String LOG_TAG = MainActivity.class.getName();
    private Bitmap theSelectedImage = null;
    private BitmapFactory.Options opts = null;
    private Button mLocalPicButton;
    private Button mremotePicButton;
    private Button mEnterFaceIn;
    private YTServerAPI mServerAPI;
    private LoadingDialog mLoadingDialog;
    private final static int REQUEST_IMAGE = 100;
    private String APP_ID = "";
    private String SECRET_ID = "";
    private String SECRET_KEY = "";
    private String p = null;
    private Bitmap bitmap = null;
    private int nCurrentCartType = 0;
    private String mName;
    private String mIdCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        APP_ID = Config.APP_ID;
        SECRET_ID = Config.SECRET_ID;
        SECRET_KEY = Config.SECRET_KEY;

        mLocalPicButton = (Button) findViewById(R.id.localTest);
        mremotePicButton = (Button) findViewById(R.id.remoteTest);
        mEnterFaceIn = (Button) findViewById(R.id.enterFaceIn);
        opts = new BitmapFactory.Options();
        opts.inDensity = this.getResources().getDisplayMetrics().densityDpi;
        opts.inTargetDensity = this.getResources().getDisplayMetrics().densityDpi;


        mLocalPicButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });

        mremotePicButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
//                            Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY, Youtu.API_YOUTU_END_POINT);
//                            JSONObject respose = faceYoutu.FaceCompareUrl("http://open.youtu.qq.com/content/img/slide-1.jpg", "http://open.youtu.qq.com/content/img/slide-1.jpg");
//                            Log.d(LOG_TAG, respose.toString());
                            Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY, Youtu.API_YOUTU_END_POINT);
                            Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.cccard, opts);
                            JSONObject respose = faceYoutu.FaceShape(selectedImage, 1);
                            Log.d(LOG_TAG, respose.toString());
                            if (null != selectedImage) {
                                selectedImage.recycle();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        mEnterFaceIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, CardVideoActivity.class);
//                startActivity(intent);
                selectImage();
            }
        });


//        testImageOcr();
        testFaceIn();
    }

    private void selectImage() {
        MultiImageSelector.create(MainActivity.this).showCamera(true) // 是否显示相机. 默认为显示
//                .count(1) // 最大选择图片数量, 默认为9. 只有在选择模式为多选时有效
                .single() // 单选模式
//                .multi() // 多选模式, 默认模式;
//                .origin(ArrayList<String>) // 默认已选择图片. 只有在选择模式为多选时有效
                .start(MainActivity.this, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                // 获取返回的图片列表
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                // 处理你自己的逻辑 ....
                if (path != null && path.size() > 0) {
                    p = path.get(0);
//                    onSelected();
                    bitmap = getImage(p);
//                    imageView.setImageBitmap(bitmap);
                    upload();
                }
            }
        }
    }

    private void backSuccess() {
//        mIdCardFist.setVisibility(View.INVISIBLE);
//        mIdCardSecond.setVisibility(View.VISIBLE);
//
//        mIndicatorLeft.setImageResource(R.drawable.indicator_correct);
//        mIndicatorRight.setImageResource(R.drawable.indicator_correct);
//
//        mErrorText.setText("");
//        mCameraPreview.releaseRes();
        Intent intent = new Intent(this, CardResultActivity.class);
        intent.putExtra("name", mName);
        intent.putExtra("idCard", mIdCard);
        startActivity(intent);

    }

    private void upload() {
        TecentHttpUtil.uploadIdCard(BitMapUtils.bitmapToBase64(bitmap), nCurrentCartType + "", new TecentHttpUtil.SimpleCallBack() {
            @Override
            public void Succ(String res) {
                IdentifyResult result = new Gson().fromJson(res, IdentifyResult.class);
                if (result != null) {
                    if (result.getErrorcode() == 0) {
                        // 识别成功
//                        showDialogInfo(result);
                        int errorCode = result.getErrorcode();
                        if (errorCode == 0) {
                            if (nCurrentCartType == 0) {
                                mName = result.getName();
                                mIdCard = result.getId();
//                                frontSuccess();
                                selectImage();
                                nCurrentCartType = 1;
                            } else {
                                backSuccess();
                            }

                        } else {
                            if (nCurrentCartType == 0) {
//                                frontFail();
                            } else {
//                                backFail();
                            }

                        }
                    } else {
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

    void testFaceIn() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY, Youtu.API_VIP_END_POINT);

                //IdcardOcrVIP
//                try {
//                    Log.d(LOG_TAG, "=====================================");
//                    Log.d(LOG_TAG, "IdcardOcrVIP");
//                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.id, opts);
//                    JSONObject respose = faceYoutu.IdcardOcrVIP(selectedImage, 0);
//                    Log.d(LOG_TAG, respose.toString());
//                    if(null != selectedImage) {
//                        selectedImage.recycle();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                //IdcardNameVIP
                try {
                    Log.d(LOG_TAG, "=====================================");
                    Log.d(LOG_TAG, "IdcardNameVIP");
                    String idNumber = "341221199001011234";
                    String idName = "李磊";
                    JSONObject respose = faceYoutu.IdcardNameVIP(idNumber, idName);
                    Log.d(LOG_TAG, respose.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }


                //FaceCompareVip

//                try {
//                    Log.d(LOG_TAG, "=====================================");
//                    Log.d(LOG_TAG, "FaceCompareVip");
//                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_2, opts);
//                    Bitmap selectedImage2 = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_1, opts);
//                    JSONObject respose = faceYoutu.FaceCompareVip(selectedImage, selectedImage2);
//                    Log.d(LOG_TAG, respose.toString());
//                    if(null != selectedImage) {
//                        selectedImage.recycle();
//                    }
//                    if(null != selectedImage2) {
//                        selectedImage2.recycle();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                //IdcardFaceCompare

//                try {
//                    Log.d(LOG_TAG, "=====================================");
//                    Log.d(LOG_TAG, "IdcardFaceCompare");
//                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_2, opts);
//                    JSONObject respose = faceYoutu.IdcardFaceCompare(selectedImage, "张三", "123456789012121234");
//                    Log.d(LOG_TAG, respose.toString());
//                    if(null != selectedImage) {
//                        selectedImage.recycle();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }


                //LivegetFour
//                try {
//                    Log.d(LOG_TAG, "=====================================");
//                    Log.d(LOG_TAG, "LivegetFour");
//                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_2, opts);
//                    JSONObject respose = faceYoutu.LivegetFour();
//                    Log.d(LOG_TAG, respose.toString());
//                    if(null != selectedImage) {
//                        selectedImage.recycle();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                //LiveDetectFour
//                try {
//                    Log.d(LOG_TAG, "=====================================");
//                    Log.d(LOG_TAG, "LiveDetectFour");
//
//                    InputStream stream = getResources().openRawResource(R.raw.video);
//                    ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
//                    byte[] b = new byte[1000];
//                    int n;
//                    while ((n = stream.read(b)) != -1)
//                        out.write(b, 0, n);
//                    stream.close();
//                    out.close();
//
//                    final byte[] vedioByte = out.toByteArray();
//                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_2, opts);
//                    JSONObject respose = faceYoutu.LiveDetectFour(vedioByte ,selectedImage, "3388", true);
//                    Log.d(LOG_TAG, respose.toString());
//                    if(null != selectedImage) {
//                        selectedImage.recycle();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }


                //IdcardLiveDetectFour
//                try {
//                    Log.d(LOG_TAG, "=====================================");
//                    Log.d(LOG_TAG, "IdcardLiveDetectFour");
//
//                    InputStream stream = getResources().openRawResource(R.raw.video);
//                    ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
//                    byte[] b = new byte[1000];
//                    int n;
//                    while ((n = stream.read(b)) != -1)
//                        out.write(b, 0, n);
//                    stream.close();
//                    out.close();
//
//                    final byte[] vedioByte = out.toByteArray();
//                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_2, opts);
//                    JSONObject respose = faceYoutu.IdcardLiveDetectFour(vedioByte ,"3388", "张三", "123456789012121234");
//                    Log.d(LOG_TAG, respose.toString());
//                    if(null != selectedImage) {
//                        selectedImage.recycle();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }


            }
        }).start();
    }

    void testImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY, API_TENCENTYUN_END_POINT);
                Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY, Youtu.API_YOUTU_END_POINT);
                //               Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY, "http://10.198.5.146/youtu/");

                try {
                    Log.d(LOG_TAG, "=====================================");
                    Log.d(LOG_TAG, "imagePorn");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.id, opts);
                    JSONObject respose = faceYoutu.ImagePorn(selectedImage);
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void testImageOcr() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY, API_TENCENTYUN_END_POINT);
                Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY, Youtu.API_YOUTU_END_POINT);
//                Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY, "http://101.226.76.164:18082/youtu/");

                try {
                    Log.d(LOG_TAG, "=====================================");
                    Log.d(LOG_TAG, "idcardocr");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.id, opts);
                    JSONObject respose = faceYoutu.IdcardOcr(selectedImage, 0);
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Log.d(LOG_TAG, "=====================================");
                    Log.d(LOG_TAG, "namecardocr");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.namecard, opts);
                    JSONObject respose = faceYoutu.NamecardOcr(selectedImage);
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    void testcase1() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY, API_TENCENTYUN_END_POINT);
                Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY, Youtu.API_YOUTU_END_POINT);

                Context context = getApplicationContext();
                Resources res = context.getResources();

                Uri noface = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(R.drawable.noface) + "/" + res.getResourceTypeName(R.drawable.noface) + "/" + res.getResourceEntryName(R.drawable.noface));
                Uri sameface1 = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(R.drawable.geyou_1) + "/" + res.getResourceTypeName(R.drawable.geyou_1) + "/" + res.getResourceEntryName(R.drawable.geyou_1));
                Uri sameface2 = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(R.drawable.geyou_2) + "/" + res.getResourceTypeName(R.drawable.geyou_2) + "/" + res.getResourceEntryName(R.drawable.geyou_2));
                Uri multiface = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(R.drawable.nose_tip_871_254) + "/" + res.getResourceTypeName(R.drawable.nose_tip_871_254) + "/" + res.getResourceEntryName(R.drawable.nose_tip_871_254));
                Uri multiface2 = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(R.drawable.multi1) + "/" + res.getResourceTypeName(R.drawable.multi1) + "/" + res.getResourceEntryName(R.drawable.multi1));
                Uri onface_in_multiface = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(R.drawable.oneface_femal) + "/" + res.getResourceTypeName(R.drawable.oneface_femal) + "/" + res.getResourceEntryName(R.drawable.oneface_femal));
                Uri broken_image1 = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(R.drawable.broken1) + "/" + res.getResourceTypeName(R.drawable.broken1) + "/" + res.getResourceEntryName(R.drawable.broken1));
                Uri broken_image2 = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(R.drawable.bad) + "/" + res.getResourceTypeName(R.drawable.bad) + "/" + res.getResourceEntryName(R.drawable.bad));
                Uri broken_image3 = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(R.drawable.broken2) + "/" + res.getResourceTypeName(R.drawable.broken2) + "/" + res.getResourceEntryName(R.drawable.broken2));
                Uri food = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(R.drawable.food01) + "/" + res.getResourceTypeName(R.drawable.food01) + "/" + res.getResourceEntryName(R.drawable.food01));
                Uri fuzzy_pic = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(R.drawable.fuzzy) + "/" + res.getResourceTypeName(R.drawable.fuzzy) + "/" + res.getResourceEntryName(R.drawable.fuzzy));
                Uri not_fuzzy_pic = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + res.getResourcePackageName(R.drawable.good_pic) + "/" + res.getResourceTypeName(R.drawable.good_pic) + "/" + res.getResourceEntryName(R.drawable.geyou_2));

                try {

                    Log.d(LOG_TAG, "=====================================");
                    Log.d(LOG_TAG, "detectFace");
                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "detect face mode 0");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_1, opts);
                    JSONObject respose = faceYoutu.DetectFace(selectedImage, 0);
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "detect face mode 1");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_1, opts);
                    JSONObject respose = faceYoutu.DetectFace(selectedImage, 1);
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "detect multiface mode 0");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.nose_tip_871_254, opts);
                    JSONObject respose = faceYoutu.DetectFace(selectedImage, 0);
                    // get respose
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "detect multiface mode 1");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.multi2, opts);
                    JSONObject respose = faceYoutu.DetectFace(selectedImage, 1);
                    // get respose
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "detect noface mode 0");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.noface, opts);
                    JSONObject respose = faceYoutu.DetectFace(selectedImage, 0);
                    // get respose
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "detect face image is illegal");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.broken1, opts);
                    JSONObject respose = faceYoutu.DetectFace(selectedImage, 0);
                    // get respose
                    Log.d(LOG_TAG, respose.toString());

                    Bitmap selectedImage2 = BitmapFactory.decodeResource(getResources(), R.drawable.broken2, opts);
                    JSONObject ret2 = faceYoutu.DetectFace(selectedImage2, 0);
                    Log.d(LOG_TAG, ret2.toString());

                    Bitmap selectedImage3 = BitmapFactory.decodeResource(getResources(), R.drawable.bad, opts);
                    JSONObject ret3 = faceYoutu.DetectFace(selectedImage3, 0);
                    Log.d(LOG_TAG, ret3.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                    if (null != selectedImage2) {
                        selectedImage2.recycle();
                    }
                    if (null != selectedImage3) {
                        selectedImage3.recycle();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "=====================================");
                    Log.d(LOG_TAG, "faceshape");
                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "faceshape mode 0");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_2, opts);
                    JSONObject respose = faceYoutu.FaceShape(selectedImage, 0);
                    // get respose
                    Log.d(LOG_TAG, respose.toString());

                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "FaceShape mode 1");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_2, opts);
                    JSONObject respose = faceYoutu.FaceShape(selectedImage, 1);
                    // get respose
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "shape multiface mode 0");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.multi2, opts);
                    ;
                    JSONObject respose = faceYoutu.FaceShape(selectedImage, 0);
                    // get respose
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "shape multiface mode 1");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.multi2, opts);
                    JSONObject respose = faceYoutu.FaceShape(selectedImage, 1);
                    // get respose
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "shape noface mode 0");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.noface, opts);
                    JSONObject respose = faceYoutu.FaceShape(selectedImage, 0);
                    // get respose
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "shape face image is illegal");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.broken1, opts);
                    JSONObject respose = faceYoutu.FaceShape(selectedImage, 0);
                    Log.d(LOG_TAG, respose.toString());

                    Bitmap selectedImage2 = BitmapFactory.decodeResource(getResources(), R.drawable.broken2, opts);
                    JSONObject ret2 = faceYoutu.FaceShape(selectedImage2, 0);
                    Log.d(LOG_TAG, ret2.toString());

                    Bitmap selectedImage3 = BitmapFactory.decodeResource(getResources(), R.drawable.bad, opts);
                    JSONObject ret3 = faceYoutu.FaceShape(selectedImage3, 0);
                    Log.d(LOG_TAG, ret3.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                    if (null != selectedImage2) {
                        selectedImage2.recycle();
                    }
                    if (null != selectedImage3) {
                        selectedImage3.recycle();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "=====================================");
                    Log.d(LOG_TAG, "FaceCompare");
                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "FaceCompare both face only one");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_2, opts);
                    Bitmap selectedImage2 = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_1, opts);
                    ;
                    JSONObject respose = faceYoutu.FaceCompare(selectedImage, selectedImage2);
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                    if (null != selectedImage2) {
                        selectedImage2.recycle();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "FaceCompare A multiface face B multiface");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.multi2, opts);
                    Bitmap selectedImage2 = BitmapFactory.decodeResource(getResources(), R.drawable.multi3, opts);

                    JSONObject respose = faceYoutu.FaceCompare(selectedImage, selectedImage2);
                    // get respose
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                    if (null != selectedImage2) {
                        selectedImage2.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "FaceCompare A face B no");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_2, opts);
                    Bitmap selectedImage2 = BitmapFactory.decodeResource(getResources(), R.drawable.noface, opts);

                    JSONObject respose = faceYoutu.FaceCompare(selectedImage, selectedImage2);
                    // get respose
                    Log.d(LOG_TAG, respose.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                    if (null != selectedImage2) {
                        selectedImage2.recycle();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    Log.d(LOG_TAG, "-------------------------------------");
                    Log.d(LOG_TAG, "FaceCompare A face B broken");
                    Bitmap selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.geyou_2, opts);
                    Bitmap selectedImage2 = BitmapFactory.decodeResource(getResources(), R.drawable.broken1, opts);
                    JSONObject respose = faceYoutu.FaceCompare(selectedImage, selectedImage2);
                    // get respose
                    Log.d(LOG_TAG, respose.toString());

                    Bitmap selectedImage3 = BitmapFactory.decodeResource(getResources(), R.drawable.broken2, opts);
                    JSONObject ret2 = faceYoutu.FaceCompare(selectedImage, selectedImage3);
                    Log.d(LOG_TAG, ret2.toString());

                    Bitmap selectedImage4 = BitmapFactory.decodeResource(getResources(), R.drawable.bad, opts);
                    JSONObject ret3 = faceYoutu.FaceCompare(selectedImage, selectedImage4);
                    Log.d(LOG_TAG, ret3.toString());
                    if (null != selectedImage) {
                        selectedImage.recycle();
                    }
                    if (null != selectedImage2) {
                        selectedImage2.recycle();
                    }
                    if (null != selectedImage3) {
                        selectedImage3.recycle();
                    }
                    if (null != selectedImage4) {
                        selectedImage4.recycle();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Bitmap getImage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }

    private Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK) {
//            Uri uri = data.getData();
//            Log.e("uri", uri.toString());
//            try {
//                String path = getPath(uri);
//                theSelectedImage = getBitmap(path, 1000, 1000);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (theSelectedImage != null) {
//                            try {
//                                Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY,Youtu.API_YOUTU_END_POINT);
//                                JSONObject respose = faceYoutu.DetectFace(theSelectedImage, 1);
//                                Log.d(LOG_TAG, respose.toString());
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }).start();
//            } catch ( Exception e) {
//                Log.e("Exception", e.getMessage(), e);
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String uri2Path(Uri uri) {
        String path = null;
        if (uri == null) {
            return path;
        }

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (!isKitKat) {
            //android 版本小于4.4
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (null != cursor) {
                cursor.moveToFirst();
                path = cursor.getString(1);
                cursor.close();
            }
        } else {
            //android 版本大于等于4.4
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }

            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{split[1]};

            path = getDataColumn(this, contentUri, selection, selectionArgs);

        }
        return path;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String getPath(final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(this, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(this, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("raw".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(this, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();

            return getDataColumn(this, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    private Bitmap getBitmap(String path, int maxWidth, int maxHeight) {
        //先解析图片边框的大小
        Bitmap bm = null;
        File file = new File(path);
        if (file.exists()) {
            BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inJustDecodeBounds = true;
            ops.inSampleSize = 1;
            BitmapFactory.decodeFile(path, ops);
            int oHeight = ops.outHeight;
            int oWidth = ops.outWidth;

            //控制压缩比
            int contentHeight = maxWidth;
            int contentWidth = maxHeight;
            if (((float) oHeight / contentHeight) < ((float) oWidth / contentWidth)) {
                ops.inSampleSize = (int) Math.ceil((float) oWidth / contentWidth);
            } else {
                ops.inSampleSize = (int) Math.ceil((float) oHeight / contentHeight);
            }
            ops.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeFile(path, ops);

        }

        return bm;
    }


}



