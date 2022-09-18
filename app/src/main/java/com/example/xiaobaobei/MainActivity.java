package com.example.xiaobaobei;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.example.xiaobaobei.bean.BuyerNum;
import com.example.xiaobaobei.service.KeepAliveService;
import com.example.xiaobaobei.util.HelpUtil;
import com.example.xiaobaobei.util.HttpClient;
import com.example.xiaobaobei.util.NotificationSetUtil;
import com.example.xiaobaobei.util.UpdateApk;
import com.example.xiaobaobei.util.WindowPermissionCheck;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


/**
 * 佣金支持卡小数点
 * 停止接单取消所有网络请求
 * 远程公告、频率等
 * try catch
 * 多买号情况下，不选择买号接单的问题
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText etUname,etPaw,etYj1;
    private TextView tvStart,tvStop,tvLog,tvAppDown,tvAppOpen,tvBrow,tvGetTitle,tvTitle;

    private Handler mHandler;
    private String tbId;
    private int tbIndex;
    /*
    接单成功音乐提示播放次数（3次）
    播放的次数是count+1次
     */
    private int count;
    private SharedPreferences userInfo;
    private int minPl;
    private Double minYj;
    private String token;
    private String yqToken;  //邀请token
    private List<BuyerNum> buyerNumList;
    private boolean isAuth = false;

    private AlertDialog alertDialog2;
    private String[] tbNameArr;
    private Dialog dialog;

    private static String LOGIN_URL = "";
    private static String BROW_OPEN = "";
    private static String DOWNLOAD = "";
    private String version = "";
    private String uuid;



    private static final String LOGIN = "/api/member/login.html";
    private static final String QUAN_XIAN = "/api/member/searchPhoneToUserNum";
    private static final String GET_TB_INFO = "/api/member/platform";
    private static final String SETTING_TB = "/api/member/platform_isaccept";
    private static final String GET_AUTH = "/api/order/getAuth";
    private static final String GET_TASK = "/api/order/acceptV2";
    private static final String LQ_TASK = "/api/order/sureOrderV2";
    private static final String GET_SHOP_DETAIL = "/api/order/getDetail";
    private static final String GET_SHOP_IMAGE = "/api/order/getMyOrderList.html";
    private static final String QUIT_TASK = "/api/order/noOrder";




    /**
     * 需要更改的地方：
     * 1、MainActivity
     * 2、build.gradle配置文件
     * 3、AndroidMainfest.xml文件
     * 4、Update文件
     * 5、KeepAlive文件
     */

//    private static final String PT_NAME = "guangMingDing";
//    private static final String TITLE = "光明顶助手";
//    private static final String SUCCESS_TI_SHI = "光明顶接单成功";
//    private static final String TI_SHI = "光明顶App未安装";
//    private static final String CHANNELID = "guangmingdingSuccess";
//    private static final String APK_PACKAGE = "app.guanmingding.com";
//    private static int ICON = R.mipmap.guangmingding;
//    private static final int JIE_DAN_SUCCESS = R.raw.gmd_success;
//    private static final int JIE_DAN_FAIL = R.raw.gmd_fail;


//    private static final String PT_NAME = "diDiDa";
//    private static final String TITLE = "滴滴哒助手";
//    private static final String SUCCESS_TI_SHI = "滴滴哒接单成功";
//    private static final String TI_SHI = "滴滴哒App未安装";
//    private static final String CHANNELID = "dididaSuccess";
//    private static final String APK_PACKAGE = "com.app.comddd";
//    private static int ICON = R.mipmap.didida;
//    private static final int JIE_DAN_SUCCESS = R.raw.ddd_success;
//    private static final int JIE_DAN_FAIL = R.raw.ddd_fail;


    private static final String PT_NAME = "luDingJi";
    private static final String TITLE = "鹿鼎记助手";
    private static final String SUCCESS_TI_SHI = "鹿鼎记接单成功";
    private static final String TI_SHI = "鹿鼎记App未安装";
    private static final String CHANNELID = "ludingjiSuccess";
    private static final String APK_PACKAGE = "app.guanmingding.com";
    private static int ICON = R.mipmap.guangmingding;
    private static final int JIE_DAN_SUCCESS = R.raw.ldj_success;
    private static final int JIE_DAN_FAIL = R.raw.ldj_fail;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, KeepAliveService.class);
        startService(intent);//启动保活服务
        ignoreBatteryOptimization();//忽略电池优化
        if(!checkFloatPermission(this)){
            //权限请求方法
            requestSettingCanDrawOverlays();
        }
        initView();
    }


    private void initView(){
        //检查更新
        UpdateApk.update(MainActivity.this);
        //是否开启通知权限
        openNotification();
        //是否开启悬浮窗权限
        WindowPermissionCheck.checkPermission(this);
        //获取平台地址
        getPtAddress();
        mHandler = new Handler();
        etYj1 = findViewById(R.id.et_yj1);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(TITLE);
        tvAppDown = findViewById(R.id.tv_appDown);
        tvAppOpen = findViewById(R.id.tv_appOpen);
        tvBrow = findViewById(R.id.tv_brow);
        etUname = findViewById(R.id.et_username);
        etPaw = findViewById(R.id.et_password);
        tvStart = findViewById(R.id.tv_start);
        tvStop = findViewById(R.id.tv_stop);
        tvLog = findViewById(R.id.tv_log);
        getUserInfo();//读取用户信息
        //设置textView为可滚动方式
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvLog.setTextIsSelectable(true);
        tvStart.setOnClickListener(this);
        tvStop.setOnClickListener(this);
        tvBrow.setOnClickListener(this);
        tvAppOpen.setOnClickListener(this);
        tvAppDown.setOnClickListener(this);
        tvGetTitle = findViewById(R.id.tv_getTitle);
        tvGetTitle.setOnClickListener(this);
        tvLog.setText("app左上角可直接联系客服~"+"\n");
        buyerNumList = new ArrayList<>();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_start:
                if("".equals(etYj1.getText().toString().trim())){
                    etYj1.setText("0");
                }
                if(Double.parseDouble(etYj1.getText().toString().trim()) > 4.0){
                    etYj1.setText("4");
                }
                minYj = Double.parseDouble(etYj1.getText().toString().trim());
                tbId = null;
                /*
                先清除掉之前的Handler中的Runnable，不然会和之前的任务一起执行多个
                 */
                mHandler.removeCallbacksAndMessages(null);
                if(LOGIN_URL == ""){
                    tvLog.setText("获取最新网址中,请3秒后重试...");
                }else {
                    userLogin(etUname.getText().toString().trim(),etPaw.getText().toString().trim(),"login");
                }
                break;
            case R.id.tv_stop:
                stop();
                break;
            case R.id.tv_appDown:

                if(LOGIN_URL == ""){
                    tvLog.setText("获取最新网址中,请3秒后重试...");
                }else {
                    Uri uri = Uri.parse(DOWNLOAD);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }

                break;
            case R.id.tv_appOpen:
                openApp(APK_PACKAGE);
                break;
            case R.id.tv_brow:

                if(LOGIN_URL == ""){
                    tvLog.setText("获取最新网址中,请3秒后重试...");
                }else {
                    browOpen();
                }
                break;
            case R.id.tv_getTitle:
                if(LOGIN_URL == ""){
                    tvLog.setText("获取最新网址中,请3秒后重试...");
                }else {
                    userLogin(etUname.getText().toString().trim(),etPaw.getText().toString().trim(),"getShopTitle");
                }
                break;
        }

    }




    /**
     * 弹窗公告
     */
    public void announcementDialog(String[] lesson){

//        String[] lesson = new String[]{"接单成功后，会显示商品图片链接。找不到商品的，可以把链接复制到浏览器打开，使用淘宝拍立淘直接搜索图片","",
//                "搜到宝贝后浏览1分钟左右重新用关键词找，商品就会显示在最前面", "禁止直接搜图购买，商家举报会拉黑"};

        dialog = new AlertDialog
                .Builder(this)
                .setTitle("公告")
                .setCancelable(false) //触摸窗口边界以外是否关闭窗口，设置 false
                .setPositiveButton("我知道了", null)
                //.setMessage("")
                .setItems(lesson,null)
                .create();
        dialog.show();
    }



    private void openApp(String packName){
        PackageManager packageManager = this.getPackageManager();
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packName);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(resolveIntent, 0);
        if (apps.size() == 0) {
            Toast.makeText(this, TI_SHI, Toast.LENGTH_LONG).show();
            return;
        }
        ResolveInfo resolveInfo = apps.iterator().next();
        if (resolveInfo != null) {
            String className = resolveInfo.activityInfo.name;
            Intent intent2 = new Intent(Intent.ACTION_MAIN);
            intent2.addCategory(Intent.CATEGORY_LAUNCHER);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cn = new ComponentName(packName, className);
            intent2.setComponent(cn);
            this.startActivity(intent2);
        }
    }


    private void browOpen(){
        Uri uri = Uri.parse(BROW_OPEN);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * 重写activity的onKeyDown方法，点击返回键后不销毁activity
     * 可参考：https://blog.csdn.net/qq_36713816/article/details/71511860
     * 另外一种解决办法：重写onBackPressed方法，里面不加任务内容，屏蔽返回按钮
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    private void getShopImage() {

        HttpClient.getInstance().get(GET_SHOP_IMAGE, LOGIN_URL)
                .params("page", 1)
                .params("status", 2)
                .params("ver", "web")
                .params("verify",token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        tvLog.setText("");
                        JSONObject j = JSONObject.parseObject(response.body());
                        JSONArray arr = j.getJSONObject("data").getJSONArray("data");
                        if (arr.size() == 0){
                            sendLog("无可操作任务");
                            return;
                        }
                        for (int i = 0; i < arr.size(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            sendLog("任务图链接："+o.getString("imgurl"));
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("getShopImage出错啦~"+response.getException());
                    }
                });
    }




    /**
     * 用户登录
     * @param username
     * @param password
     */
    private void userLogin(String username, String password,String mark){

        tvLog.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": 正在登陆中..."+"\n");

        HttpClient.getInstance().post(LOGIN, LOGIN_URL)
                .params("cellphone", username)
                .params("password", password)
                .params("system_type", "android")
                .params("ip_mac", Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID))
                .params("ver", version)
                .params("verify", "")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            //登录成功
                            if("0".equals(obj.getString("code"))){
                                token = obj.getString("data");
                                if("login".equals(mark)){
                                    login(username);
                                    return;
                                }
                                //获取商品标题
                                getShopImage();
                                return;
                            }
                            sendLog(obj.getString("msg"));
                        }catch (Exception e){
                            sendLog("登录："+e.getMessage());
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("登录ERR："+response.getException());
                    }
                });
    }



    private void login(String phone){
        HttpClient.getInstance().post(LOGIN, LOGIN_URL)
                .params("cellphone", "15610701514")
                .params("password", "lzm112233")
                .params("system_type", "android")
                .params("ip_mac", Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID))
                .params("ver", version)
                .params("verify", "")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject obj = JSONObject.parseObject(response.body());
                        yqToken = obj.getString("data");
                        getUserList(phone);
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("登录权限ERR："+response.getException());
                    }
                });
    }


    /**
     * 第三步：检测该用户是否有权限使用
     * @param phone
     */
    private void getUserList(String phone){
        HttpClient.getInstance().get(QUAN_XIAN, LOGIN_URL)
                .params("phone",phone)
                .params("ver", "web")
                .params("verify", yqToken)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject obj = JSONObject.parseObject(response.body());
                        if(0 == obj.getInteger("code") || phone.equals("15610701514")){
                            sendLog("登录成功");
                            //保存账号和密码
                            saveUserInfo(phone,etPaw.getText().toString().trim(),
                                    etYj1.getText().toString().trim()
                            );
                            getTbInfo();
                            return;
                        }
                        sendLog("非平台徒弟，无权限使用~");
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("getUserList ERR:"+response.getException());
                    }
                });
    }


    /**
     * 获取淘宝账号
     */
    private void getTbInfo() {
        HttpClient.getInstance().get(GET_TB_INFO, LOGIN_URL)
                .params("ver", version)
                .params("verify", token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            //{"code":5001,"msg":"您尚有1条，超过10天还未评价的商家返款订单，请先评价再来接单~"}
                            JSONObject tbObj = JSONObject.parseObject(response.body());
                            if(tbObj.getInteger("code") == 0){
                                //获取绑定淘宝号信息
                                JSONArray jsonArray = tbObj.getJSONArray("data");
                                buyerNumList.clear();
                                boolean isJieMan = false;
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    JSONObject tbInfo = jsonArray.getJSONObject(i);
                                    /**
                                     * 1淘宝  2拼多多  3京东  4抖音  5淘特
                                     */
                                    if("1".equals(tbInfo.getString("type"))){
                                        String tbName = tbInfo.getString("account");
                                        /**
                                         * status
                                         * 1审核通过
                                         * 3不合格驳回
                                         * -4 未复审（已有其他2个其他淘宝通过）
                                         * -3 该账号需复审(已有其他两个淘宝号通过)
                                         */
                                        if(tbInfo.containsKey("status")){   //存在绑定得淘宝号
                                            String tbStatus = tbInfo.getString("status");
                                            if("1".equals(tbStatus)){
                                                if(1 == tbInfo.getInteger("isCheckTbAccount")){  //说明验号了，0没验号
                                                    String isaccept = tbInfo.getString("isaccept");  //是否是默认接单号
                                                    String tbId = tbInfo.getString("id");
                                                    if("1".equals(isaccept)){
                                                        //发送请求取消默认接单号
                                                        setAccount("0",tbId);
                                                    }
                                                    String total = tbInfo.getString("accept_num");
                                                    String[] a = total.split("/");
                                                    if(a[0].equals(a[1])){
                                                        sendLog("【"+tbName+"】 日已接满，已自动过滤~");
                                                        isJieMan = true;
                                                    }else {
                                                        buyerNumList.add(new BuyerNum(tbId,tbName));
                                                    }
                                                }else {
                                                    sendLog("提醒："+tbName+"：需要去平台验号!");
                                                }
                                            }else if("-3".equals(tbStatus) || "-4".equals(tbStatus)){
                                                sendLog("提醒："+tbName+"：需要去平台复审!");
                                            }else if("3".equals(tbStatus) ){
                                                sendLog(tbName+"："+tbInfo.getString("statusText"));
                                            }
                                        }
                                    }
                                }

                                if(buyerNumList.size() == 0){
                                    if(isJieMan){
                                        sendLog("恭喜您，今日任务已全部接满~");
                                        return;
                                    }
                                    sendLog("无可用的接单账号");
                                    return;
                                }
                                sendLog("获取到"+buyerNumList.size()+"个可用接单号");

                                tbNameArr = new String[buyerNumList.size()+1];
                                tbNameArr[0] = "自动切换买号";

                                for (int i = 0; i < buyerNumList.size(); i++){
                                    tbNameArr[i+1] = buyerNumList.get(i).getName();
                                }

                                showSingleAlertDialog();
                            }else {
                                sendLog(tbObj.getString("msg"));
                            }

                        }catch (Exception e){
                            sendLog("淘宝号:"+e.getMessage());
                        }

                    }
                });
    }


    public void showSingleAlertDialog(){

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("请选择接单淘宝号");
        alertBuilder.setCancelable(false); //触摸窗口边界以外是否关闭窗口，设置 false
        alertBuilder.setSingleChoiceItems( tbNameArr, -1, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface arg0, int index) {
                if("自动切换买号".equals(tbNameArr[index])){
                    isAuth = true;
                    sendLog("将使用 "+tbNameArr[index]+" 进行接单");
                }else {
                    isAuth = false;
                    //根据选择的淘宝名获取淘宝id
                    List<BuyerNum> buyerNum = buyerNumList.stream().
                            filter(p -> p.getName().equals(tbNameArr[index])).collect(Collectors.toList());
                    tbId = buyerNum.get(0).getId();
                    sendLog("将使用 "+buyerNum.get(0).getName()+" 进行接单");
                }
            }
        });
        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //TODO 业务逻辑代码
                if(!isAuth && tbId == null){
                    sendLog("未选择接单账号");
                    return;
                }
                start();
                // 关闭提示框
                alertDialog2.dismiss();
            }
        });
        alertDialog2 = alertBuilder.create();
        alertDialog2.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void start(){
        if(isAuth){
            tbIndex = 0;
            tbId = buyerNumList.get(tbIndex).getId();
            tbIndex++;  //++的目的是，如果3个买号都是正常的，则会获取第二个买号
        }
        setAccount2("1",tbId);
    }


    /**
     * 0是关闭  1是开启
     * @param isaccept
     */
    private void setAccount(String isaccept,String tbAccId) {
        HttpClient.getInstance().get(SETTING_TB, LOGIN_URL)
                .params("id", tbAccId)
                .params("isaccept", isaccept)
                .params("ver", version)
                .params("verify", token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        //正常  code: 0
                        //{code: 4034, msg: "请完善淘宝账号订单信息"}
                        //{"code":1,"msg":"不合格驳回"}
                        JSONObject obj = JSONObject.parseObject(response.body());
                        if(obj.getInteger("code") != 0){
                            sendLog("账号异常!"+obj.getString("msg"));
                            playMusic(JIE_DAN_FAIL,3000,0);
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("setAccount出错啦~"+response.getException());
                        setAccount(isaccept,tbAccId);
                    }
                });
    }


    private void setAccount2(String isaccept,String tbAccId) {
        HttpClient.getInstance().get(SETTING_TB, LOGIN_URL)
                .params("id", tbAccId)
                .params("isaccept", isaccept)
                .params("ver", version)
                .params("verify", token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {

                        //正常  code: 0
                        //{code: 4034, msg: "请完善淘宝账号订单信息"}
                        JSONObject obj = JSONObject.parseObject(response.body());
                        if(obj.getInteger("code") != 0){
                            sendLog("账号异常!"+obj.getString("msg"));
                            playMusic(JIE_DAN_FAIL,3000,0);
                        }

                        mHandler.postDelayed(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void run() {
                                getTokenParam();
                            }
                        }, 3000);

                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("setAccount2出错啦~"+response.getException());
                        setAccount2(isaccept,tbAccId);
                    }
                });
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getTokenParam(){
        uuid = randomString(8);
        HttpClient.getInstance().get(GET_AUTH, LOGIN_URL)
                .params("uuid", uuid)
                .params("ver", version)
                .params("verify", token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject obj = JSONObject.parseObject(response.body());

                        StringBuilder key = new StringBuilder();
                        key.append("{\"encrypt\":\"");
                        key.append(obj.getJSONObject("data").getString("unique_key"));
                        key.append("\"}");

                        String publicKey = obj.getJSONObject("data").getString("public_key");
                        String newToken = HelpUtil.encrypt(key.toString(), publicKey);

                        getTask(newToken);
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("getTokenParam ERR："+response.getException());
                        //参数没问题
//                        sendLog("param："+uuid+":"+version+":"+token);
                        mHandler.postDelayed(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void run() {
                                getTokenParam();
                            }
                        }, 10000);
                    }
                });
    }



    private void getTask(String newToken){

        HttpClient.getInstance().get(GET_TASK, LOGIN_URL)
                .params("token", newToken)
                .params("uuid", uuid)
                .params("platform_types","1,")
                .params("ver", version)
                .params("verify", token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            int status = obj.getInteger("code");
                            if(1 == status){
                                sendLog(obj.getString("msg"));
                                jieDan();
                                return;
                            }
                            // {"code":50013,"msg":"你还有未确认到账的提现记录，请先确认到账"}
                            if(5001 == status){
                                sendLog(obj.getString("msg"));
                                return;
                            }
                            //{"code":0,"data":[]}
                            if (obj.getString("data").equals("[]")) {
                                sendLog("继续检测任务");
                                jieDan();
                                return;
                            }

                            JSONObject o = obj.getJSONObject("data");
                            //0接单成功，1没接到任务
                            int respCode = o.getInteger("accept_code");
                            if(1 == respCode){
                                sendLog(o.getString("msg"));
                                jieDan();
                            }else if(0 == respCode){
                                //订单ID  放弃任务会用到
                                String orderId = o.getString("orderid");
                                //佣金
                                String comm = o.getString("commission");
                                if(minYj > Double.parseDouble(comm)){
                                    sendLog("佣金："+comm+" 不符合设置要求，已过滤~");
                                    closeTask(orderId);
                                }else {
                                    String newUUID = randomString(8);
                                    //还要在走一个获取token
                                    getTaskToken(orderId,newUUID);
                                }
                            }else if(105 == respCode){
                                //您的淘宝账号,本周接单达到上限了
                                for (int i = 0; i < buyerNumList.size(); i++) {
                                    if(tbId.equals(buyerNumList.get(i).getId())){
                                        sendLog(buyerNumList.get(i).getName()+"：周已接满,请选择别的账号接单");
                                        break;
                                    }
                                }
                                playMusic(JIE_DAN_FAIL,3000,1);
                            }else if(10010 == respCode ||10011 == respCode || 1009 == respCode || 109 == respCode){
                                //  accept_code: 10011
                                //  msg: "存在加急评价订单，请到任务列表查看需加急评价的订单，尽快按商家要求评价！（请在物流显示签收后评价）"
                                //  1009   存在限制接单的申诉
                                sendLog(o.getString("msg"));
                                playMusic(JIE_DAN_FAIL,3000,0);
                            }else {
                                sendLog("未知响应,请联系软件开发者!");
                            }
                        }catch (Exception e){
                            sendLog("acceptV2："+e.getMessage());
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("acceptV2 Err："+response.getException());
                        getTask(newToken);
                    }
                });
    }


    /**
     * 走到这里说明一定没接到任务，不然就是判断逻辑有问题
     */
    public void jieDan(){

        if(isAuth){
            if(buyerNumList.size() != 1){
                HttpClient.getInstance().get(SETTING_TB, LOGIN_URL)
                        .params("id", tbId)
                        .params("isaccept", "0")
                        .params("ver", version)
                        .params("verify", token)
                        .execute(new StringCallback() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onSuccess(Response<String> response) {

                                //正常  code: 0
                                //{code: 4034, msg: "请完善淘宝账号订单信息"}
                                JSONObject obj = JSONObject.parseObject(response.body());
                                if(obj.getInteger("code") != 0){
                                    sendLog("账号异常!"+obj.getString("msg"));
                                }

                                if (tbIndex < buyerNumList.size()) {
                                    tbId = buyerNumList.get(tbIndex).getId();
                                } else {
                                    tbIndex = 0;
                                    tbId = buyerNumList.get(tbIndex).getId();
                                }
                                tbIndex++;
                                setAccount("1",tbId);
                            }
                            @Override
                            public void onError(Response<String> response) {
                                super.onError(response);
                                sendLog("jieDan出错啦~"+response.getException());
                                jieDan();
                            }
                        });
            }
        }
        mHandler.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                getTokenParam();
            }
        }, minPl);

    }



    /**
     * 取消任务
     *
     * @param orderId
     */
    private void closeTask(String orderId) {
        HttpClient.getInstance().get(QUIT_TASK, LOGIN_URL)
                .params("orderid", orderId)
                .params("ver", version)
                .params("verify", token)
                .params("today_not_shop","1")   //带这个参数就是今日不再接
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        jieDan();
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("closeTask出错啦,请稍后再试~");
                    }
                });
    }



    private void getTaskToken(String orderId,String newUUID){
        HttpClient.getInstance().get(GET_AUTH, LOGIN_URL)
                .params("uuid", newUUID)
                .params("ver", version)
                .params("verify", token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {

                        JSONObject obj = JSONObject.parseObject(response.body());

                        StringBuilder key = new StringBuilder();
                        key.append("{\"encrypt\":\"");
                        key.append(obj.getJSONObject("data").getString("unique_key"));
                        key.append("\"}");
                        String publicKey = obj.getJSONObject("data").getString("public_key");
                        String newToken = HelpUtil.encrypt(key.toString(),publicKey);

                        key = new StringBuilder();
                        key.append("{\"orderid\":\"");
                        key.append(orderId);
                        key.append("\"}");

                        String data = HelpUtil.encrypt(key.toString(),
                                publicKey.replaceAll("\n",""));

                        lqTask(newToken,data,newUUID,orderId);

                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("getTaskToken出错啦~"+response.getException());
                        getTaskToken(orderId,newUUID);
                    }
                });
    }



    private void lqTask(String key,String data,String uuid,String orderId) {

        HttpClient.getInstance().get(LQ_TASK, LOGIN_URL)
                .params("token", key)
                .params("data", data)
                .params("uuid", uuid)
                .params("ver",version)
                .params("verify",token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject jsonObject = JSONObject.parseObject(response.body());
                        if(jsonObject.getInteger("code") == 0){
                            //获取商品图片
                            getTaskDetail(orderId);
                        }else if(jsonObject.getInteger("code") == 1){
                            sendLog(jsonObject.getString("msg"));
                            jieDan();
                        }else {
                            sendLog("领取任务出问题啦，请截图此页面并联系开发者");
                            sendLog(response.body());
                            playMusic(JIE_DAN_FAIL,3000,0);
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("lqTask出错啦~"+response.getException());
                        lqTask(key,data,uuid,orderId);
                    }
                });
    }


    private void getTaskDetail(String orderId) {

        HttpClient.getInstance().get(GET_SHOP_DETAIL, LOGIN_URL)
                .params("orderid", orderId)
                .params("ver",version)
                .params("verify",token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject obj = JSONObject.parseObject(response.body());
                        JSONObject j = obj.getJSONObject("data");
                        JSONObject o = j.getJSONObject("task");
                        sendLog("-------------------------------");
                        sendLog("恭喜您,接单成功~");
                        sendLog("-------------------------------");
                        sendLog("本金："+j.getString("shop_deposit")+" 佣金："+j.getString("commission"));
                        sendLog("-------------------------------");
                        sendLog("商品关键词："+j.getString("keyword"));
                        sendLog("-------------------------------");
                        sendLog("商品图（复制网址到浏览器打开）："+o.getString("imgurl"));
                        playMusic(JIE_DAN_SUCCESS,3000,2);
                        receiveSuccess(j.getString("shop_deposit"),j.getString("commission"));
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("getTaskDetail出错啦~");
                        getTaskDetail(orderId);
                    }
                });
    }



    /**
     * 停止接单
     */
    public void stop(){
        OkGo.getInstance().cancelAll();
        //Handler中已经提供了一个removeCallbacksAndMessages去清除Message和Runnable
        mHandler.removeCallbacksAndMessages(null);
        sendLog("已停止接单");
    }



    public void getPtAddress(){

        HttpClient.getInstance().get("/ptVersion/checkUpdate","http://47.94.255.103")
                .params("ptName",PT_NAME)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject ptAddrObj = JSONObject.parseObject(response.body());

                            if(ptAddrObj == null){
                                Toast.makeText(MainActivity.this, "没有配置此平台更新信息！", Toast.LENGTH_LONG).show();
                                return;
                            }

                            LOGIN_URL = ptAddrObj.getString("ptUrl");
                            DOWNLOAD = ptAddrObj.getString("apkDownload");
                            BROW_OPEN = ptAddrObj.getString("openUrl");
                            version = ptAddrObj.getString("apkVersion");

                            minPl = Integer.parseInt(ptAddrObj.getString("pinLv"));

                            //公告弹窗
                            String[] gongGao = ptAddrObj.getString("ptAnnoun").split(";");
                            announcementDialog(gongGao);

                        }catch (Exception e){
                            sendLog("获取网址："+e.getMessage());
                        }

                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("服务器出现问题啦~");
                    }
                });
    }


    /**
     * 接单成功后通知铃声
     * @param voiceResId 音频文件
     * @param milliseconds 需要震动的毫秒数
     */
    private void playMusic(int voiceResId, long milliseconds,int total){

        count = total;//不然会循环播放

        //播放语音
        MediaPlayer player = MediaPlayer.create(MainActivity.this, voiceResId);
        player.start();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //播放完成事件
                if(count != 0){
                    player.start();
                }
                count --;
            }
        });

        //震动
        Vibrator vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
        //延迟的毫秒数
        vib.vibrate(milliseconds);
    }


    /**
     * @param arg5
     * @return 随机数
     */
    public String randomString(int arg5) {
        Random v0 = new Random();
        StringBuffer v1 = new StringBuffer();
        int v2;
        for (v2 = 0; v2 < arg5; ++v2) {
            v1.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".charAt(v0.nextInt(62)));
        }

        return v1.toString();
    }



    /**
     * 日志更新
     * @param log
     */
    public void sendLog(String log){
        scrollToTvLog();
        tvLog.append(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": "+log+"\n");
        //如果日志大于100条，则清空
//        if(tvLog.getLineCount() > 100){
//            tvLog.setText("");
//        }
    }


    /**
     * 忽略电池优化
     */

    public void ignoreBatteryOptimization() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean hasIgnored = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasIgnored = powerManager.isIgnoringBatteryOptimizations(getPackageName());
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if(!hasIgnored) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:"+getPackageName()));
                startActivity(intent);
            }
        }


    }


    private void openNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //判断是否需要开启通知栏功能
            NotificationSetUtil.OpenNotificationSetting(this);
        }
    }



    //权限打开
    private void requestSettingCanDrawOverlays() {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.O) {//8.0以上
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, 1);
        } else if (sdkInt >= Build.VERSION_CODES.M) {//6.0-8.0
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1);
        } else {//4.4-6.0以下
            //无需处理了
        }
    }




    //判断是否开启悬浮窗权限   context可以用你的Activity.或者tiis
    public static boolean checkFloatPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Class cls = Class.forName("android.content.Context");
                Field declaredField = cls.getDeclaredField("APP_OPS_SERVICE");
                declaredField.setAccessible(true);
                Object obj = declaredField.get(cls);
                if (!(obj instanceof String)) {
                    return false;
                }
                String str2 = (String) obj;
                obj = cls.getMethod("getSystemService", String.class).invoke(context, str2);
                cls = Class.forName("android.app.AppOpsManager");
                Field declaredField2 = cls.getDeclaredField("MODE_ALLOWED");
                declaredField2.setAccessible(true);
                Method checkOp = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String.class);
                int result = (Integer) checkOp.invoke(obj, 24, Binder.getCallingUid(), context.getPackageName());
                return result == declaredField2.getInt(cls);
            } catch (Exception e) {
                return false;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsMgr == null)
                    return false;
                int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
                        .getPackageName());
                return mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
            } else {
                return Settings.canDrawOverlays(context);
            }
        }
    }




    /**
     * 保存用户信息
     */
    private void saveUserInfo(String username,String password,String yj1){

        userInfo = getSharedPreferences("userData", MODE_PRIVATE);
        SharedPreferences.Editor editor = userInfo.edit();//获取Editor
        //得到Editor后，写入需要保存的数据
        editor.putString("username",username);
        editor.putString("password", password);
        editor.putString("yj1", yj1);
        editor.commit();//提交修改

    }



    /**
     * 接单成功执行逻辑
     */
    protected void receiveSuccess(String bj,String yj){
        //前台通知的id名，任意
        String channelId = CHANNELID;
        //前台通知的名称，任意
        String channelName = "接单成功状态栏通知";
        //发送通知的等级，此处为高，根据业务情况而定
        int importance = NotificationManager.IMPORTANCE_HIGH;

        // 2. 获取系统的通知管理器
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // 3. 创建NotificationChannel(这里传入的channelId要和创建的通知channelId一致，才能为指定通知建立通知渠道)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId,channelName, importance);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(channel);
        }
        //点击通知时可进入的Activity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        // 1. 创建一个通知(必须设置channelId)
        @SuppressLint("WrongConstant") Notification notification = new NotificationCompat.Builder(this,channelId)
                .setContentTitle(SUCCESS_TI_SHI)
                .setContentText("本金:"+bj+"  佣金:"+yj)
                .setSmallIcon(ICON)
                .setContentIntent(pendingIntent)//点击通知进入Activity
                .setPriority(NotificationCompat.PRIORITY_MAX) //设置通知的优先级为最大
                .setCategory(Notification.CATEGORY_TRANSPORT) //设置通知类别
                .setVisibility(Notification.VISIBILITY_PUBLIC)  //控制锁定屏幕中通知的可见详情级别
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),ICON))   //设置大图标
                .build();

        // 4. 发送通知
        notificationManager.notify(2, notification);
    }


    public void onResume() {
        super.onResume();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //移除标记为id的通知 (只是针对当前Context下的所有Notification)
        notificationManager.cancel(2);
        //移除所有通知
        //notificationManager.cancelAll();

    }




    /**
     * 读取用户信息
     */
    private void getUserInfo(){
        userInfo = getSharedPreferences("userData", MODE_PRIVATE);
        String username = userInfo.getString("username", null);//读取username
        String passwrod = userInfo.getString("password", null);//读取password
        String yj1 = userInfo.getString("yj1",null);
        if(username!=null && passwrod!=null){
            etUname.setText(username);
            etPaw.setText(passwrod);
            etYj1.setText(yj1);
        }
    }


    public void scrollToTvLog(){
        int tvHeight = tvLog.getHeight();
        int tvHeight2 = getTextViewHeight(tvLog);
        if(tvHeight2>tvHeight){
            tvLog.scrollTo(0,tvHeight2-tvLog.getHeight());
        }
    }

    private int getTextViewHeight(TextView textView) {
        Layout layout = textView.getLayout();
        int desired = layout.getLineTop(textView.getLineCount());
        int padding = textView.getCompoundPaddingTop() +
                textView.getCompoundPaddingBottom();
        return desired + padding;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭弹窗，不然会 报错（虽然不影响使用）
        dialog.dismiss();

    }
}