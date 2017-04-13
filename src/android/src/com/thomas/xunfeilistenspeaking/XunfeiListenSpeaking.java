package com.thomas.xunfeilistenspeaking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.iflytek.cloud.*;
import com.iflytek.sunflower.FlowerCollector;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Thomas.Wang on 17/2/9.
 */
public class XunfeiListenSpeaking extends CordovaPlugin{
    private static String TAG = XunfeiListenSpeaking.class.getSimpleName();
    private Context context;
    private CallbackContext callbackContext;
    private Toast mToast;
    private Handler mHandler = new Handler();

    private  SpeechSynthesizer mTts;

    // 语音听写对象
    private SpeechRecognizer mIat;

    private SharedPreferences mSharedPreferences;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        context = cordova.getActivity();
//        SpeechUtility.createUtility(context, SpeechConstant.APPID +"=584e7225");
        SpeechUtility.createUtility(context, SpeechConstant.APPID +"="+context.getString(getId("app_id","string")));
    }

    private int getId(String idName,String type){
        return context.getResources().getIdentifier(idName, type,context.getPackageName());
    }
    private static final int DIALOG_ACTIVIT_CODE = 0;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        this.callbackContext = callbackContext;
        //开始听写
        if (action.equals("startListen")){
//            boolean isShowDialog = args.getBoolean(0);

//            String punc = args.getBoolean(1)?"1":"0";
            boolean isShowDialog ;
            try {
                isShowDialog = args.getBoolean(0);
            }catch (Exception e){
                isShowDialog = true;
            }
            String punc;
            try{
                punc = args.getBoolean(1)?"1":"0";
            }catch (Exception e){
                punc = "1";
            }
            if (isShowDialog){
                Intent intent = new Intent();
                intent.setClass(context, XunfeiDialogActivity.class);
                intent.putExtra("isShowDialog",isShowDialog);
                intent.putExtra("punc",punc);
                cordova.startActivityForResult( this,intent, DIALOG_ACTIVIT_CODE);
            }else {
                startListenWidthNotDialog(punc);
            }


            return true;
        }

        //停止听写
        if (action.equals("stopListen")) {
            stopListen();
            return true;
        }


        //开始听写
        if (action.equals("startSpeak")){
            mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
            String speakMessage = args.getString(0).trim();
            startSpeak(speakMessage);
            return true;
        }
        //停止说话
        if (action.equals("stopSpeak")){
            stopSpeak();
            return true;
        }

        //暂停
        if (action.equals("pauseSpeaking")){
            pauseSpeaking();
            return true;
        }
        //继续
        if (action.equals("resumeSpeaking")){
            resumeSpeaking();
            return true;
        }

        return false;
    }


    private void stopListen(){
        if (mIat!=null&&mIat.isListening()) {
            mIat.stopListening();
        }
    }
    int ret = 0; // 函数调用返回值
    private void startListenWidthNotDialog(String punc){
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);
        mSharedPreferences = context.getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);
        if (mIat.isListening()) {
            mIat.stopListening();
        }
        // 移动数据分析，收集开始听写事件
        FlowerCollector.onEvent(context, "iat_recognize");
        mIatResults.clear();
        // 设置参数
        setParam(punc);


        // 不显示听写对话框
        ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
//                showTip("听写失败,错误码：" + ret);
//            finishThisActivity(RESULT_CANCELED,"听写失败,错误码：" + ret);
            callbackContext.error("听写失败,错误码：" + ret);

        } else {
//                showTip(this.getString(getId("text_begin","string")));
        }



    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
//            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
//            showTip(error.getPlainDescription(true));
//            finishThisActivity(RESULT_CANCELED,error.getPlainDescription(true));
            callbackContext.error(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
//            showTip("结束说话");

        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());

            printResult(results,isLast);

        }


        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            showTip("当前正在说话，音量大小：" + volume);
//            showTip("当前正在说话...");
//            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
//            	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
//            		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
//            		Log.d(TAG, "session id =" + sid);
//            	}

        }
    };

    private void printResult(RecognizerResult results, boolean isLast) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        Log.d(TAG, "音频中文：" + resultBuffer.toString());
//        mResultText.setText(resultBuffer.toString());
//        mResultText.setSelection(mResultText.length());
//        Intent resultIntent = new Intent();
//        Bundle bundle = new Bundle();
//        bundle.putString("result", resultBuffer.toString());
//        resultIntent.putExtras(bundle);
//        this.setResult(RESULT_OK, resultIntent);
//        finish();
        if (isLast)
            callbackContext.success(resultBuffer.toString());
    }

    /**
     * 参数设置
     *
     * @param
     * @return
     */
    public void setParam(String punc) {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = mSharedPreferences.getString("iat_language_preference",
                "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
//        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
//         mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", punc));
        mIat.setParameter(SpeechConstant.ASR_PTT, punc);

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }


    private void resumeSpeaking(){
        mTts.resumeSpeaking();
    }
    private void pauseSpeaking(){
        mTts.pauseSpeaking();
    }

    private void stopSpeak(){
        if(mTts!=null&&mTts.isSpeaking()){
            mTts.stopSpeaking();
        }
    }

    private void startSpeak(String speakMessage) {
        setSpeakParameter();
        if (mTts.isSpeaking()){
            mTts.stopSpeaking();
        }
        mTts.startSpeaking(speakMessage, mSynListener);

    }

    private void setSpeakParameter(){
        if (mTts==null){
            //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
            mTts = SpeechSynthesizer.createSynthesizer(context, null);
            //2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
            mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
            mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
            mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
            //设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
            //保存在SD卡需要在AndroidManifest.xml添加写SD卡权限
            //如果不需要保存合成音频，注释该行代码
            mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
            //3.开始合成
            // mTts.startSpeaking("科大讯飞，让世界聆听我们的声音", mSynListener);
        }
    }
    //合成监听器
    private SynthesizerListener mSynListener = new SynthesizerListener(){
        //会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {
            if (error == null) {
//                showTip("播放完成");
                callbackContext.success("播放完成");
            } else if (error != null) {
                showTip(error.getPlainDescription(true));
                callbackContext.error(error.getPlainDescription(true));
            }
        }
        //缓冲进度回调
        //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {}
        //开始播放
        public void onSpeakBegin() {

//            showTip("开始播放");
        }
        //暂停播放
        public void onSpeakPaused() {}
        //播放进度回调
        //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {}
        //恢复播放回调接口
        public void onSpeakResumed() {}
        //会话事件回调接口
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {}
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DIALOG_ACTIVIT_CODE:
                if(resultCode == Activity.RESULT_OK){
                    Bundle bundle = data.getExtras();
                    callbackContext.success(bundle.getString("result"));
                }else if (resultCode == Activity.RESULT_CANCELED){
                    Bundle bundle = data.getExtras();
                    callbackContext.error(bundle.getString("result"));
                }
                break;
        }
    }
    private void showTip(final String str) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mToast.setText(str);
                mToast.show();
            }
        });
    }


}
