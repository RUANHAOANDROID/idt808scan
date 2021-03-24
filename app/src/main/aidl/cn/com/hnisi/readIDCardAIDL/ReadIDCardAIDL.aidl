package cn.com.hnisi.readIDCardAIDL;

import cn.com.hnisi.readIDCardAIDL.CallBackIdCard;

interface ReadIDCardAIDL{
	/**在service绑定成功时候调用，（绑定方法在onResume中）*/
	void create(CallBackIdCard call);
	/**在程序的onPause方法中调用*/
	void pause();
	/**在service解绑的时候使用*/
	void finish();
}