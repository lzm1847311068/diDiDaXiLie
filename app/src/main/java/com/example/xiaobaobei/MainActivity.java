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
 * ????????????????????????
 * ????????????????????????????????????
 * ????????????????????????
 * try catch
 * ???????????????????????????????????????????????????
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText etUname,etPaw,etYj1;
    private TextView tvStart,tvStop,tvLog,tvAppDown,tvAppOpen,tvBrow,tvGetTitle,tvTitle;

    private Handler mHandler;
    private String tbId;
    private int tbIndex;
    /*
    ???????????????????????????????????????3??????
    ??????????????????count+1???
     */
    private int count;
    private SharedPreferences userInfo;
    private int minPl;
    private Double minYj;
    private String token;
    private String yqToken;  //??????token
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
     * ????????????????????????
     * 1???MainActivity
     * 2???build.gradle????????????
     * 3???AndroidMainfest.xml??????
     * 4???Update??????
     * 5???KeepAlive??????
     */

    private static final String PT_NAME = "guangMingDing";
    private static final String TITLE = "???????????????";
    private static final String SUCCESS_TI_SHI = "?????????????????????";
    private static final String TI_SHI = "?????????App?????????";
    private static final String CHANNELID = "guangmingdingSuccess";
    private static final String APK_PACKAGE = "app.guanmingding.com";
    private static int ICON = R.mipmap.guangmingding;
    private static final int JIE_DAN_SUCCESS = R.raw.gmd_success;
    private static final int JIE_DAN_FAIL = R.raw.gmd_fail;


//    private static final String PT_NAME = "diDiDa";
//    private static final String TITLE = "???????????????";
//    private static final String SUCCESS_TI_SHI = "?????????????????????";
//    private static final String TI_SHI = "?????????App?????????";
//    private static final String CHANNELID = "dididaSuccess";
//    private static final String APK_PACKAGE = "com.app.comddd";
//    private static int ICON = R.mipmap.didida;
//    private static final int JIE_DAN_SUCCESS = R.raw.ddd_success;
//    private static final int JIE_DAN_FAIL = R.raw.ddd_fail;


//    private static final String PT_NAME = "luDingJi";
//    private static final String TITLE = "???????????????";
//    private static final String SUCCESS_TI_SHI = "?????????????????????";
//    private static final String TI_SHI = "?????????App?????????";
//    private static final String CHANNELID = "ludingjiSuccess";
//    private static final String APK_PACKAGE = "app.guanmingding.com";
//    private static int ICON = R.mipmap.guangmingding;
//    private static final int JIE_DAN_SUCCESS = R.raw.ldj_success;
//    private static final int JIE_DAN_FAIL = R.raw.ldj_fail;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //???????????????
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, KeepAliveService.class);
        startService(intent);//??????????????????
        ignoreBatteryOptimization();//??????????????????
        if(!checkFloatPermission(this)){
            //??????????????????
            requestSettingCanDrawOverlays();
        }
        initView();
    }


    private void initView(){
        //????????????
        UpdateApk.update(MainActivity.this);
        //????????????????????????
        openNotification();
        //???????????????????????????
        WindowPermissionCheck.checkPermission(this);
        //??????????????????
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
        getUserInfo();//??????????????????
        //??????textView??????????????????
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvLog.setTextIsSelectable(true);
        tvStart.setOnClickListener(this);
        tvStop.setOnClickListener(this);
        tvBrow.setOnClickListener(this);
        tvAppOpen.setOnClickListener(this);
        tvAppDown.setOnClickListener(this);
        tvGetTitle = findViewById(R.id.tv_getTitle);
        tvGetTitle.setOnClickListener(this);
        tvLog.setText("app??????????????????????????????~"+"\n");
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
                ?????????????????????Handler??????Runnable????????????????????????????????????????????????
                 */
                mHandler.removeCallbacksAndMessages(null);
                if(LOGIN_URL == ""){
                    tvLog.setText("?????????????????????,???3????????????...");
                }else {
                    userLogin(etUname.getText().toString().trim(),etPaw.getText().toString().trim(),"login");
                }
                break;
            case R.id.tv_stop:
                stop();
                break;
            case R.id.tv_appDown:

                if(LOGIN_URL == ""){
                    tvLog.setText("?????????????????????,???3????????????...");
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
                    tvLog.setText("?????????????????????,???3????????????...");
                }else {
                    browOpen();
                }
                break;
            case R.id.tv_getTitle:
                if(LOGIN_URL == ""){
                    tvLog.setText("?????????????????????,???3????????????...");
                }else {
                    userLogin(etUname.getText().toString().trim(),etPaw.getText().toString().trim(),"getShopTitle");
                }
                break;
        }

    }




    /**
     * ????????????
     */
    public void announcementDialog(String[] lesson){

//        String[] lesson = new String[]{"??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????","",
//                "?????????????????????1??????????????????????????????????????????????????????????????????", "????????????????????????????????????????????????"};

        dialog = new AlertDialog
                .Builder(this)
                .setTitle("??????")
                .setCancelable(false) //??????????????????????????????????????????????????? false
                .setPositiveButton("????????????", null)
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
     * ??????activity???onKeyDown????????????????????????????????????activity
     * ????????????https://blog.csdn.net/qq_36713816/article/details/71511860
     * ?????????????????????????????????onBackPressed??????????????????????????????????????????????????????
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
                            sendLog("??????????????????");
                            return;
                        }
                        for (int i = 0; i < arr.size(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            sendLog("??????????????????"+o.getString("imgurl"));
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("getShopImage?????????~"+response.getException());
                    }
                });
    }




    /**
     * ????????????
     * @param username
     * @param password
     */
    private void userLogin(String username, String password,String mark){

        tvLog.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": ???????????????..."+"\n");

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
                            //????????????
                            if("0".equals(obj.getString("code"))){
                                token = obj.getString("data");
                                if("login".equals(mark)){
                                    login(username);
                                    return;
                                }
                                //??????????????????
                                getShopImage();
                                return;
                            }
                            sendLog(obj.getString("msg"));
                        }catch (Exception e){
                            sendLog("?????????"+e.getMessage());
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("??????ERR???"+response.getException());
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
                        sendLog("????????????ERR???"+response.getException());
                    }
                });
    }


    /**
     * ????????????????????????????????????????????????
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
                            sendLog("????????????");
                            //?????????????????????
                            saveUserInfo(phone,etPaw.getText().toString().trim(),
                                    etYj1.getText().toString().trim()
                            );
                            getTbInfo();
                            return;
                        }
                        sendLog("?????????????????????????????????~");
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("getUserList ERR:"+response.getException());
                    }
                });
    }


    /**
     * ??????????????????
     */
    private void getTbInfo() {
        HttpClient.getInstance().get(GET_TB_INFO, LOGIN_URL)
                .params("ver", version)
                .params("verify", token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            //{"code":5001,"msg":"?????????1????????????10???????????????????????????????????????????????????????????????~"}
                            JSONObject tbObj = JSONObject.parseObject(response.body());
                            if(tbObj.getInteger("code") == 0){
                                //???????????????????????????
                                JSONArray jsonArray = tbObj.getJSONArray("data");
                                buyerNumList.clear();
                                boolean isJieMan = false;
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    JSONObject tbInfo = jsonArray.getJSONObject(i);
                                    /**
                                     * 1??????  2?????????  3??????  4??????  5??????
                                     */
                                    if("1".equals(tbInfo.getString("type"))){
                                        String tbName = tbInfo.getString("account");
                                        /**
                                         * status
                                         * 1????????????
                                         * 3???????????????
                                         * -4 ????????????????????????2????????????????????????
                                         * -3 ??????????????????(?????????????????????????????????)
                                         */
                                        if(tbInfo.containsKey("status")){   //????????????????????????
                                            String tbStatus = tbInfo.getString("status");
                                            if("1".equals(tbStatus)){
                                                if(1 == tbInfo.getInteger("isCheckTbAccount")){  //??????????????????0?????????
                                                    String isaccept = tbInfo.getString("isaccept");  //????????????????????????
                                                    String tbId = tbInfo.getString("id");
                                                    if("1".equals(isaccept)){
                                                        //?????????????????????????????????
                                                        setAccount("0",tbId);
                                                    }
                                                    String total = tbInfo.getString("accept_num");
                                                    String[] a = total.split("/");
                                                    if(a[0].equals(a[1])){
                                                        sendLog("???"+tbName+"??? ??????????????????????????????~");
                                                        isJieMan = true;
                                                    }else {
                                                        buyerNumList.add(new BuyerNum(tbId,tbName));
                                                    }
                                                }else {
                                                    sendLog("?????????"+tbName+"????????????????????????!");
                                                }
                                            }else if("-3".equals(tbStatus) || "-4".equals(tbStatus)){
                                                sendLog("?????????"+tbName+"????????????????????????!");
                                            }else if("3".equals(tbStatus) ){
                                                sendLog(tbName+"???"+tbInfo.getString("statusText"));
                                            }
                                        }
                                    }
                                }

                                if(buyerNumList.size() == 0){
                                    if(isJieMan){
                                        sendLog("???????????????????????????????????????~");
                                        return;
                                    }
                                    sendLog("????????????????????????");
                                    return;
                                }
                                sendLog("?????????"+buyerNumList.size()+"??????????????????");

                                tbNameArr = new String[buyerNumList.size()+1];
                                tbNameArr[0] = "??????????????????";

                                for (int i = 0; i < buyerNumList.size(); i++){
                                    tbNameArr[i+1] = buyerNumList.get(i).getName();
                                }

                                showSingleAlertDialog();
                            }else {
                                sendLog(tbObj.getString("msg"));
                            }

                        }catch (Exception e){
                            sendLog("?????????:"+e.getMessage());
                        }

                    }
                });
    }


    public void showSingleAlertDialog(){

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("????????????????????????");
        alertBuilder.setCancelable(false); //??????????????????????????????????????????????????? false
        alertBuilder.setSingleChoiceItems( tbNameArr, -1, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface arg0, int index) {
                if("??????????????????".equals(tbNameArr[index])){
                    isAuth = true;
                    sendLog("????????? "+tbNameArr[index]+" ????????????");
                }else {
                    isAuth = false;
                    //????????????????????????????????????id
                    List<BuyerNum> buyerNum = buyerNumList.stream().
                            filter(p -> p.getName().equals(tbNameArr[index])).collect(Collectors.toList());
                    tbId = buyerNum.get(0).getId();
                    sendLog("????????? "+buyerNum.get(0).getName()+" ????????????");
                }
            }
        });
        alertBuilder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //TODO ??????????????????
                if(!isAuth && tbId == null){
                    sendLog("?????????????????????");
                    return;
                }
                start();
                // ???????????????
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
            tbIndex++;  //++?????????????????????3??????????????????????????????????????????????????????
        }
        setAccount2("1",tbId);
    }


    /**
     * 0?????????  1?????????
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
                        //??????  code: 0
                        //{code: 4034, msg: "?????????????????????????????????"}
                        //{"code":1,"msg":"???????????????"}
                        JSONObject obj = JSONObject.parseObject(response.body());
                        if(obj.getInteger("code") != 0){
                            sendLog("????????????!"+obj.getString("msg"));
                            playMusic(JIE_DAN_FAIL,3000,0);
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("setAccount?????????~"+response.getException());
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

                        //??????  code: 0
                        //{code: 4034, msg: "?????????????????????????????????"}
                        JSONObject obj = JSONObject.parseObject(response.body());
                        if(obj.getInteger("code") != 0){
                            sendLog("????????????!"+obj.getString("msg"));
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
                        sendLog("setAccount2?????????~"+response.getException());
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
                        sendLog("getTokenParam ERR???"+response.getException());
                        //???????????????
//                        sendLog("param???"+uuid+":"+version+":"+token);
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
                            // {"code":50013,"msg":"????????????????????????????????????????????????????????????"}
                            if(5001 == status){
                                sendLog(obj.getString("msg"));
                                return;
                            }
                            //{"code":0,"data":[]}
                            if (obj.getString("data").equals("[]")) {
                                sendLog("??????????????????");
                                jieDan();
                                return;
                            }

                            JSONObject o = obj.getJSONObject("data");
                            //0???????????????1???????????????
                            int respCode = o.getInteger("accept_code");
                            if(1 == respCode){
                                sendLog(o.getString("msg"));
                                jieDan();
                            }else if(0 == respCode){
                                //??????ID  ?????????????????????
                                String orderId = o.getString("orderid");
                                //??????
                                String comm = o.getString("commission");
                                if(minYj > Double.parseDouble(comm)){
                                    sendLog("?????????"+comm+" ?????????????????????????????????~");
                                    closeTask(orderId);
                                }else {
                                    String newUUID = randomString(8);
                                    //????????????????????????token
                                    getTaskToken(orderId,newUUID);
                                }
                            }else if(105 == respCode){
                                //??????????????????,???????????????????????????
                                for (int i = 0; i < buyerNumList.size(); i++) {
                                    if(tbId.equals(buyerNumList.get(i).getId())){
                                        sendLog(buyerNumList.get(i).getName()+"???????????????,???????????????????????????");
                                        break;
                                    }
                                }
                                playMusic(JIE_DAN_FAIL,3000,1);
                            }else if(10010 == respCode ||10011 == respCode || 1009 == respCode || 109 == respCode){
                                //  accept_code: 10011
                                //  msg: "???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????"
                                //  1009   ???????????????????????????
                                sendLog(o.getString("msg"));
                                playMusic(JIE_DAN_FAIL,3000,0);
                            }else {
                                sendLog("????????????,????????????????????????!");
                            }
                        }catch (Exception e){
                            sendLog("acceptV2???"+e.getMessage());
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("acceptV2 Err???"+response.getException());
                        getTask(newToken);
                    }
                });
    }


    /**
     * ???????????????????????????????????????????????????????????????????????????
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

                                //??????  code: 0
                                //{code: 4034, msg: "?????????????????????????????????"}
                                JSONObject obj = JSONObject.parseObject(response.body());
                                if(obj.getInteger("code") != 0){
                                    sendLog("????????????!"+obj.getString("msg"));
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
                                sendLog("jieDan?????????~"+response.getException());
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
     * ????????????
     *
     * @param orderId
     */
    private void closeTask(String orderId) {
        HttpClient.getInstance().get(QUIT_TASK, LOGIN_URL)
                .params("orderid", orderId)
                .params("ver", version)
                .params("verify", token)
                .params("today_not_shop","1")   //????????????????????????????????????
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        jieDan();
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("closeTask?????????,???????????????~");
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
                        sendLog("getTaskToken?????????~"+response.getException());
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
                            //??????????????????
                            getTaskDetail(orderId);
                        }else if(jsonObject.getInteger("code") == 1){
                            sendLog(jsonObject.getString("msg"));
                            jieDan();
                        }else {
                            sendLog("???????????????????????????????????????????????????????????????");
                            sendLog(response.body());
                            playMusic(JIE_DAN_FAIL,3000,0);
                        }
                    }
                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("lqTask?????????~"+response.getException());
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
                        sendLog("?????????,????????????~");
                        sendLog("-------------------------------");
                        sendLog("?????????"+j.getString("shop_deposit")+" ?????????"+j.getString("commission"));
                        sendLog("-------------------------------");
                        sendLog("??????????????????"+j.getString("keyword"));
                        sendLog("-------------------------------");
                        sendLog("????????????????????????????????????????????????"+o.getString("imgurl"));
                        playMusic(JIE_DAN_SUCCESS,3000,2);
                        receiveSuccess(j.getString("shop_deposit"),j.getString("commission"));
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("getTaskDetail?????????~");
                        getTaskDetail(orderId);
                    }
                });
    }



    /**
     * ????????????
     */
    public void stop(){
        OkGo.getInstance().cancelAll();
        //Handler????????????????????????removeCallbacksAndMessages?????????Message???Runnable
        mHandler.removeCallbacksAndMessages(null);
        sendLog("???????????????");
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
                                Toast.makeText(MainActivity.this, "????????????????????????????????????", Toast.LENGTH_LONG).show();
                                return;
                            }

                            LOGIN_URL = ptAddrObj.getString("ptUrl");
                            DOWNLOAD = ptAddrObj.getString("apkDownload");
                            BROW_OPEN = ptAddrObj.getString("openUrl");
                            version = ptAddrObj.getString("apkVersion");

                            minPl = Integer.parseInt(ptAddrObj.getString("pinLv"));

                            //????????????
                            String[] gongGao = ptAddrObj.getString("ptAnnoun").split(";");
                            announcementDialog(gongGao);

                        }catch (Exception e){
                            sendLog("???????????????"+e.getMessage());
                        }

                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("????????????????????????~");
                    }
                });
    }


    /**
     * ???????????????????????????
     * @param voiceResId ????????????
     * @param milliseconds ????????????????????????
     */
    private void playMusic(int voiceResId, long milliseconds,int total){

        count = total;//?????????????????????

        //????????????
        MediaPlayer player = MediaPlayer.create(MainActivity.this, voiceResId);
        player.start();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //??????????????????
                if(count != 0){
                    player.start();
                }
                count --;
            }
        });

        //??????
        Vibrator vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
        //??????????????????
        vib.vibrate(milliseconds);
    }


    /**
     * @param arg5
     * @return ?????????
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
     * ????????????
     * @param log
     */
    public void sendLog(String log){
        scrollToTvLog();
        tvLog.append(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": "+log+"\n");
        //??????????????????100???????????????
//        if(tvLog.getLineCount() > 100){
//            tvLog.setText("");
//        }
    }


    /**
     * ??????????????????
     */

    public void ignoreBatteryOptimization() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean hasIgnored = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasIgnored = powerManager.isIgnoringBatteryOptimizations(getPackageName());
            //  ????????????APP??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if(!hasIgnored) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:"+getPackageName()));
                startActivity(intent);
            }
        }


    }


    private void openNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //???????????????????????????????????????
            NotificationSetUtil.OpenNotificationSetting(this);
        }
    }



    //????????????
    private void requestSettingCanDrawOverlays() {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.O) {//8.0??????
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, 1);
        } else if (sdkInt >= Build.VERSION_CODES.M) {//6.0-8.0
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1);
        } else {//4.4-6.0??????
            //???????????????
        }
    }




    //?????????????????????????????????   context???????????????Activity.??????tiis
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
     * ??????????????????
     */
    private void saveUserInfo(String username,String password,String yj1){

        userInfo = getSharedPreferences("userData", MODE_PRIVATE);
        SharedPreferences.Editor editor = userInfo.edit();//??????Editor
        //??????Editor?????????????????????????????????
        editor.putString("username",username);
        editor.putString("password", password);
        editor.putString("yj1", yj1);
        editor.commit();//????????????

    }



    /**
     * ????????????????????????
     */
    protected void receiveSuccess(String bj,String yj){
        //???????????????id????????????
        String channelId = CHANNELID;
        //??????????????????????????????
        String channelName = "???????????????????????????";
        //???????????????????????????????????????????????????????????????
        int importance = NotificationManager.IMPORTANCE_HIGH;

        // 2. ??????????????????????????????
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // 3. ??????NotificationChannel(???????????????channelId?????????????????????channelId????????????????????????????????????????????????)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId,channelName, importance);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(channel);
        }
        //???????????????????????????Activity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        // 1. ??????????????????(????????????channelId)
        @SuppressLint("WrongConstant") Notification notification = new NotificationCompat.Builder(this,channelId)
                .setContentTitle(SUCCESS_TI_SHI)
                .setContentText("??????:"+bj+"  ??????:"+yj)
                .setSmallIcon(ICON)
                .setContentIntent(pendingIntent)//??????????????????Activity
                .setPriority(NotificationCompat.PRIORITY_MAX) //?????????????????????????????????
                .setCategory(Notification.CATEGORY_TRANSPORT) //??????????????????
                .setVisibility(Notification.VISIBILITY_PUBLIC)  //????????????????????????????????????????????????
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),ICON))   //???????????????
                .build();

        // 4. ????????????
        notificationManager.notify(2, notification);
    }


    public void onResume() {
        super.onResume();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //???????????????id????????? (??????????????????Context????????????Notification)
        notificationManager.cancel(2);
        //??????????????????
        //notificationManager.cancelAll();

    }




    /**
     * ??????????????????
     */
    private void getUserInfo(){
        userInfo = getSharedPreferences("userData", MODE_PRIVATE);
        String username = userInfo.getString("username", null);//??????username
        String passwrod = userInfo.getString("password", null);//??????password
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
        //???????????????????????? ?????????????????????????????????
        dialog.dismiss();

    }
}