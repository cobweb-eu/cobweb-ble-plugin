package eu.cobwebproject.ucd.ble;

import com.example.bletest.WaspmoteBLEReader.Receiver;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

public class PluginReceiver implements Receiver{

	CallbackContext cbContext;
	public PluginReceiver(CallbackContext cbc){
		
		cbContext=cbc;
	}
	@Override
	public void update(String message) {
		// TODO Auto-generated method stub
		
		/*
		try{
			
			JSONObject jo=new JSONObject();
			jo.put("BLE Message",message);
			PluginResult r = new PluginResult(PluginResult.Status.OK,jo);
			r.setKeepCallback(true);
			cbContext.sendPluginResult(r);
			
		}catch(JSONExcpetion e){
			e.printStackTrace();
		}*/
		
	}

	@Override
	public void addData(String vane, String plu0, String plu1, String plu2,
			String anem) {
		
		JSONObject json=new JSONObject();
		json.put("Vane",vane);
		json.put("Plu0",plu0);
		json.put("Plu1",plu1);
		json.put("Plu2",plu2);
		json.put("Anem",anem);
		PluginResult r = new PluginResult(PluginResult.Status.OK,json);
		cbContext.sendPluginResult(r);
		
		
		
	}
	
	

}
