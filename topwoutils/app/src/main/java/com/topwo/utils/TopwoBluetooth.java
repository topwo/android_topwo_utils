package com.tangram.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class BluetoothHelper {
    private static final String TAG = BluetoothHelper.class.getSimpleName();

    public static final int REQUEST_ENABLE_BT = 2;

    //蓝牙称的蓝牙属性
    public static final UUID RX_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");// 服务
    public static final UUID RX_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");// 写
    public static final UUID TX_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");// 读

    private Context mContext;
    private IntentFilter intentFilter;
    private BluetoothAdapter mDefaultAdapter;
    private BluetoothDevice mBluetoothDevice = null;
    //连接服务对应的Gatt
    private BluetoothGatt mBluetoothGatt;
    public static final String BLE_NAME_PREFIX = "";
    public static BluetoothGattCharacteristic mWriteCharacteristic;
    public static BluetoothGattCharacteristic mReadCharacteristic;
    private boolean isConnect = false;
    private boolean isScanAfterOpened = false; //蓝牙开启后是否进行搜索

    private BluetoothHelper() {

    }

    public String getBluetoothName() {
        if (mDefaultAdapter != null) {
            return mDefaultAdapter.getName();
        }
        return null;
    }

    public String getBluetoothAddress() {
        if (mDefaultAdapter != null) {
            return mDefaultAdapter.getAddress();
        }
        return null;
    }

    public void setBluetoothName(String userName) {
        if (mDefaultAdapter != null) {
            mDefaultAdapter.setName(userName);
        }
    }

    private static class SingleBluetooth {
        private static BluetoothHelper sCommonBluetooth = new BluetoothHelper();
    }

    public static BluetoothHelper getInstance() {
        return SingleBluetooth.sCommonBluetooth;
    }

    public boolean initBlueFunction(Activity activity) {
        mContext = activity.getApplicationContext();
        //获取 BluetoothAdapter
        mDefaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mDefaultAdapter == null) {
            Log.e(TAG, "不支持蓝牙功能");
            //不支持蓝牙功能
            return false;
        }

        //蓝牙是否打开
        boolean enabled = mDefaultAdapter.isEnabled();
        if (!enabled) {
            //蓝牙未打开
            Log.i(TAG, "蓝牙未打开");
            //打开蓝牙设备
            mDefaultAdapter.enable();
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            activity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        //校对本地数据
//        //注册蓝牙设备接入监听
//        registDeviceConnect();
//        //设置可被发现
//        setBluetoothDiscover();
//        //接收消息
//        registDeviceMsgFunction();

        //注册蓝牙广播
        registBroadBluetooth();

        return true;
    }

    // 销毁
    public void destroy() {
        if(intentFilter != null){
            intentFilter = null;
            unRegistBroadBluetooth();
        }
    }


    //获取以配对过的设备
    public Set<BluetoothDevice> getBondedDevices() {
        if (mDefaultAdapter == null) {
            return null;
        }

        return mDefaultAdapter.getBondedDevices();
    }

    //开始扫描经典蓝牙设备
    public void startDiscovery() {
        if (mDefaultAdapter != null) {
            if (!mDefaultAdapter.isEnabled()) {
//                isScanAfterOpened = true;
                //打开蓝牙设备
                mDefaultAdapter.enable();
                mDeviceDiscoveryListener.onBluetoothDiscoverFinish();
                return;
            }
            //判断当前是否正在扫描
            boolean discovering = mDefaultAdapter.isDiscovering();
            if (!discovering) {
                //开启扫描
                mDefaultAdapter.startDiscovery();
            }
        }
    }

    //关闭扫描
    public boolean stopDiscovery() {
        if (mDefaultAdapter != null) {
            boolean discovering = mDefaultAdapter.isDiscovering();
            if (discovering) {
                return mDefaultAdapter.cancelDiscovery();
            }

        }
        return true;
    }



    /**
     * 连接蓝牙设备Gatt服务
     * @param device
     */
    public void connectBluetoothDeviceGatt(BluetoothDevice device) {
        if (null != mBluetoothGatt) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            mBluetoothDevice = null;
        }
        if (null != device) {
            mBluetoothDevice = device;
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, true, mGattCallback);
        }
    }

    /**
     * 是否连接
     * @param macAddress
     * @return
     */
    public boolean isConnected(String macAddress){
        if (!BluetoothAdapter.checkBluetoothAddress(macAddress)){
            return false;
        }
        BluetoothDevice device = mDefaultAdapter.getRemoteDevice(macAddress);

        Method isConnectedMethod = null;
        boolean isConnected;
        try {
            isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
            isConnectedMethod.setAccessible(true);
            isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
        } catch (NoSuchMethodException e) {
            isConnected = false;
        } catch (IllegalAccessException e) {
            isConnected = false;
        } catch (InvocationTargetException e) {
            isConnected = false;
        }
        return isConnected;
    }

    //注册蓝牙广播
    private void registBroadBluetooth() {
        if(intentFilter != null){
            return;
        }

        intentFilter = new IntentFilter();
        //蓝牙状态改变
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //开始扫描设备
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //扫描完成
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //发现设备
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //配对状态改变
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //设备建立连接
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        //设备断开连接
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        mContext.registerReceiver(mBluetoothBroadcastReceiver, intentFilter);
    }

    //注销蓝牙广播
    private void unRegistBroadBluetooth() {
        mContext.unregisterReceiver(mBluetoothBroadcastReceiver);
        mBluetoothBroadcastReceiver = null;
    }

    private BroadcastReceiver mBluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            //蓝牙状态改变
            if (TextUtils.equals(action, BluetoothAdapter.ACTION_STATE_CHANGED)) {

                //获取当前蓝牙的状态
                int currentStatue = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                //获取改变前的蓝牙的状态
                int preStatue = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, 0);

                switch (currentStatue) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        //蓝牙将要打开
                        Log.d(TAG, "蓝牙将要打开");
                        //接口回调
                        if (mStateListener != null) {
                            mStateListener.onBluetoothWillOpen();
                        }
                        break;
                    case BluetoothAdapter.STATE_ON:
                        //蓝牙打开
                        Log.d(TAG, "蓝牙打开");
                        if (isScanAfterOpened) {
                            isScanAfterOpened = false;
                            startDiscovery();
                        }
                        //接口回调
                        if (mStateListener != null) {
                            mStateListener.onBluetoothOpen();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //蓝牙将要关闭
                        Log.d(TAG, "蓝牙将要关闭");
                        //接口回调
                        if (mStateListener != null) {
                            mStateListener.onBluetoothWillClose();
                        }
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        //蓝牙已关闭
                        Log.d(TAG, "蓝牙已关闭");
                        //接口回调
                        if (mStateListener != null) {
                            mStateListener.onBluetoothClose();
                        }
                        break;
                }
                return;
            }
            //设备开始扫描
            else if (TextUtils.equals(action, BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Log.d(TAG, "开始扫描蓝牙设备");
                if (mDeviceDiscoveryListener != null) {
                    mDeviceDiscoveryListener.onBluetoothStart();
                }
                return;
            }
            else if (TextUtils.equals(action, BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Log.d(TAG, "扫描完成");
                if (mDeviceDiscoveryListener != null) {
                    mDeviceDiscoveryListener.onBluetoothDiscoverFinish();
                }
                return;
            }
            else if (TextUtils.equals(action, BluetoothDevice.ACTION_FOUND)) {
                Log.d(TAG, "发现设备");
                //获取蓝牙对象
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e(TAG, device.getName() + "->" + device.getAddress());
                if (isConnected(device.getAddress())) {
                    mBluetoothDevice = device;
                }
                if (mDeviceDiscoveryListener != null) {
                    mDeviceDiscoveryListener.onBluetoothDiscover(device);
                }
                return;
            }
            //配对状态改变时
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING://正在配对
                        Log.d(TAG, "正在配对......");
                        //回调

                        break;
                    case BluetoothDevice.BOND_BONDED://配对结束
                        Log.d(TAG, "完成配对");

                        break;
                    case BluetoothDevice.BOND_NONE://取消配对/未配对
                        Log.d(TAG, "取消配对");

                    default:
                        break;
                }
            }
            else if (TextUtils.equals(action, BluetoothDevice.ACTION_ACL_CONNECTED)) {
                Log.d(TAG, "设备建立连接");
                // int STATE_DISCONNECTED = 0; //未连接
                // int STATE_CONNECTING = 1; //连接中
                // int STATE_CONNECTED = 2; //连接成功
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e(TAG, device.getName() + "->" + device.getAddress() + "设备建立连接：" + device.getBondState());
                if (mDeviceConnectListener != null) {
                    mDeviceConnectListener.onDeviceConnect(device);
                }
                return;
            }
            else if (TextUtils.equals(action, BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Log.d(TAG, "设备断开连接");
                //获取蓝牙对象
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e(TAG, device.getName() + "->" + device.getAddress());
                if (mDeviceConnectListener != null) {
                    mDeviceConnectListener.onDeviceDisconnect(device);
                }
                return;
            }

        }
    };


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                isConnect = true;
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                isConnect = false;
                mBluetoothGatt = null;
                mBluetoothDevice = null;
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered gatt succeess received: " + status);
                //处理服务数据
                if (mBluetoothGatt != null) {
                    //获取指定类型的服务
                    BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(RX_SERVICE_UUID);
                    if (mBluetoothGattService != null) {
                        //获取服务中对应的读写characteristic
                        mWriteCharacteristic = mBluetoothGattService.getCharacteristic(RX_CHAR_UUID);
                        mReadCharacteristic = mBluetoothGattService.getCharacteristic(TX_CHAR_UUID);
                    }
                    else {
                        mBluetoothGatt.disconnect();
                        mBluetoothGatt.close();
                        mBluetoothGatt = null;
                        mBluetoothDevice = null;
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered gatt other received: " + status);
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (mReadCharacteristic != null) {
                    //指定的characteristic 读取数据
                    boolean b = mBluetoothGatt.readCharacteristic(mReadCharacteristic);
                    if (b) {
                        Log.e(TAG, "readBluetoohMsgFunction: 读取成功");
                        byte[] value = mReadCharacteristic.getValue();
                        Log.d(TAG, "ble -- " + "收到消息 " + new String(value));
                    } else {
                        Log.e(TAG, "readBluetoohMsgFunction: 读取失败");
                    }
                }
            }
            byte[] value = characteristic.getValue();

            Log.d(TAG, "ble -- " + "收到消息 " + new String(value));
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

        }
    };

    //写入BLE 消息
    public void writeToBleBluetooh(byte[] sendData) {
        if (mBluetoothGatt != null) {
            if (!isConnect) {
                Log.e("ble", "连接断开，重新连接");
                //连接已断开
                mBluetoothGatt.connect();
                return;
            }

            if (mWriteCharacteristic != null) {
                //设置数据
                mWriteCharacteristic.setValue(sendData);
                //写入
                mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
            }
        } else if (mBluetoothDevice != null) {
            Log.e("ble", "Gatt 为 null 重新创建");
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback);
        }
    }


    //蓝牙状态的监听接口
    public interface OnBluetoothStateListener {
        //打开
        void onBluetoothOpen();

        //关闭
        void onBluetoothClose();

        //将要打开
        void onBluetoothWillOpen();

        //将要关闭
        void onBluetoothWillClose();
    }

    private OnBluetoothStateListener mStateListener;

    public void setBluetoothStateListener(OnBluetoothStateListener stateListener) {
        mStateListener = stateListener;
    }

    //扫描到设备监听接口
    public interface OnBluetoothDeviceDiscoveryListener {
        //开始扫描
        void onBluetoothStart();

        //发现设备
        void onBluetoothDiscover(BluetoothDevice device);

        //扫描完成
        void onBluetoothDiscoverFinish();
    }

    private OnBluetoothDeviceDiscoveryListener mDeviceDiscoveryListener;

    public void setDeviceDiscoveryListener(OnBluetoothDeviceDiscoveryListener deviceDiscoveryListener) {
        mDeviceDiscoveryListener = deviceDiscoveryListener;
    }


    //蓝牙设备接入监听
    public interface OnBluetoothDeviceConnectListener {
        void onDeviceConnect(BluetoothDevice device);
        void onDeviceDisconnect(BluetoothDevice device);
    }

    private OnBluetoothDeviceConnectListener mDeviceConnectListener;

    public void setDeviceConnectListener(OnBluetoothDeviceConnectListener deviceConnectListener) {
        mDeviceConnectListener = deviceConnectListener;
    }

    //蓝牙设备连接监听
    public interface OnBluetoothOnBondConnectListener {
        void onBondConnectSuccess(BluetoothSocket bluetoothSocket);

        void onBondConnectFailes();
    }

    private OnBluetoothOnBondConnectListener mOnBondConnectListener;

    public void setOnBondConnectListener(OnBluetoothOnBondConnectListener onBondConnectListener) {
        mOnBondConnectListener = onBondConnectListener;
    }


    //蓝牙消息接收回调
    public interface OnBluetoothMsgListener {
        void onReciveMsg(String msg, BluetoothDevice remoteDevice);
    }

    private OnBluetoothMsgListener mOnBluetoothMsgListener;

    public void setOnBluetoothMsgListener(OnBluetoothMsgListener onBluetoothMsgListener) {
        mOnBluetoothMsgListener = onBluetoothMsgListener;
    }
}
