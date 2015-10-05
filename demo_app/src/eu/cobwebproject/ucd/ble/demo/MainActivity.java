package eu.cobwebproject.ucd.ble.demo;

import eu.cobwebproject.ucd.ble.WaspmoteBLEReader;
import eu.cobwebproject.ucd.ble.WaspmoteBLEReader.Receiver;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements Receiver, OnClickListener {

	private static final int REQUEST_ENABLE_BT = 55;
	private TextView view;
	private WaspmoteBLEReader reader;
	private TextView readings;
	private Button button;
	private Button stop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		view = (TextView) findViewById(R.id.text);
		readings = (TextView) findViewById(R.id.readings);
		button = (Button) findViewById(R.id.button);
		button.setEnabled(false);
		button.setOnClickListener(this);
		stop = (Button) findViewById(R.id.stop);
		stop.setOnClickListener(this);

		// Initializes Bluetooth adapter.
		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			update("Awaiting input");
			reader = new WaspmoteBLEReader(mBluetoothAdapter, this, this);
			button.setEnabled(true);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ENABLE_BT) {
			BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
			if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
				update("Bluetooth not enabled.");
				return;
			}
			update("Awaiting input");
			// reader = new WaspmoteBLEReader(mBluetoothAdapter, this);
			reader = new WaspmoteBLEReader(mBluetoothAdapter, this, this);
			button.setEnabled(true);
		}
	}

	public void update(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				view.setText(message);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void addData(final String address, final String vane,
			final String plu0, final String plu1, final String plu2,
			final String anem, final String powr, final String time) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				readings.setText(String
						.format("%s\r\nVane %s\r\n Plu %s %s %s\r\nAnem %s\r\nTime %s\r\nPower %s",
								address, vane, plu0, plu1, plu2, anem, time,
								powr));
			}
		});
	}

	

	@Override
	public void onClick(View v) {
		if (v == button)
			reader.start();
		if (v == stop && reader != null)
			reader.stop();
	}

	@Override
	public void failed() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				update("Failed to get reading");
			}
		});
	}
}
