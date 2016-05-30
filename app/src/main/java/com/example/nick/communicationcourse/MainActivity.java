package com.example.nick.communicationcourse;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyActivity";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private ScanFilter mScanFilter;
    private ScanSettings mScanSettings;

    private Button mStartScanButton;
    private TextView mUUIDTitleTextview;
    private TextView mUUIDFieldTextview;
    private TextView mMajorTitleTextview;
    private TextView mMajorFieldTextview;
    private TextView mMinorTitleTextview;
    private TextView mMinorFieldTextview;

    private final static int REQUEST_ENABLE_BT = 1;

    private final static int REQUEST_PERMISSION_STATIC = 1;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_STATIC);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mUUIDTitleTextview = (TextView)findViewById(R.id.uuidTitle);
        mUUIDFieldTextview = (TextView)findViewById(R.id.uuidField);

        mMajorTitleTextview = (TextView)findViewById(R.id.majorTitle);
        mMajorFieldTextview = (TextView)findViewById(R.id.majorField);

        mMinorTitleTextview = (TextView)findViewById(R.id.minorTitle);
        mMinorFieldTextview = (TextView)findViewById(R.id.minorField);

        /**
         * Checks if Bluetooth is enabled on device
         * Use this within and Activity
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
        }


        mStartScanButton = (Button)findViewById(R.id.startScanButton);

        mStartScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                }, 5000);
            }
        });
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            /**
             * We'll look at this bit in a minute
             */
            int startByte = 2;
            boolean patternFound = false;
            while (startByte <= 5) {
                if (    ((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                        ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                    patternFound = true;
                    break;
                }
                startByte++;
            }

            if (patternFound) {
                //Convert to hex String
                byte[] uuidBytes = new byte[16];
                System.arraycopy(scanRecord, startByte+4, uuidBytes, 0, 16);
                String hexString = bytesToHex(uuidBytes);

                //Here is your UUID
                String uuid =  hexString.substring(0,8) + "-" +
                        hexString.substring(8,12) + "-" +
                        hexString.substring(12,16) + "-" +
                        hexString.substring(16,20) + "-" +
                        hexString.substring(20,32);

                mUUIDFieldTextview.setText(uuid);

                //Here is your Major value
                int major = (scanRecord[startByte+20] & 0xff) * 0x100 + (scanRecord[startByte+21] & 0xff);

                mMajorFieldTextview.setText(major);

                //Here is your Minor value
                int minor = (scanRecord[startByte+22] & 0xff) * 0x100 + (scanRecord[startByte+23] & 0xff);

                mMinorFieldTextview.setText(minor);
            }
        }
    };



    /**
     * bytesToHex method
     * Found on the internet
     * http://stackoverflow.com/a/9855338
     */
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

//        private void setScanFilter() {
//            ScanFilter.Builder mBuilder = new ScanFilter.Builder();
//            ByteBuffer mManufacturerData = ByteBuffer.allocate(23);
//            ByteBuffer mManufacturerDataMask = ByteBuffer.allocate(24);
//
//            String s = "0CF052C2-97CA-407C-84F8-B62AAC4E9020";
//
//            String s2 = s.replace("-", "");
//            UUID uuidx = new UUID(
//                    new BigInteger(s2.substring(0, 16), 16).longValue(),
//                    new BigInteger(s2.substring(16), 16).longValue());
//
//            byte[] uuid = getIdAsByte(uuidx);
//            mManufacturerData.put(0, (byte)0xBE);
//            mManufacturerData.put(1, (byte)0xAC);
//            for (int i=2; i<=17; i++) {
//                mManufacturerData.put(i, uuid[i-2]);
//            }
//            for (int i=0; i<=17; i++) {
//                mManufacturerDataMask.put((byte)0x01);
//            }
//            mBuilder.setManufacturerData(224, mManufacturerData.array(), mManufacturerDataMask.array());
//            mScanFilter = mBuilder.build();
//        }
//
//        private void setScanSettings() {
//            ScanSettings.Builder mBuilder = new ScanSettings.Builder();
//            mBuilder.setReportDelay(0);
//            mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
//            mScanSettings = mBuilder.build();
//        }
//
//        protected ScanCallback mScanCallback = new ScanCallback() {
//            @Override
//            public void onScanResult(int callbackType, ScanResult result) {
//                ScanRecord mScanRecord = result.getScanRecord();
//                IntBuffer intBuf =
//                        ByteBuffer.wrap(mScanRecord.getManufacturerSpecificData(224))
//                                .order(ByteOrder.BIG_ENDIAN)
//                                .asIntBuffer();
//                int[] array = new int[intBuf.remaining()];
//                int[] manufacturerData = array;
//                int mRssi = result.getRssi();
//                int contents = result.describeContents();
//
//                Log.i(TAG, "" + mRssi);
//
//
//                double accuracy = calculateDistance(contents, mRssi);
//                String distance = getDistance(accuracy);
//                mTextView.setText("" + mRssi);
//            }
//
//            public double calculateDistance(int txPower, double rssi) {
//                if (rssi == 0) {
//                    return -1.0; // if we cannot determine accuracy, return -1.
//                }
//                double ratio = rssi*1.0/txPower;
//                if (ratio < 1.0) {
//                    return Math.pow(ratio,10);
//                }
//                else {
//                    double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
//                    return accuracy;
//                }
//            }
//        };
//
//
//
//    private String getDistance(double accuracy) {
//        if (accuracy == -1.0) {
//            return "Unknown";
//        } else if (accuracy < 1) {
//            return "Immediate";
//        } else if (accuracy < 3) {
//            return "Near";
//        } else {
//            return "Far";
//        }
//    }
//
//    public byte[] getIdAsByte(UUID uuid)
//    {
//        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
//        bb.putLong(uuid.getMostSignificantBits());
//        bb.putLong(uuid.getLeastSignificantBits());
//        return bb.array();
//    }

}

