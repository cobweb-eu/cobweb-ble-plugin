package eu.cobwebproject.ucd.ble;

import eu.cobwebproject.ucd.ble.WaspmoteBLEReader.Receiver;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

public class PluginReceiver implements Receiver{


	private static final String ADDR="Address";
	private static final String VANE="Vane";
	private static final String PLU0="Plu0";
	private static final String PLU1="Plu1";
	private static final String PLU2="Plu2";
	private static final String ANEM="Anem";
	private static final String POWR="Power";
	private static final String TIME="Time";
	
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
	public void addData(String addr, String vane, String plu0, String plu1,
				String plu2, String anem, String powr, String time) {
		try{
			JSONObject json=new JSONObject();
			json.put(ADDR,addr);
			json.put(VANE,vane);
			json.put(PLU0,plu0);
			json.put(PLU1,plu1);
			json.put(PLU2,plu2);
			json.put(ANEM,anem);
			json.put(POWR,powr);
			json.put(TIME,time);
			JSONArray jArr=new JSONArray();
			jArr.put(json);
			sendData(jArr);
		}catch(JSONException e){
			e.printStackTrace();
		}
		
	}
	
	private void sendData(JSONArray jArr){
		PluginResult r = new PluginResult(PluginResult.Status.OK,jArr);
		cbContext.sendPluginResult(r);
	}
	
	public void addData(double[][]data) {
		try{
			int n=data.length;
			JSONArray jArr=new JSONArray();
			for(int i=0;i<n;i++){
				JSONObject json=new JSONObject();
				json.put(VANE,data[i][0]);
				json.put(PLU0,data[i][1]);
				json.put(PLU1,data[i][2]);
				json.put(PLU2,data[i][3]);
				json.put(ANEM,data[i][4]);
				jArr.put(json);
			}
			
			sendData(jArr);
			
		}catch(JSONException e){
			e.printStackTrace();
		}
		
	}
	
	

}
