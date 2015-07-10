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

public class COBWEBBLEPlugin extends CordovaPlugin {


	private static String EMSG="Error connecting to Bluetooth device";

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {

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

	}

	
}
