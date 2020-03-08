package com.example.dialoguebot;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.dialoguebot.DialogueContent.DialogueItem;
import com.example.dialoguebot.player.DingdangMediaPlayer;
import com.example.dialoguebot.player.TestMediaPlayer;
import com.example.dialoguebot.record.VoiceRecord;
import com.example.dialoguebot.settings.SettingsManager;
import com.example.dialoguebot.util.PackageUtil;
import com.tencent.ai.tvs.api.DialogManager;
import com.tencent.ai.tvs.api.TVSApi;
import com.tencent.ai.tvs.capability.userinterface.data.ASRTextMessageBody;
import com.tencent.ai.tvs.capability.userinterface.data.UIDataMessageBody;
import com.tencent.ai.tvs.tvsinterface.DialogOptions;
import com.tencent.ai.tvs.tvsinterface.IAuthInfoListener;
import com.tencent.ai.tvs.tvsinterface.IMediaPlayer;
import com.tencent.ai.tvs.tvsinterface.IRecognizeListener;
import com.tencent.ai.tvs.tvsinterface.ITTSListener;
import com.tencent.ai.tvs.tvsinterface.IUIDataListener;
import com.tencent.ai.tvs.tvsinterface.ResultCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class DailogueFragment extends Fragment implements View.OnClickListener{

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private Handler mHandler;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DailogueFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DailogueFragment newInstance(int columnCount) {
        DailogueFragment fragment = new DailogueFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        mAt_Main = (MainActivity)getActivity();
        mBtn_Talk = (Button) mAt_Main.findViewById(R.id.btn_talk);
        if (mBtn_Talk != null){
            mBtn_Talk.setOnClickListener(this);
        }
        else {
            Log.d(TAG, "mBtn_Talk == null");
        }

        mDailogueView = (RecyclerView)mAt_Main.findViewById(R.id.Dailogue_list);
        if (mDailogueView != null){
            mAdapter = new MyDailogueRecyclerViewAdapter(mList, mListener);
            mDailogueView.setAdapter(mAdapter);
        }
        else {
            Log.d(TAG, "mDailogueView == null");
        }

        init();
    }
    public MyDailogueRecyclerViewAdapter mAdapter;
    public static List<DialogueItem> mList = new ArrayList<DialogueItem>();

    /**
     * 初始化流程
     */
    private void init() {

        // 初始化SDK
        initSDK();
    }

    /**
     * 处理setClientId返回码
     *
     * @param ret
     */
    private void handleSetClientIdRet(int ret) {
        Log.i(TAG, "handleSetClientIdRet : " + ret);
        switch (ret) {
            case ResultCode.RESULT_AUTH_NOT_INITED:
                printLog("错误：AuthManager还未初始化");
                break;
            case ResultCode.RESULT_AUTH_HAD_CLIENTID_ALREADY:
                printLog("错误：已存在授权信息，请先调用clear后，再次授权");
                break;
            case ResultCode.RESULT_AUTH_SET_CLIENTID_EMPTY:
                printLog("错误：传入的clientId为空");
                break;
            case ResultCode.RESULT_AUTH_PARSE_AUTHCODE_FAILED:
                printLog("错误：AuthRespInfo的json解析失败");
                break;
            case ResultCode.RESULT_AUTH_SESSIONID_INVALID:
                printLog("错误：AuthRespInfo中的sessionId已失效，请重新生成");
                break;
            default:
                printLog("授权错误码未知：" + ret);
                break;
        }
    }

    private void printLog(String st_Log){
        Log.d(TAG, st_Log);
    }


    public final String APP_KEY = "6f3dab205d2f11ea8e313b96031bfc48";
    public final String ACCESS_TOKEN = "8e37009d50e9498981bf792453565afe";
    //设置设备的唯一标识码，接入时需换成真实的
    public final String DSN = "braind_dsn";
    /**
     * 录音实例
     * 注：目前要保证是同一个，否则会出现资源抢占问题
     */
    private VoiceRecord mVoiceRecord;
    /**
     * 初始化SDK
     */
    private void initSDK() {

        // 云端注册成功，开始初始化SDK
        TVSApi tvsApi = TVSApi.getInstance();

        // 根据demo设置中的值，初始化SDK的后台环境
        SettingsManager.getInstance().initEnvValue();

        // TODO 这里可以替换成自己实现的媒体播放器
        TestMediaPlayer mediaPlayer = new TestMediaPlayer(mAt_Main.getApplicationContext());
        // 语音发起的媒体的播放器接口实现，需要正确处理接口，并回调播放器状态
        IMediaPlayer dingdangMediaPlayer = new DingdangMediaPlayer(mediaPlayer);
        //mMediaPlayerView.setMediaPlayer(mediaPlayer);

        // 设置设备版本号（VN），必须是 x.x.x.x 格式，每一段必须为数字，且新版本的版本号必须比旧版本大
        // 该参数会与设备开放平台的应用版本进行匹配，用于后台确定下发哪些技能
        // 当需要区分终端版本下发不同技能的时候生效，映射规则为设备开放平台上配置的比VN小的最大的应用版本号
        Map<String, String> quaMap = new HashMap<>();
        quaMap.put(TVSApi.QUA_KEY_VN, "0.0.0.0");
        // 上报应用版本号，辅助查问题
        quaMap.put(TVSApi.QUA_KEY_APPVN, PackageUtil.getVersionName(mAt_Main.getApplicationContext(), mAt_Main.getPackageName()));

        // 初始化TVSApi
        DefaultPluginProvider defaultPluginProvider = new DefaultPluginProvider(mAt_Main.getApplicationContext(), dingdangMediaPlayer);
        int result = tvsApi.init(defaultPluginProvider, mAt_Main.getApplicationContext(), APP_KEY, ACCESS_TOKEN,
                DSN, mAuthInfoListener, quaMap);
        if (result == 0) {
            /*mAuthView.printLog("初始化,DSN:" + DSN + ", result : " + result
                    + ", APP_KEY : " + APP_KEY + ", ACCESS_TOKEN : " + ACCESS_TOKEN);*/

            // 初始化语音识别
            DialogManager dialogManager = tvsApi.getDialogManager();
            if (null != dialogManager) {
                // 初始化语音识别模块，ret为0表示初始化成功，非0详见ResultCode（一般为so、模型加载失败）
                int ret = dialogManager.init(null, false);
                if (ret != 0) {
                    Log.e(TAG, "initDialogManager failed ：" + ret);
                    return;
                }
                // 设置录音的实现
                mVoiceRecord = new VoiceRecord();
                dialogManager.setOuterAudioRecorder(mVoiceRecord);
                // 先静音，等设备授权成功后再启动收音
                dialogManager.disableVoiceCapture();

                // 设置语音识别状态变化的listener
                dialogManager.addRecognizeListener(mRecognizeListener);
                // 设置语音合成状态变化的listener
                dialogManager.addTTSListener(mTTSListener);
                // 设置UI数据的listener
                dialogManager.addUIDataListener(mUIDataListener);

                // 注：设备授权成功后()，需启动dialogManager收音。见onInitTokenSucceed，enableVoiceCapture

            }
            // 注册通用媒体指令的回调（收藏状态变化、播放模式变化、下载）
            //mMediaPlayerView.registerMediaListener();
            // 实现通话接口
            //mPhoneCallCallback = new PhoneCallCallback(MainActivity.this);
            //tvsApi.addCommunicationListener(mPhoneCallCallback);
            // 实现自定义技能接口
            //mCustomSkillCallback = new CustomSkillCallback();
            //tvsApi.addCustomSkillHandler(mCustomSkillCallback);
            // 实现自定义数据
            //mCustomDataCallback = new CustomDataCallback();
            //tvsApi.addCustomDataHandler(mCustomDataCallback);

            // TODO 需要启动远程服务，控制第三方App，再开启
            /**
             * 如果要控制腾讯视频等媒体类的App，需要上报当前活跃的媒体App包名，用于区分播放控制的响应
             * ActiveMediaAppDemo是一个示例，通过媒体类应用互斥的方式，接入方也可以修改成自己的实现
             */
//            ActiveMediaAppDemo.enableAccessibility();
            // 如果要控制腾讯视频等第三方App，需要开启SDK的远程服务，不需要可以删除
//            TVSApi.getInstance().openRemoteService(new RemoteServiceCallbackImpl());

            // 初始化demo的设置
            SettingsManager.getInstance().initSettingsValue();
            Log.d(TAG, "initSDK end Env is ：" + tvsApi.getEnv()
                    + " sandbox is: " + tvsApi.getSandBoxEnable());
        } else if (result == ResultCode.RESULT_SDK_INIT_PARAM_EMPTY) {
            Log.e(TAG, "initSDK error ：必填参数未填");
            return;
        } else if (result == ResultCode.RESULT_SDK_INIT_ALREADY) {
            Log.e(TAG, "initSDK error ：重复初始化");
            return;
        }
    }

    IAuthInfoListener mAuthInfoListener = new IAuthInfoListener() {
        @Override
        public void onMissingClientId(boolean isInit) {
            Log.i(TAG, "onMissingClientId isInit : " + isInit);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    /*mAuthView.printLog("未授权状态，请输入ClientId授权帐号，或访客登录");
                    mAuthView.show();
                    mAuthView.showAuthProgress(false);*/

                    // 直接走访客账号
                    int ret = TVSApi.getInstance().getAuthManager().setGuestClientId();
                    if (ret != ResultCode.RESULT_OK){
                        handleSetClientIdRet(ret);
                    }

                    DialogManager dialogManager = TVSApi.getInstance().getDialogManager();
                    if (null != dialogManager) {
                        dialogManager.disableVoiceCapture();
                    }
                }
            });
        }

        @Override
        public void onInitTokenSucceed(String grantType) {
            Log.i(TAG, "onInitTokenSucceed ：" + grantType);

            // 设备授权成功，启动收音！
            DialogManager dialogManager = TVSApi.getInstance().getDialogManager();
            if (null != dialogManager) {
                dialogManager.enableVoiceCapture();
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    /*mAuthView.hide();
                    mLogTv.setText("当前环境：" + TVSApi.getInstance().getEnv()
                            + ", 是否开启沙箱：" + TVSApi.getInstance().getSandBoxEnable());*/
                }
            });
        }

        @Override
        public void onInitTokenFailed(String grantType, int errorCode, String errorMsg) {
            Log.e(TAG, "onInitTokenFailed ：" + grantType + ", errorCode : " + errorMsg + ", errorMsg : " + errorMsg);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                   /* mAuthView.printLog("账号初始化失败，errorCode：" + errorCode + "，errorMsg：" + errorMsg + "\n 请重新输入");
                    // TODO 云端注册失败，语音功能无法使用，需要重试
                    mAuthView.showAuthProgress(false);*/
                }
            });
        }
    };


    private int mLastPosition = 0;
    private boolean mB_firstWord = true;
    // 语音识别流程的状态回调
    IRecognizeListener mRecognizeListener = new IRecognizeListener() {

        @Override
        public void onRecognizationStart(int recoType, String dialogRequestId, String tag) {
            Log.i(TAG, "onRecognizationStart : " + dialogRequestId + ", tag : " + tag );
            //sample在启动语音识别的时候，停止正在响铃的闹钟，接入方可根据闹钟UI，自行调用停止闹钟。
            /*if (AlertControlManager.getInstance() != null
                    && AlertControlManager.getInstance().isAlertPlaying()) {
                Log.i(TAG, "stopPlayingAlert ");
                AlertControlManager.getInstance().stopPlayingAlert();
            }*/
            mB_firstWord = true;
            //printLog("开始录音");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBtn_Talk.setText(R.string.st_talking);
                }
            });
        }

        @Override
        public void onStartRecord(int recoType, String dialogRequestId, String tag) {
            Log.i(TAG, "onStartRecord : " + dialogRequestId + " tag = " + tag);

        }

        @Override
        public void onSpeechStart(int recoType, String dialogRequestId, String tag) {
            Log.i(TAG, "onSpeechStart : " + dialogRequestId + " tag = " + tag);

            // 没有本地VAD，这个回调不会触发
            //printLog("检测到说话开始");
        }

        @Override
        public void onSpeechEnd(int recoType, String dialogRequestId, String tag) {
            Log.i(TAG, "onSpeechEnd : " + dialogRequestId + " tag = " + tag);

            //printLog("检测到说话结束");

        }

        @Override
        public void onFinishRecord(int recoType, String dialogRequestId, String tag) {
            Log.i(TAG, "onFinishRecord : " + dialogRequestId + " tag = " + tag);

            //printLog("结束录音");
        }

        @Override
        public void onGetASRText(String dialogRequestId, String asrText, boolean isFinal, String status,
                                 ASRTextMessageBody.UserInfo userInfo, List<ASRTextMessageBody.AsrClassifierInfo> asrClassifierInfos) {
            Log.i(TAG, "onGetASRText : " + dialogRequestId + ", asrText : " + asrText
                    + ", isFinal : " + isFinal + ", status : " + status);

            //printLog("识别文字结果：" + asrText + (isFinal ? ", 识别完毕" : ""));
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (isFinal){
                        mBtn_Talk.setText(R.string.st_thinking);
                    }
                    if (mB_firstWord && !asrText.isEmpty()){
                        mLastPosition = mAdapter.add(asrText, MyDailogueRecyclerViewAdapter.TYPE_USER_TALK);
                        mB_firstWord = false;
                    }
                    else if (!asrText.isEmpty()){
                        mAdapter.update(asrText, mLastPosition);
                    }
                    mDailogueView.scrollToPosition(mLastPosition);
                }
            });
        }

        @Override
        public void onGetResponse(int recoType, String dialogRequestId, String tag) {
            Log.i(TAG, "onGetResponse : " + dialogRequestId + " tag = " + tag);

            //printLog("收到服务器数据");
        }

        @Override
        public void onRecognizationFinished(int recoType, String dialogRequestId, String tag) {
            Log.i(TAG, "onRecognizationFinished : " + dialogRequestId + " tag = " + tag);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBtn_Talk.setText(R.string.press_to_talk);
                }
            });
        }

        @Override
        public void onVolume(int volume) {
            //Log.i(TAG, "onVolume : " + volume);
        }

        @Override
        public void onRecognizeError(int errorCode, String errorMessage, int recoType, String dialogRequestId, String tag) {
            Log.i(TAG, "onRecognizeError : " + dialogRequestId + ", errorCode : " + errorCode + ", msg : " + errorMessage + " tag = " + tag);

            //printLog("出现错误，错误码：" + errorCode + ", errorMessage : " + errorMessage);
        }

        @Override
        public void onRecognizeCancel(int recoType, String dialogRequestId, String tag) {
            Log.i(TAG, "onRecognizeCancel : " + dialogRequestId + " tag = " + tag);
        }

        @Override
        public void onSaveRecord(String tag, String recordPath) {
            Log.i(TAG, "onSaveRecord : " + tag + ", " + recordPath);
        }
    };

    // TTS播报的回调
    ITTSListener mTTSListener = new ITTSListener() {
        @Override
        public void onTTSStarted(String dialogRequestId, String tag) {
            Log.i(TAG, "onTTSStarted : " + dialogRequestId + " tag = " + tag);

            //printLog("语音播报开始");

            //printLog("开始录音");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBtn_Talk.setText(R.string.st_speeching);
                }
            });
        }

        @Override
        public void onTTSFinished(String dialogRequestId, boolean complete, String tag) {
            Log.i(TAG, "onTTSFinished : " + dialogRequestId + " tag = " + tag);

            //printLog("语音播报结束");
            //printLog("开始录音");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBtn_Talk.setText(R.string.press_to_talk);
                }
            });
        }
    };

    // UI模版的回调
    private String mSt_ReceiveText = "";
    IUIDataListener mUIDataListener = new IUIDataListener()
    {
        @Override
        public void onGetUIData(String tag, String dialogRequestId, UIDataMessageBody uiDataPayload, boolean reportEnd) {
            Log.i(TAG, "onGetUIData : " + dialogRequestId + " tag = " + tag + ", UI : " + uiDataPayload.jsonUI.data);
            mSt_ReceiveText = "";
            try {
                JSONObject jo = new JSONObject(uiDataPayload.jsonUI.data);
                JSONArray ja = jo.getJSONArray("listItems");
                Log.d(TAG, "ListItems length = " + ja.length());
                jo = (JSONObject) ja.get(0);
                mSt_ReceiveText = jo.getString("textContent");
                Log.d(TAG, "textContent = "+mSt_ReceiveText);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!mSt_ReceiveText.isEmpty()){
                        mLastPosition = mAdapter.add(mSt_ReceiveText, MyDailogueRecyclerViewAdapter.TYPE_DINGDANG_RECEIVE);
                        mDailogueView.scrollToPosition(mLastPosition);
                    }
                }
            });
        }
    };


    private Button mBtn_Talk;
    private Activity mAt_Main;
    private RecyclerView mDailogueView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dailogue_list, container, false);
        if (view == null){
            Log.d(TAG, "onCreateView return null");
        }

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            recyclerView.setAdapter(new MyDailogueRecyclerViewAdapter(DialogueContent.ITEMS, mListener));
        }
        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private final String TAG = "braind_DailogueFragment";
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_talk){
            Log.d(TAG, "start recognize");
            startRecognize();
        }
    }

    private void startRecognize() {
        DialogManager dialogManager = TVSApi.getInstance().getDialogManager();
        //如无会话配置项的请求，使用下面方法即可
        //dialogManager.startRecognize(IRecognizeListener.RECO_TYPE_MANUAL);

        //带有会话配置项的请求
        DialogOptions dialogOptions = new DialogOptions();
        dialogOptions.tag = "demo_test_user_tag";
        dialogManager.startRecognize(IRecognizeListener.RECO_TYPE_MANUAL, dialogOptions,null);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DialogueItem item);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TVSApi tvsApi = TVSApi.getInstance();
        DialogManager dialogManager = tvsApi.getDialogManager();
        if (null != dialogManager) {
            dialogManager.setOuterAudioRecorder(null);
            dialogManager.removeRecognizeListener(mRecognizeListener);
            dialogManager.removeTTSListener(mTTSListener);
            dialogManager.removeUIDataListener(mUIDataListener);
        }
        tvsApi.release();
    }
}
