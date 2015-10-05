package eu.cobwebproject.ucd.ble;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;

import android.os.Build;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;

import android.os.Bundle;

import android.util.Log;
import java.util.Random;

public class COBWEBBLEPlugin extends CordovaPlugin {


	private static String EMSG="Error connecting to Bluetooth device";
	private static final int NUMT=10;
	private static final int NUMS=5;
	private static final String READ="btRead";
	private static final String TEST="test";
	private static final String AREAD="btArrayRead";
	private static final String ATEST="testArr";
	private static final int STIME=10000;

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {

		if(action.equals(READ)){
			BluetoothManager bluetoothManager = (BluetoothManager) cordova.getActivity().
				getSystemService(Context.BLUETOOTH_SERVICE);
			BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

			if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
				callbackContext.error(EMSG);
			}else{
				PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
				r.setKeepCallback(true);
				callbackContext.sendPluginResult(r);
				WaspmoteBLEReader reader = new WaspmoteBLEReader(mBluetoothAdapter, new PluginReceiver(callbackContext),cordova.getActivity());
				reader.start();
			}
			return true;
			
		}else if(action.equals(AREAD)){
			
			// Not supported yet
			callbackContext.error(EMSG);
			
			return true;
		}else if(action.equals(ATEST)){
			final PluginReceiver pr= new PluginReceiver(callbackContext);
			new Thread(){
				public void run(){
					try{
						Thread.sleep(STIME);
						double testData[][]=new double[NUMT][NUMS];
						Random r=new Random();
						for(int i=0;i<NUMT;i++){
							for(int j=0;j<NUMS;j++){
								testData[i][j]=r.nextDouble();
							}
						}
						
						pr.addData(testData);
						
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}.start();
			
			return true;

		}/*else if(action.equals(TEST)){
			final PluginReceiver pr= new PluginReceiver(callbackContext);
			new Thread(){
				public void run(){
					try{
						Thread.sleep(STIME);
						pr.addData("1.3","2.3","1.0","2.0","2.1");
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}.start();
			return true;
		}*/
		
		return false;

	}

	
}
