package eu.cobwebproject.ucd.ble;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

public class WaspmoteBLEReader {
	// private static final int SCAN_PERIOD = 10000;
	protected static final UUID UUID_SERVICE = UUID
			.fromString("eed82c0a-b1c2-401e-ae4a-afac80c80c72");

	protected static final UUID UUID_CHARACTERISTIC = UUID
			.fromString("2eec2523-cf8f-4c65-8538-397baa8b1b0b");
	// protected static final UUID UUID_ANEM = UUID
	// .fromString("a5853c93-08af-4186-8dba-b4c0cc74a23b");
	// protected static final UUID UUID_PLU0 = UUID
	// .fromString("e562b410-e8d2-4fe0-89c1-91432f108fe1");//Current Hour
	// protected static final UUID UUID_PLU1 = UUID
	// .fromString("a94ba516-c627-4e00-a28b-b5cd825d8e14");//Previous Hour
	// protected static final UUID UUID_PLU2 = UUID
	// .fromString("be39a5dc-048b-4b8f-84cb-94c197edd26e");//Day

	private String vane = null;
	private String plu0 = null;
	private String plu1 = null;
	private String plu2 = null;
	private String anem = null;
	private String powr = null;
	private String time = null;

	private String address = null;

	private BluetoothAdapter adapter;
	private Receiver receiver;

	public WaspmoteBLEReader(BluetoothAdapter adapter, Receiver receiver) {
		this.adapter = adapter;
		this.receiver = receiver;

		receiver.update("Bluetooth Connected.");
	}

	@SuppressWarnings("deprecation")
	public void start() {
		adapter.startLeScan(callback_scan);

		receiver.update("Scanning");
	}

	@SuppressWarnings("deprecation")
	public void stop() {
		this.adapter.stopLeScan(callback_scan);
	}

	private BluetoothAdapter.LeScanCallback callback_scan = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			String address = device.getAddress();
			if (address.startsWith("00:07:80:04:FA:35"))
				device.connectGatt(receiver.getContext(), false, callback_gatt);
		}
	};

	private BluetoothGattCallback callback_gatt = new BluetoothGattCallback() {
		private Queue<BluetoothGattCharacteristic> queue = new LinkedList<BluetoothGattCharacteristic>();

		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			if (newState == BluetoothGatt.STATE_CONNECTED) {
				gatt.discoverServices();
				receiver.update("Discovering Services");
			}
		};

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// super.onServicesDiscovered(gatt, status);
			if (status != BluetoothGatt.GATT_SUCCESS) {
				gatt.discoverServices();
				return;
			}

			BluetoothGattService service = gatt.getService(UUID_SERVICE);
			if (service == null)
				return;

			// stop();
			receiver.update("Reading Properties");

			BluetoothGattCharacteristic prop = service
					.getCharacteristic(UUID_CHARACTERISTIC);
			if (prop == null) {
				gatt.discoverServices();
				return;
			}
			boolean success = gatt.readCharacteristic(prop);
			// queue.add(gatt_vane);
			// prop = service.getCharacteristic(UUID_PLU0);
			// queue.add(prop);
			// prop = service.getCharacteristic(UUID_PLU1);
			// queue.add(prop);
			// prop = service.getCharacteristic(UUID_PLU2);
			// queue.add(prop);
			// prop = service.getCharacteristic(UUID_ANEM);
			// queue.add(prop);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// super.onCharacteristicRead(gatt, characteristic, status);

			if (status != BluetoothGatt.GATT_SUCCESS) {
				Log.w("BLE Test", "Read failed for some reason.");
				queue.add(characteristic);
			}

			BluetoothGattCharacteristic next = queue.poll();
			if (next != null) {
				boolean success = gatt.readCharacteristic(next);
				Log.i("BLE Test", "Reading next charactertic -" + success);
				if (!success)
					queue.add(next);
			}

			if (status != BluetoothGatt.GATT_SUCCESS) {
				return;
			}
			address = gatt.getDevice().getAddress();
			UUID id = characteristic.getUuid();
			byte[] val = characteristic.getValue();

			if (UUID_CHARACTERISTIC.equals(id)) {
				// Prop Loc Len
				// Vane 0 1
				// Anem 1 3
				// Plu0 4 3
				// Plu1 7 3
				// Plu2 10 3
				// Pwr 13 1
				// Time 14 4
				// empty 18 - 20
				vane = getDirection(val[0]);
				anem = extract(Arrays.copyOfRange(val, 1, 4));
				plu0 = extract(Arrays.copyOfRange(val, 4, 7));
				plu1 = extract(Arrays.copyOfRange(val, 7, 10));
				plu2 = extract(Arrays.copyOfRange(val, 10, 13));
				powr = Byte.valueOf(val[13]).toString();
				time = extractTime(Arrays.copyOfRange(val, 14, 18));

				receiver.update("All sensors received");
				receiver.addData(address, vane, plu0, plu1, plu2, anem, powr,
						time);
				vane = plu0 = plu1 = plu2 = anem = powr = time = null;
				stop();
				gatt.disconnect();
				gatt.close();
			}
		}
	};

	public interface Receiver {
		void update(String message);

		void addData(String address, String vane, String plu0, String plu1,
				String plu2, String anem, String powr, String time);

		Context getContext();

		Looper getMainLooper();
	}

	protected String getDirection(byte b) {
		String val = "error";
		switch (b) {
		case 0:
			val = "N";
			break;
		case 1:
			val = "NNE";
			break;
		case 2:
			val = "NE";
			break;
		case 3:
			val = "ENE";
			break;
		case 4:
			val = "E";
			break;
		case 5:
			val = "ESE";
			break;
		case 6:
			val = "SE";
			break;
		case 7:
			val = "SSE";
			break;
		case 8:
			val = "S";
			break;
		case 9:
			val = "SSW";
			break;
		case 10:
			val = "SW";
			break;
		case 11:
			val = "WSW";
			break;
		case 12:
			val = "W";
			break;
		case 13:
			val = "WNW";
			break;
		case 14:
			val = "NW";
			break;
		case 15:
			val = "NNW";
			break;
		}
		return val;
	}

	protected String extractTime(byte[] temp) {
		long val = ((temp[0] & 0xFF) << 24);
		val |= ((temp[1] & 0xFF) << 16);
		val |= ((temp[2] & 0xFF) << 8);
		val |= (temp[3] & 0xFF);
		return String.format("%d", val);
	}

	protected String extract(byte[] temp) {
		long val = ((temp[0] & 0xFF) << 16);
		val |= ((temp[1] & 0xFF) << 8);
		val |= (temp[2] & 0xFF);
		float value = ((float) val) / 100;
		return String.format("%.2f", value);
	}
}
