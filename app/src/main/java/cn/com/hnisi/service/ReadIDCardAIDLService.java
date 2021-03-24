package cn.com.hnisi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hitown.hitownsdk.IdCardHitown;
import com.jsecode.entity.IdCardInfo;
import com.jsecode.interfaces.AbstractIdCardReader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import cn.com.hnisi.readIDCardAIDL.CallBackIdCard;
import cn.com.hnisi.readIDCardAIDL.ReadIDCardAIDL;
//import com.konka.sdt.IDCard;
//import com.konka.sdt.SerialPort;
//import com.synjones.bluetooth.DecodeWlt;

public class ReadIDCardAIDLService extends Service {
    public final static String TAG = "ReadIDCardAIDLService";
    private static Context ctx;
    protected static ReadIDCardAIDLService cxt;

    static CallBackIdCard callback;
    private static IdCardHitown hitownIDcard = null;
    //二代证模块是否打开
    private boolean isModelOpened = false;


    private static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d("IdcardScanActivity", "handleMsg:msg.what = " + msg.what);
            switch (msg.what) {
                /**扫描成功*/
                case AbstractIdCardReader.STATUS_OK:
                    Bundle b = msg.getData();
                    IdCardInfo idCardInfo = (IdCardInfo) b.getSerializable(AbstractIdCardReader.EXTRA_DATA);

                    updateInfo(idCardInfo);

//				mResult.setText("识别率:" + ++countSuccess * 100
//						/ (countSuccess + countFail) + "%\n成功识别" + countSuccess
//						+ "次\n失败识别" + countFail + "次");
                    break;
                case 1:
                    /**扫描异常状态处理*/
                    updateGuidInfo(msg.arg1, msg.arg2, msg.obj);
                    break;
                case 2:
                    Toast.makeText(ctx, "设备连接成功！", 1).show();

                    //mGuidInfo.setText(getString(R.string.idcard_scan_guid));
                    break;
                case 3:
                    //连接失败
                    Toast.makeText(ctx, "设备连接失败！", 1).show();
                    //showDialog(DIALOG_RECONNECT);
                    break;
                default:
                    break;
            }
        }
    };

    ReadIDCardAIDL.Stub stub = new ReadIDCardAIDL.Stub() {
        @Override
        public void create(CallBackIdCard call) {
            // TODO Auto-generated method stub
            callback = call;
            /** 在service绑定成功时候调用，（绑定方法在onResume中） */
            hitownIDcard = new IdCardHitown(ReadIDCardAIDLService.this, handler);
            if (!isModelOpened) {
                new InitTask().execute("load init");
                isModelOpened = true;
                Log.d(TAG, "  load init");
            }


            // 启动身份证读卡器，即扫描身份证的代码
            // 将扫描到的信息放到map中，并调用callback.setInfo(map)方法即可
            // 具体信息请看下面handleMsg方法中的示例代码，map中的key不能改变，必须与示例代
            // 中的key值一致
        }

        @Override
        public void pause() {
            // TODO Auto-generated method stub
            /** 在程序的onPause方法中调用 */
        }

        @Override
        public void finish() {
            // TODO Auto-generated method stub
            /** 在service解绑的时候使用 */
            closeIdt();
        }
    };

    private class InitTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.v(TAG, "doInBackground  " + params[0]);
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /**打开背甲，准备扫描*/
            if (hitownIDcard.hasModule()) {
                hitownIDcard.open();
                if (hitownIDcard.isOpened()) {
                    hitownIDcard.scanMore(handler, 10);
                    isModelOpened = true;
                    handler.sendEmptyMessage(2);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    isModelOpened = false;
                    handler.sendEmptyMessage(3);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    private static void updateInfo(IdCardInfo ici) {

        Log.d(TAG, "updateInfo() =555555555555555 ");

//		mPhoto.setImageBitmap((Bitmap) ici.getmPersonImage());
//		mName.setText(ici.getmPersonName());
//		mSex.setText(ici.getmPersonSex());
//		mNation.setText(hitownIDcard.getPeopleNation(ici.getmPersonNation()));
//		mBirthday.setText(ici.getmPersonBirthday());
//		mCardNum.setText(ici.getmPersonIdCardNum());
//		mAddress.setText(ici.getmPersonAddress());
//		mDepart.setText(ici.getmPersonDepartment());
//		mDate.setText(ici.getmPersonStarDate() +"-"+ ici.getmPersonEndDate());

        //	List<byte[]> fingers=ici.getFingersImage();
        Map map1 = new HashMap();
        map1.put("address", ici.getmPersonAddress()); //idcard.getAddress());
        map1.put("birthday", ici.getmPersonBirthday()); //idcard.getBirthday());
        map1.put("idCode", ici.getmPersonIdCardNum());//idcard.getIDCardNo());
        map1.put("date", ici.getmPersonStarDate() + "-" + ici.getmPersonEndDate());// idcard.getUserLifeBegin()+"-"+idcard.getUserLifeEnd());
        map1.put("department", ici.getmPersonDepartment());// idcard.getGrantDept());
        map1.put("name", ici.getmPersonName());// idcard.getName());
        map1.put("nation", hitownIDcard.getPeopleNation(ici.getmPersonNation()));// idcard.getNation());
        map1.put("sex", ici.getmPersonSex());// idcard.getSex());
        map1.put("photo", (Bitmap) ici.getmPersonImage());// loadImg());

        map1.put("category", ici.getmTagCategory());
        map1.put("passnumber", ici.getmPassNumber());
        map1.put("fingers", ici.getFingersImage());
        if (null != callback) {
            try {
                callback.setInfo(map1);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static void updateGuidInfo(int arg1, int arg2, Object obj) {
        Log.v(TAG, "updateGuidInfo:arg1 = " + arg1);
        switch (arg1) {
            /**设备连接成功*/
            case IdCardHitown.RESULT_INIT_SUCCED:
//			isFinishedInitialized=true;
//			mGuidInfo.setText(getString(R.string.idcard_scan_guid));
                break;
            /**扫描失败*/
            case -IdCardHitown.IDCARD_SETP_READ:
//			mGuidInfo.setText("扫描失败，请重新扫描！");
//			mResult.setText("识别率:" + countSuccess * 100
//					/ (countSuccess + ++countFail) + "%\n成功识别" + countSuccess
//					+ "次\n失败识别" + countFail + "次");
                break;
            case IdCardHitown.IDCARD_DEVICE:
//			mGuidInfo.setText("设备连接成功！");
                break;
            case -IdCardHitown.IDCARD_DEVICE:
//			mGuidInfo.setText("设备连接失败，请检查设备！");
                break;
            /**背甲低电量退出,电量值低于5*/
            case IdCardHitown.IDCARD_DEVICE_POWER_WARN:
//			showDialog(DIALOG_EXIT);
                break;
            /**连接异常*/
            default:
//				mGuidInfo.setText("设备异常，请检查设备！");
//				if (mAnimation.isRunning()) {
//					mAnimation.stop();
//				}
//				Log.d("IdcardScanActivity", "RESULT_ERRO_INIT");
//				isModelOpened=false;	
//				showDialog(DIALOG_RECONNECT);
                break;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind() called");
        ctx = ReadIDCardAIDLService.this;
        return stub;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind() called");
        return true;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        closeIdt();
        Log.i(TAG, "onDestroy() called");
    }

    private void closeIdt() {
        try {
            if (null != hitownIDcard) {
                hitownIDcard.close();
                hitownIDcard = null;
            }
        } catch (IllegalArgumentException e) {
            //Toast.makeText(IdcardScanActivity.this, "hitownIDcard.close() exception", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        isModelOpened = false;
    }

}
