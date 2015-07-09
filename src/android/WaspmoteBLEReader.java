package eu.cobwebproject.ucd.ble;

import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class WaspmoteBLEReader {
	private static final int SCAN_PERIOD = 10000;
	private static final UUID UUID_SERVICE = UUID
			.fromString("eed82c0a-b1c2-401e-ae4a-afac80c80c72");

	private static final UUID UUID_VANE = UUID
			.fromString("2eec2523-cf8f-4c65-8538-397baa8b1b0b");
	private static final UUID UUID_PLU0 = UUID
			.fromString("e562b410-e8d2-4fe0-89c1-91432f108fe1");
	private static final UUID UUID_PLU1 = UUID
			.fromString("a94ba516-c627-4e00-a28b-b5cd825d8e14");// Hour
	private static final UUID UUID_PLU2 = UUID
			.fromString("be39a5dc-048b-4b8f-84cb-94c197edd26e");// Day
	private static final UUID UUID_ANEM = UUID
			.fromString("a5853c93-08af-4186-8dba-b4c0cc74a23b");

	private String vane = null;
	private String plu0 = null;
	private String plu1 = null;
	private String plu2 = null;
	private String anem = null;

	private BluetoothAdapter adapter;
	private Receiver receiver;
	private Context context;

	@SuppressWarnings("deprecation")
	public WaspmoteBLEReader(BluetoothAdapter adapter, Receiver receiver,
			Context context) {
		this.adapter = adapter;
		this.receiver = receiver;
		this.context = context;
		receiver.update("Bluetooth Connected.");
	}

	public void start() {
		Handler mHandler = new Handler(context.getMainLooper());
		// mHandler.post
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				WaspmoteBLEReader.this.adapter.stopLeScan(callback_scan);
			}
		}, SCAN_PERIOD);

		adapter.startLeScan(callback_scan);

		receiver.update("Scanning");
	}

	private BluetoothAdapter.LeScanCallback callback_scan = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			device.connectGatt(context, false, callback_gatt);
		}
	};

	private BluetoothGattCallback callback_gatt = new BluetoothGattCallback() {
		private BluetoothGattCharacteristic gatt_plu0;
		private BluetoothGattCharacteristic gatt_plu1;

		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			if (newState == BluetoothGatt.STATE_CONNECTED) {
				gatt.discoverServices();
				receiver.update("Discovering Services");
			}
		};

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			super.onServicesDiscovered(gatt, status);

			BluetoothGattService service = gatt.getService(UUID_SERVICE);
			if (service == null)
				return;

			receiver.update("Reading Properties");

			BluetoothGattCharacteristic prop;
			prop = service.getCharacteristic(UUID_VANE);
			gatt.readCharacteristic(prop);
			gatt_plu0 = service.getCharacteristic(UUID_PLU0);
			// gatt.readCharacteristic(gatt_plu0);
			gatt_plu1 = service.getCharacteristic(UUID_PLU1);
			// gatt.readCharacteristic(gatt_plu1sFs);
			prop = service.getCharacteristic(UUID_PLU2);
			gatt.readCharacteristic(prop);
			prop = service.getCharacteristic(UUID_ANEM);
			gatt.readCharacteristic(prop);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicRead(gatt, characteristic, status);
			if (status != BluetoothGatt.GATT_SUCCESS)
				Log.w("BLE Test", "Read failed for some reason.");

			UUID id = characteristic.getUuid();
			byte[] val = characteristic.getValue();
			String value = val != null && val.length > 0 ? new String(val)
					: "Unkown";

			if (UUID_VANE.equals(id)) {
				vane = value;
				gatt.readCharacteristic(gatt_plu0);
				Log.i("BLE Test", "Vane: " + value);
			} else if (UUID_PLU0.equals(id)) {
				plu0 = value;
				gatt.readCharacteristic(gatt_plu1);
				Log.i("BLE Test", "Plu0: " + value);
			} else if (UUID_PLU1.equals(id)) {
				plu1 = value;
				Log.i("BLE Test", "Plu1: " + value);
			} else if (UUID_PLU2.equals(id)) {
				plu2 = value;
				Log.i("BLE Test", "Plu2: " + value);
			} else if (UUID_ANEM.equals(id)) {
				anem = value;
				Log.i("BLE Test", "Anem: " + value);
			}
			if (vane != null && plu0 != null && plu1 != null && plu2 != null
					&& anem != null) {
				Log.i("BLE Test", "Completed");
				receiver.update("All sensors received");
				receiver.addData(vane, plu0, plu1, plu2, anem);
				vane = plu0 = plu1 = plu2 = anem = null;
				gatt.disconnect();
				gatt.close();
			}
		}
	};

	public interface Receiver {
		void update(String message);

		void addData(String vane, String plu0, String plu1, String plu2,
				String anem);

	}
}
