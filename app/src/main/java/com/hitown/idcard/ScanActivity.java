package com.hitown.idcard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hitown.hitownsdk.IdCardHitown;
import com.unistring_hitown.idcard.R;

import java.util.List;
import java.util.Map;

import cn.com.hnisi.readIDCardAIDL.CallBackIdCard;
import cn.com.hnisi.readIDCardAIDL.ReadIDCardAIDL;
import cn.com.hnisi.service.ReadIDCardAIDLService;


public class ScanActivity extends Activity{

	private static final String TAG = "IdcardScanActivity";
	private PowerManager.WakeLock mWakeLock;
	private Context mContext=null;
	private boolean isFirst = true;
	private static final int DIALOG_RECONNECT = 1000;
	private static final int DIALOG_EXIT = 1010;
	private static final int MSG_INIT = 1000;
	private static final int MSG_INIT_RESULT = 1001;
	private boolean  isFinishedInitialized=false;
	private RelativeLayout mScanLayout;
	private LinearLayout mInfoLayout;
	
	//二代证模块是否打开
	private boolean isModelOpened=false;
	
	private ImageView mPhoto;
	private TextView mWraning, mCategory,mName, mSex, mNationLable,mNation, mBirthday, mCardNum,
	mAddress, mDepart, mDate,mFinger,mFingerLable, mGuidInfo ,mPassNumber,mPassNumberLable;
	
	private Button btn_close;
	private Button btn_open;

	private ProgressDialog progressDialog = null;
	
	private ImageView mScanImg;
	private TextView mResult;
	private AnimationDrawable mAnimation;
	IdCardHitown hitownIDcard = null;
	int countSuccess = 0, countFail = 0;
	
	private ReadIDCardAIDL readIDCard;	
	private CallBackIdCard callBackIdCard=new CallBackIdCard(){

		@Override
		public void setInfo(Map map) {
			// TODO Auto-generated method stub
			mInfoLayout.setVisibility(View.VISIBLE);
			mScanLayout.setVisibility(View.GONE);	
			updateInfo(map);		
			if (mAnimation.isRunning()) {
				mAnimation.stop();
			}
			mResult.setText("识别率:" + ++countSuccess * 100
					/ (countSuccess + countFail) + "%\n成功识别" + countSuccess
					+ "次\n失败识别" + countFail + "次");
		}

		@Override
		public IBinder asBinder() {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	private ServiceConnection mSConn=new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			try {
				readIDCard.finish();					
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.w(TAG, "IDCRead service finished error! reason: " + e.getMessage());
			}
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			readIDCard = ReadIDCardAIDL.Stub.asInterface(service);
			try {
				readIDCard.create(callBackIdCard);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.w(TAG, "IDCRead service created error! reason: " + e.getMessage());
			}			
		}
	};
	private boolean isOtherOpen;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.idcard_main);
		mContext= ScanActivity.this;
		initView();
		
		Intent idtService=new Intent(this, ReadIDCardAIDLService.class);
		idtService.setAction("android.intent.action.ReadIDCardAIDLService");
		idtService.setPackage("cn.com.hnisi.service");
		isModelOpened = bindService(idtService, mSConn, Service.BIND_AUTO_CREATE);
		isOtherOpen=getIntent()!=null&&getIntent().getDataString()!=null&&getIntent().getDataString().equals("idcard://dev.unistrong.com");

	}

	
	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();		

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isModelOpened) {
			unbindService(mSConn);
			isModelOpened = false;
		}
		readIDCard = null;
	}

	private void initView() {
		mScanLayout = (RelativeLayout) findViewById(R.id.idcard_scan_layout);
		mInfoLayout = (LinearLayout) findViewById(R.id.finger_info_layout);
		mPhoto = (ImageView) findViewById(R.id.idcard_info_photo);
		mWraning = (TextView) findViewById(R.id.idcard_info_warning);
		mWraning.setVisibility(View.GONE);
		mName = (TextView) findViewById(R.id.idcard_name);
		mCategory=(TextView) findViewById(R.id.idcard_category);
		mSex = (TextView) findViewById(R.id.idcard_sex);
		mNationLable=(TextView) findViewById(R.id.idcard_nation_lable);
		mNation = (TextView) findViewById(R.id.idcard_nation);
		mBirthday = (TextView) findViewById(R.id.idcard_birthday);
		mCardNum = (TextView) findViewById(R.id.idcard_num);
		mAddress = (TextView) findViewById(R.id.idcard_address);
		mDepart = (TextView) findViewById(R.id.idcard_department);
		mDate = (TextView) findViewById(R.id.idcard_date);
		mFinger=(TextView) findViewById(R.id.idcard_fingers);
		mFingerLable=(TextView) findViewById(R.id.idcard_fingers_lable);
		mResult = (TextView) findViewById(R.id.result);
		mScanImg = (ImageView) findViewById(R.id.idcard_scan_img);
		mGuidInfo = (TextView) findViewById(R.id.idcard_scan_guidInfo);
		mPassNumber=(TextView) findViewById(R.id.gat_passnumber);
		mPassNumberLable=(TextView) findViewById(R.id.gat_passnumber_lable);
		mPassNumberLable.setVisibility(View.INVISIBLE);
		mScanImg.setImageResource(R.anim.idcard_scan_anim);
		// mScanImg.setBackgroundResource(R.anim.idcard_scan_anim);
		mAnimation = (AnimationDrawable) mScanImg.getDrawable();
		mGuidInfo.setText("设备正在初始化，请稍候...");
		mInfoLayout.setVisibility(View.GONE);
		mScanLayout.setVisibility(View.VISIBLE);
		btn_close = (Button)findViewById(R.id.btn_close);
		btn_open = (Button)findViewById(R.id.btn_open);
		btn_close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (isModelOpened) {
					unbindService(mSConn);
					isModelOpened = false;
				}
			}
		});
		
		btn_open.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent idtService=new Intent("android.intent.action.ReadIDCardAIDLService");
				isModelOpened = bindService(idtService, mSConn, Service.BIND_AUTO_CREATE);
			}
		});
	}

	private void updateInfo(Map map) {
		Intent result =new Intent();

		Bitmap photo = (Bitmap) map.get("photo");
		result.putExtra("photo",photo);
		mPhoto.setImageBitmap(photo);
		String name = map.get("name").toString();
		result.putExtra("name",name);
		mName.setText(name);
		String sex = map.get("sex").toString();
		result.putExtra("sex",sex);
		mSex.setText(sex);

		String birthday = map.get("birthday").toString();
		result.putExtra("birthday",birthday);
		mBirthday.setText(birthday);
		String idCode = map.get("idCode").toString();
		result.putExtra("idCode",idCode);
		mCardNum.setText(idCode);
		String address = map.get("address").toString();
		result.putExtra("address",address);
		mAddress.setText(address);
		String department = map.get("department").toString();
		result.putExtra("department",department);
		mDepart.setText(department);
		String date = map.get("date").toString();
		result.putExtra("date",date);
		mDate.setText(date);

		String category = map.get("category").toString();
		result.putExtra("category",category);
		if(category.equalsIgnoreCase("GAT")){
			mCategory.setText("港澳通居住证");
			mPassNumberLable.setVisibility(View.VISIBLE);
			mPassNumber.setVisibility(View.VISIBLE);
			String passnumber = map.get("passnumber").toString();
			result.putExtra("passnumber",passnumber);
			mPassNumber.setText(passnumber);
			mNationLable.setVisibility(View.INVISIBLE);
			mNation.setVisibility(View.INVISIBLE);			
		}else{
			mCategory.setText("二代身份证");
			mNationLable.setVisibility(View.VISIBLE);
			mNation.setVisibility(View.VISIBLE);
			String nation = map.get("nation").toString();
			result.putExtra("nation",nation);
			mNation.setText(nation);
			mPassNumberLable.setVisibility(View.INVISIBLE);
			mPassNumber.setVisibility(View.INVISIBLE);
		}
		
		List<byte[]> fingers=(List<byte[]>)map.get("fingers");
		if(fingers!=null){			
			int count=fingers.size();	
			if(count==2){
				mFinger.setText("双指指纹特征信息已查询到");
			}else if(count==1){
				mFinger.setText("单指指纹特征信息已查询到");
			}			
		}else{
			mFinger.setText("该卡未查询到有录入指纹信息");
		}
		if (isOtherOpen){
			setResult(Activity.RESULT_OK,result);
			finish();
		}
	}
}
