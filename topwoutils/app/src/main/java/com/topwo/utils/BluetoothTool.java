package com.tangram.mwl.fenjian;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.tangram.bluetooth.BluetoothHelper;
import com.tangram.hubri.none.YayoGameMidlet;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import tangram.engine.tools.systools;

public class BluetoothTool {

	private static final String TAG = "BleTool";

	enum ExternalDeviceEventId {
		SCAN_END(0),
		ADD_DEVICE(1),
		UPDATE_STATE(2),
		RECEIVE_DATA(3);
		private int value;
		ExternalDeviceEventId(int value) {
			this.value = value;
		}
	}
	enum ExternalDeviceConnectState {
		DISCONNECTED("DISCONNECTED"),
		CONNECT_FAILED("CONNECT_FAILED");
		private String value;
		ExternalDeviceConnectState(String value) {
			this.value = value;
		}
	}

	// 蓝牙相关
	public static boolean mState = false;
	public static boolean isConnecting = false;
	public static BluetoothAdapter mBtAdapter = null;

	public static List<BluetoothDevice> deviceList = new ArrayList<>();

	@SuppressLint("NewApi")
	public BluetoothTool() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			String[] mPermissionList = new String[] {
					Manifest.permission.ACCESS_FINE_LOCATION};
			if (ContextCompat.checkSelfPermission(YayoGameMidlet.ygm, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

			} else {
				ActivityCompat.requestPermissions(YayoGameMidlet.ygm, mPermissionList, 200);
			}
		}

		deviceList = new ArrayList<BluetoothDevice>();

		BluetoothHelper.getInstance().initBluetoothHelper(YayoGameMidlet.ygm);
		BluetoothHelper.getInstance().setDeviceDiscoveryListener(new BluetoothHelper.OnBluetoothDeviceDiscoveryListener() {
			@Override
			public void onBluetoothStart() {
			}

			@Override
			public void onBluetoothDiscover(BluetoothDevice device) {
				addDevice(device);
			}

			@Override
			public void onBluetoothDiscoverFinish() {
				tellQQBScanEnd("");
			}
		});
		BluetoothHelper.getInstance().setDeviceConnectListener(new BluetoothHelper.OnBluetoothDeviceConnectListener() {
			@Override
			public void onDeviceConnect(BluetoothDevice device) {
				if (null != device) {
					tellQQBConnectState(device.getAddress());
				}
			}

			@Override
			public void onDeviceDisconnect(BluetoothDevice device) {
				tellQQBConnectState("");
			}
		});
	}

	public static int blueToothEventRank = 0;
	public static int blueToothEventId = 0;

	/**
	 * 搜索设备
	 * @param enable
	 * @param rank
	 * @param eventId
	 */
	public void scanLeDevice(final boolean enable, int rank, int eventId) {
		blueToothEventRank = rank;
		blueToothEventId = eventId;

		if (enable) {
			deviceList.clear();
			Set<BluetoothDevice> bondedDevices = BluetoothHelper.getInstance().getBondedDevices();
			if (null != bondedDevices) {
				for (BluetoothDevice device : bondedDevices) {
					addDevice(device);
				}
			}
			BluetoothHelper.getInstance().startDiscovery();
		} else {
			BluetoothHelper.getInstance().stopDiscovery();
		}
	}

	/**
	 * 连接蓝牙设备
	 * @param address
	 */
	public void connectByAddress(String address) {

		for (BluetoothDevice listDev : deviceList) {
			if (listDev.getAddress().equals(address)) {
				BluetoothHelper.getInstance().connectBluetoothDeviceGatt(listDev);
				break;
			}
		}
	}


	/**
	 * 添加设备
	 * @param device
	 */
	private void addDevice(BluetoothDevice device) {
		boolean deviceFound = false;

		for (BluetoothDevice listDev : deviceList) {
			if (listDev.getAddress().equals(device.getAddress())) {
				deviceFound = true;
				break;
			}
		}
		String name = device.getName();
		String address = device.getAddress();

		if(name == null) {// || !(name.startsWith("LD HR")||name.startsWith("LD hr"))
			deviceFound = true;
			Log.e(TAG, name + ":" + device.getAddress());
		}

		//if (!deviceFound && device.getName() != null && (device.getName().startsWith("LD HR")||device.getName().startsWith("LD hr"))) {
		if (!deviceFound) {
			deviceList.add(device);

			tellQQBAddDevice(name, address);

			if (BluetoothHelper.getInstance().isConnected(address)) {
				tellQQBConnectState(address);
			}
		}
	}

	/**
	 * 告诉七巧板增加设备
	 * @param name
	 * @param address
	 */
	private static void tellQQBAddDevice(String name, String address) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("eventId", ExternalDeviceEventId.ADD_DEVICE.value);
			jsonObject.put("address", address);
			jsonObject.put("name", name);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		systools.sstl.callBackWithStr(blueToothEventId, jsonObject.toString(), blueToothEventRank);
	}

	/**
	 * 告诉七巧板搜索结束
	 * @param address
	 */
	private static void tellQQBScanEnd(String address) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("eventId", ExternalDeviceEventId.SCAN_END.value);
			jsonObject.put("address", address);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		systools.sstl.callBackWithStr(blueToothEventId, jsonObject.toString(), blueToothEventRank);
	}

	/**
	 * 告诉七巧板连接状态
	 * @param state，直接给地址代表连接的哪个设备
	 */
	private static void tellQQBConnectState(String state) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("eventId", ExternalDeviceEventId.UPDATE_STATE.value);
			jsonObject.put("address", state); // CONNECT_FAILED:连接失败
		} catch (JSONException e) {
			e.printStackTrace();
		}
		systools.sstl.callBackWithStr(blueToothEventId, jsonObject.toString(), blueToothEventRank);
	}


	/**
	 * 告诉七巧板接收到的数据
	 * @param data
	 */
	private static void tellQQBReceiveData(String data) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("eventId", ExternalDeviceEventId.RECEIVE_DATA.value);
			jsonObject.put("data", data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		systools.sstl.callBackWithStr(blueToothEventId, jsonObject.toString(), blueToothEventRank);
	}
}
