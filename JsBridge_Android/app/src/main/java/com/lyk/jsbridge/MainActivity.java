package com.lyk.jsbridge;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.Toast;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;
import com.google.gson.Gson;
import com.lyk.jsbridge.entity.IdentifyResult;
import com.lyk.jsbridge.facein.CameraActivity;
import com.lyk.jsbridge.facein.CardResultActivity;
import com.lyk.jsbridge.modle.User;
import com.lyk.jsbridge.photopick.ImageInfo;
import com.lyk.jsbridge.photopick.PhotoPickActivity;
import com.lyk.jsbridge.youtu.sign.BitMapUtils;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class MainActivity extends BaseActivty implements TencentLocationListener {

    private BridgeWebView mWebView;
    private final static int REQUEST_IDIMAGE = 100;
    private final static int REQUEST_DRIVEIMAGE = 200;
    private final static int REQUEST_BANKIMAGE = 300;
    private final static int REQUEST_PHOTO = 400;
    ValueCallback<Uri> mUploadMessage;
    private Bitmap bitmap = null;
    private int nCurrentCartType = 0;
    int RESULT_CODE = 0;
    private List<ContactBean> mContactBeanList;//所有联系人集合
    private TencentLocationManager mLocationManager;
    public final String USER_IMAGE_NAME = "image.png";
    public final String USER_CROP_IMAGE_NAME = "temporary.png";
    private static final String TAG = "MainActivity";
    private CallBackFunction contactFunction, cameraFunction, photoFunction, locationFunction, idCardFunction, driveCardFunction, bankCardFunction;
    public Uri imageUriFromCamera;
    public Uri cropImageUri;
    public final int GET_IMAGE_BY_CAMERA_U = 5001;
    public final int CROP_IMAGE_U = 5003;
    private long mLastBackTime = 0;
    private long TIME_DIFF = 2 * 1000;
    private String p = null;
    private TelephonyManager tm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setImmersionStatus();
        tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        mLocationManager = TencentLocationManager.getInstance(this);
        // 设置坐标系为 gcj-02, 缺省坐标为 gcj-02, 所以通常不必进行如下调用
        mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
        mWebView = (BridgeWebView) findViewById(R.id.webView);
        initWebView();
    }

    public void setImmersionStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 透明导航栏
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    private void selectImage(int result) {
        MultiImageSelector.create(MainActivity.this).showCamera(true) // 是否显示相机. 默认为显示
//                .count(1) // 最大选择图片数量, 默认为9. 只有在选择模式为多选时有效
                .single() // 单选模式
//                .multi() // 多选模式, 默认模式;
//                .origin(ArrayList<String>) // 默认已选择图片. 只有在选择模式为多选时有效
                .start(MainActivity.this, result);
    }

    private void initWebView() {
        // 设置具体WebViewClient
        mWebView.setWebViewClient(new MyWebViewClient(mWebView));
        // set HadlerCallBack
        mWebView.setDefaultHandler(new myHadlerCallBack());
        // setWebChromeClient
        mWebView.setWebChromeClient(new WebChromeClient() {

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
                this.openFileChooser(uploadMsg);
            }

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
                this.openFileChooser(uploadMsg);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                pickFile();
            }
        });

//        mWebView.loadUrl("file:///android_asset/demo.html");
//        mWebView.loadUrl("http://10.10.1.173:8077/");
        mWebView.loadUrl("http://10.10.11.189:4217/");
//        mWebView.loadUrl("http://wbs.rmbboxs.cn/#/chosen");
//        String ua = mWebView.getSettings().getUserAgentString();
//        mWebView.getSettings().setUserAgentString(ua + "; " + "xxc_android" );
        //必须和js函数名字一致，注册好具体执行回调函数，类似java实现类。
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
//		WebView.setWebContentsDebuggingEnabled(true);
        // This next one is crazy. It's the DEFAULT location for your app's
        // cache
        // But it didn't work for me without this line.
        // UPDATE: no hardcoded path. Thanks to Kevin Hawkins
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();

        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAppCachePath(appCachePath);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.registerHandler("submitFromWeb", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {

                String str = "这是html返回给java的数据:" + data;
                // 例如你可以对原始数据进行处理
                str = str + ",Java经过处理后截取了一部分：" + str.substring(0, 5);
                Log.i(TAG, "handler = submitFromWeb, data from web = " + data);
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
                //回调返回给Js
                function.onCallBack(str + ",Java经过处理后截取了一部分：" + str.substring(0, 5));
            }

        });

        mWebView.registerHandler("functionOpen", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                Toast.makeText(MainActivity.this, "网页在打开你的下载文件预览", Toast.LENGTH_SHORT).show();
                pickFile();

            }

        });
        mWebView.registerHandler("contact", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                contactFunction = function;
                requestContactPermission(Manifest.permission.READ_CONTACTS, function);

            }

        });
        mWebView.registerHandler("useragent", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                function.onCallBack("xxc_android");

            }

        });

        mWebView.registerHandler("deviceId", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {

                function.onCallBack(getdeviceId());

            }

        });
        mWebView.registerHandler("mobileType", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                Build build = new Build();

                function.onCallBack(build.MODEL);

            }

        });
        mWebView.registerHandler("statusBlack", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                StatusBarUtil.StatusBarLightMode(MainActivity.this);
            }
        });
        mWebView.registerHandler("idCard", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                selectImage(REQUEST_IDIMAGE);
                nCurrentCartType = Integer.parseInt(data);
                idCardFunction = function;
            }
        });
        mWebView.registerHandler("bankCard", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                selectImage(REQUEST_BANKIMAGE);
                nCurrentCartType = Integer.parseInt(data);
                bankCardFunction = function;
            }
        });
        mWebView.registerHandler("driverCard", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                selectImage(REQUEST_DRIVEIMAGE);
                nCurrentCartType = Integer.parseInt(data);
                driveCardFunction = function;
            }
        });
        mWebView.registerHandler("photo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                selectImage(REQUEST_PHOTO);
                photoFunction = function;
            }
        });

        mWebView.registerHandler("face", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("name", "沈硕");
                intent.putExtra("validate_data", "0000");
                intent.putExtra("idCard", "");
                startActivity(intent);
            }
        });
        mWebView.registerHandler("statusWhite", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                int a = 0;
                if (Rom.check(Rom.ROM_MIUI)) {
                    a = 1;
                } else if (Rom.check(Rom.ROM_FLYME)) {
                    a = 2;
                } else {
                    a = 3;
                }
                StatusBarUtil.StatusBarDarkMode(MainActivity.this, a);
            }
        });
        mWebView.registerHandler("youtu", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Intent intent = new Intent(MainActivity.this, com.lyk.jsbridge.youtuyundemo.MainActivity.class);
                startActivity(intent);
            }
        });
        mWebView.registerHandler("location", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                locationFunction = function;
                startLocation(function);
            }
        });
        mWebView.registerHandler("settings", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
        mWebView.registerHandler("share", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
        mWebView.registerHandler("activity", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                try {
                    startActivity(new Intent(MainActivity.this, Class.forName(data)));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        //模拟用户信息 获取本地位置，用户名返回给html
        User user = new User();
        user.setLocation("上海");
        user.setName("Bruce");
        // 回调 "functionInJs"
        mWebView.callHandler("toast", new Gson().toJson(user), new CallBackFunction() {
            @Override
            public void onCallBack(String data) {

                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();

            }
        });
        mWebView.send("hello");
        mWebView.registerHandler("camera", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                cameraFunction = function;
                if (Build.VERSION.SDK_INT >= 24) {  // 或者 android.os.Build.VERSION_CODES.KITKAT这个常量的值是19

                    onPermissionRequests(Manifest.permission.CAMERA, new OnBooleanListener() {
                        @Override
                        public void onClick(boolean bln) {
                            if (bln) {
                                Log.d("MainActivity", "进入权限");
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                File photoFile = createImagePathFile(MainActivity.this);
                                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

                                /*
                                * 这里就是高版本需要注意的，需用使用FileProvider来获取Uri，同时需要注意getUriForFile
                                * 方法第二个参数要与AndroidManifest.xml中provider的里面的属性authorities的值一致
                                * */
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                imageUriFromCamera = FileProvider.getUriForFile(MainActivity.this, "com.lyk.jsbridge.fileprovider", photoFile);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriFromCamera);

                                startActivityForResult(intent, GET_IMAGE_BY_CAMERA_U);
                            } else {
                                Toast.makeText(MainActivity.this, "扫码拍照或无法正常使用", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    imageUriFromCamera = createImagePathUri(MainActivity.this);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriFromCamera);
                    startActivityForResult(intent, GET_IMAGE_BY_CAMERA_U);
                }
            }
        });
    }

    public void stopLocation() {
        mLocationManager.removeUpdates(MainActivity.this);

    }

    public String getdeviceId() {
        return tm.getDeviceId();

    }

    @Override
    protected void onResume() {
        super.onResume();
        StatusBarUtil.StatusBarLightMode(this);

    }

    public void startLocation(CallBackFunction callBackFunction) {

        // 创建定位请求
        TencentLocationRequest request = TencentLocationRequest.create();

        // 修改定位请求参数, 定位周期 3000 ms

        mLocationManager.requestLocationUpdates(TencentLocationRequest.create().setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME).setInterval(500).setAllowDirection(true), this);
    }

    private void uploadbank(final int nCurrentCartType) {
        TecentHttpUtil.uploadBank(BitMapUtils.bitmapToBase64(bitmap), nCurrentCartType + "", new TecentHttpUtil.SimpleCallBack() {
            @Override
            public void Succ(String res) {
                IdentifyResult result = new Gson().fromJson(res, IdentifyResult.class);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(res);
                    jsonObject.put("bankimage", BitMapUtils.bitmapToBase64(bitmap));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (result != null) {
                    if (result.getErrorcode() == 0) {
                        // 识别成功
//                        showDialogInfo(result);
                        int errorCode = result.getErrorcode();
                        if (errorCode == 0) {
                            if (nCurrentCartType == 0) {
//                                mName = result.getName();
//                                mIdCard = result.getId();
////                                frontSuccess();
//                                selectImage();
//                                nCurrentCartType = 1;
                                bankCardFunction.onCallBack(jsonObject.toString());
                            } else {
//                                backSuccess();
                                bankCardFunction.onCallBack(res);
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

    private void uploaddrive(final int nCurrentCartType) {
        TecentHttpUtil.uploaddriver(BitMapUtils.bitmapToBase64(bitmap), nCurrentCartType + "", new TecentHttpUtil.SimpleCallBack() {
            @Override
            public void Succ(String res) {
                IdentifyResult result = new Gson().fromJson(res, IdentifyResult.class);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(res);
                    jsonObject.put("driveimage", BitMapUtils.bitmapToBase64(bitmap));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (result != null) {
                    if (result.getErrorcode() == 0) {
                        // 识别成功
//                        showDialogInfo(result);
                        int errorCode = result.getErrorcode();
                        if (errorCode == 0) {
                            if (nCurrentCartType == 0) {
//                                mName = result.getName();
//                                mIdCard = result.getId();
////                                frontSuccess();
//                                selectImage();
//                                nCurrentCartType = 1;
                                driveCardFunction.onCallBack(jsonObject.toString());
                            } else {
//                                backSuccess();
                                driveCardFunction.onCallBack(res);
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

    private void upload(final int nCurrentCartType) {
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
//                                mName = result.getName();
//                                mIdCard = result.getId();
////                                frontSuccess();
//                                selectImage();
//                                nCurrentCartType = 1;
                                idCardFunction.onCallBack(res);
                            } else {
//                                backSuccess();
                                idCardFunction.onCallBack(res);
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

    public Uri createImagePathUri(Activity activity) {
        //文件目录可以根据自己的需要自行定义
        Uri imageFilePath;
        File file = new File(activity.getExternalCacheDir(), USER_IMAGE_NAME);
        imageFilePath = Uri.fromFile(file);
        return imageFilePath;
    }

    public File createImagePathFile(Activity activity) {
        //文件目录可以根据自己的需要自行定义
        Uri imageFilePath;
        File file = new File(activity.getExternalCacheDir(), USER_IMAGE_NAME);
        imageFilePath = Uri.fromFile(file);
        return file;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IDIMAGE) {
            if (resultCode == RESULT_OK) {
                // 获取返回的图片列表
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                // 处理你自己的逻辑 ....
                if (path != null && path.size() > 0) {
                    p = path.get(0);
//                    onSelected();
                    bitmap = getImage(p);
//                    imageView.setImageBitmap(bitmap);
                    upload(nCurrentCartType);
                }
            }
        }
        if (requestCode == REQUEST_BANKIMAGE) {
            if (resultCode == RESULT_OK) {
                // 获取返回的图片列表
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                // 处理你自己的逻辑 ....
                if (path != null && path.size() > 0) {
                    p = path.get(0);
//                    onSelected();
                    bitmap = getImage(p);
//                    imageView.setImageBitmap(bitmap);
                    uploadbank(nCurrentCartType);
                }
            }
        }
        if (requestCode == REQUEST_DRIVEIMAGE) {
            if (resultCode == RESULT_OK) {
                // 获取返回的图片列表
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                // 处理你自己的逻辑 ....
                if (path != null && path.size() > 0) {
                    p = path.get(0);
//                    onSelected();
                    bitmap = getImage(p);
//                    imageView.setImageBitmap(bitmap);
                    uploaddrive(nCurrentCartType);
                }
            }
        }
        if (requestCode == REQUEST_PHOTO) {
            if (resultCode == RESULT_OK) {
                // 获取返回的图片列表
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                // 处理你自己的逻辑 ....
                if (path != null && path.size() > 0) {
                    p = path.get(0);
//                    onSelected();
                    bitmap = getImage(p);
//                    imageView.setImageBitmap(bitmap);
                    String a = BitmapUtils.bitmapToBase64(BitmapUtils.compressImage(bitmap));
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("img", "data:image/png;base64," + a);
                        photoFunction.onCallBack(jsonObject.toString());
                    } catch (JSONException e) {
                        //TODO
                    }

                }
            }
        }
        if (resultCode != this.RESULT_CANCELED) {
            switch (requestCode) {
                case GET_IMAGE_BY_CAMERA_U:
                    /*
                    * 这里我做了一下调用系统切图，高版本也有需要注意的地方
                    * */
                    if (imageUriFromCamera != null) {
                        cropImage(imageUriFromCamera, 1, 1, CROP_IMAGE_U);
                        break;
                    }
                    break;
                case CROP_IMAGE_U:
                    final String s = getExternalCacheDir() + "/" + USER_CROP_IMAGE_NAME;

                    Bitmap imageBitmap = GetBitmap(s, 320, 320);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);
                    String a = BitmapUtils.bitmapToBase64(BitmapUtils.compressImage(imageBitmap));
                    if (!TextUtils.isEmpty(a)) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("img", "data:image/png;base64," + a);
                            cameraFunction.onCallBack(jsonObject.toString());
                        } catch (JSONException e) {
                            //TODO
                        }
                    }
//                    try {
//                        cameraFunction.onCallBack(URLDecoder.decode(BitmapUtils.bitmapToBase64(imageBitmap),"utf-8"));
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }

                    break;
                default:
                    break;
            }
        }
        if (requestCode == PhotoPickActivity.REQUEST_PHOTO_CROP && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            String string = bundle.getString(PhotoPickActivity.EXTRA_RESULT_CROP_PHOTO);
            String path = ImageInfo.pathAddPreFix(string);
            String temPath = path.substring(path.indexOf("file://") + 7);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(temPath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            String b = BitmapUtils.bitmapToBase64(BitmapUtils.compressImage(bitmap));
            if (!TextUtils.isEmpty(b)) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("img", "data:image/png;base64," + b);
                    photoFunction.onCallBack(jsonObject.toString());
                } catch (JSONException e) {
                    //TODO
                }
            }


        }
//        if(requestCode == PhotoPickActivity.REQUEST_PHOTO_LIST && resultCode == Activity.RESULT_OK){
//            ArrayList<ImageInfo> imageInfos = (ArrayList<ImageInfo>) data.getSerializableExtra(PhotoPickActivity.EXTRA_RESULT_PHOTO_LIST);
//            for (int i = 0; i < imageInfos.size(); i++) {
//                switch (i){
//                    case 0:
//                        String path1 = ImageInfo.pathAddPreFix(imageInfos.get(i).path);
//                        String temPath1 = path1.substring(path1.indexOf("file://") + 7);
//                        break;
//                    case 1:
//
//                        String path2 = ImageInfo.pathAddPreFix(imageInfos.get(i).path);
//                        String temPath2 = path2.substring(path2.indexOf("file://") + 7);
//                        break;
//                }
//
//
//            }
//        }

    }

    public Bitmap GetBitmap(String path, int w, int h) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeFile(path, opts);
        int width = opts.outWidth;
        int height = opts.outHeight;
        float scaleWidth = 0.f, scaleHeight = 0.f;
        if (width > w || height > h) {
            scaleWidth = ((float) width) / w;
            scaleHeight = ((float) height) / h;
        }
        opts.inJustDecodeBounds = false;
        float scale = Math.max(scaleWidth, scaleHeight);
        opts.inSampleSize = (int) scale;
        WeakReference<Bitmap> weak = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));
        return Bitmap.createScaledBitmap(weak.get(), w, h, true);
    }

    public void cropImage(Uri imageUri, int aspectX, int aspectY, int return_flag) {
        File file = new File(this.getExternalCacheDir(), USER_CROP_IMAGE_NAME);
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= 24) {
            //高版本一定要加上这两句话，做一下临时的Uri
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            FileProvider.getUriForFile(MainActivity.this, "com.xuezj.fileproviderdemo.fileprovider", file);
        }
        cropImageUri = Uri.fromFile(file);

        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", 320);
        intent.putExtra("outputY", 320);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);

        startActivityForResult(intent, return_flag);
    }

    private void requestContactPermission(String permission, CallBackFunction function) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            //申请 WRITE_CONTACTS 权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        } else {
            initdata();

        }
    }

    public void initdata() {
        ContactInfoService mContactInfoService = new ContactInfoService(MainActivity.this);
        mContactBeanList = mContactInfoService.getContactList();//返回手机联系人对象集合
        mWebView.postDelayed(new Runnable() {
            @Override
            public void run() {
                contactFunction.onCallBack(ContactInfoService.changeArrayDateToJson(mContactBeanList));
            }
        }, 1000);
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("location", tencentLocation.getAddress());
            jsonObject.put("Latitude", tencentLocation.getLatitude());
            jsonObject.put("Longitude", tencentLocation.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        locationFunction.onCallBack(jsonObject.toString());

    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {

    }

    /**
     * 自定义的WebViewClient
     */
    class MyWebViewClient extends BridgeWebViewClient {

        public MyWebViewClient(BridgeWebView webView) {
            super(webView);
        }
    }


    /**
     * 自定义回调
     */
    class myHadlerCallBack extends DefaultHandler {

        @Override
        public void handler(String data, CallBackFunction function) {
            if (function != null) {

                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
                function.onCallBack("沈少");
            }
        }
    }

    public void pickFile() {
        Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooserIntent.setType("image/*");
        startActivityForResult(chooserIntent, RESULT_CODE);
    }


}
