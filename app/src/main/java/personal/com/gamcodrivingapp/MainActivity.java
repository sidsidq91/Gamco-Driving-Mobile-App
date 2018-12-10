package personal.com.gamcodrivingapp;
// todo add support for not already paired devices
// todo do some styling it looks like shit now
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private BluetoothAdapter mBluetoothAdapter;
    private static boolean isConnected=false;
    private static BluetoothConnectionService mBluetoothConnection;
    public static boolean Online;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private BluetoothDevice mBTDevice;

    private ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    public DeviceListAdapter mDeviceListAdapter;

    private ListView lvNewDevices;

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println(action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                showMsg("deviceFound");
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Online = true;
                isConnected = true;
                startActivity(new Intent(MainActivity.this,PlayGround.class));
                showMsg("device connected");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                showMsg("Done Searching");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                showMsg("device about to disconect");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                isConnected = false;
                showMsg("device disconnected");
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {

                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvNewDevices = findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        IntentFilter connectedFilter = new IntentFilter();
        connectedFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        connectedFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        connectedFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mBroadcastReceiver1, connectedFilter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        lvNewDevices.setOnItemClickListener(MainActivity.this);
        btnDiscover();
//        enableDisableBT();
    }

    private void btnDiscover() {

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();

            //check BT permissions in manifest
            //          checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){

            //check BT permissions in manifest
//            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mBluetoothAdapter.cancelDiscovery();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!isConnected) {
            mBluetoothAdapter.cancelDiscovery();

            String deviceName = mBTDevices.get(position).getName();
            String deviceAddress = mBTDevices.get(position).getAddress();

            //create the bond.
            //NOTE: Requires API 17+? I think this is JellyBean
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBTDevices.get(position).createBond();
                mBTDevice = mBTDevices.get(position);
                mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
            }
            startConnection();
        }else {
            startActivity(new Intent(MainActivity.this,PlayGround.class));
        }
    }


    public void startConnection(){
        startBTConnection(mBTDevice,MY_UUID_INSECURE);
    }

    /**
     * starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        mBluetoothConnection.startClient(device,uuid);
    }

    public static void sendDataOut(String data){
        byte[] dataToGo = data.getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(dataToGo);
    }

    public static String recieveData(){
        return mBluetoothConnection.incomingMessage;
    }

    public static void setIncomeAsZero(){
        mBluetoothConnection.incomingMessage = "0";
    }

    public void sendData(View view) {
        sendDataOut("helloWorld!");
    }

    public void showMsg(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    public void offlineMode(View view) {
        Online = false;
        startActivity(new Intent(MainActivity.this,PlayGround.class));
    }

    public void help(View view) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
        }
        builder.setTitle("راهنمای اتصال")
                .setMessage("هنگام وصل شدن برای اولین بار به منوی تنظیمات تلفن همراه خود رفته و تنظیمات بلوتوث را انتخاب کنید. در میان لیست دستگاه های موجود گزینه hc-05 را انتخاب کرده و درصورت نیاز به پسورد ۱۲۳۴ را وارد کنید. حال می توانید به نرم افزار بازگشته و به دستگاه متصل شوید")
                .setPositiveButton("خیلی خب!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_menu_help)
                .show();
    }

    public void exit(View view) {
        MainActivity.this.finishAffinity();
    }
//    public void enableBT(){
//        if(mBluetoothAdapter == null){
//
//        }
//        if(!mBluetoothAdapter.isEnabled()){
//            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivity(enableBTIntent);
//
//            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//            registerReceiver(mBroadcastReceiver1, BTIntent);
//        }
//    }
}
