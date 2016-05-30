package com.hva.iot.ibeaconscan;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.*;
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

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private ScanFilter mScanFilter;
    private ScanSettings mScanSettings;

    private Button mStartScanButton;
    private TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        setScanFilter();
        setScanSettings();

        mStartScanButton = (Button)findViewById(R.id.startScanButton);
        mTextView = (TextView)findViewById(R.id.somethingTextView);

        mStartScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeScanner.startScan(Arrays.asList(mScanFilter), mScanSettings, mScanCallback);
            }
        });
    }

    private void setScanFilter() {
        ScanFilter.Builder mBuilder = new ScanFilter.Builder();
        ByteBuffer mManufacturerData = ByteBuffer.allocate(23);
        ByteBuffer mManufacturerDataMask = ByteBuffer.allocate(24);

        String s = "0CF052C2-97CA-407C-84F8-B62AAC4E9020";

        String s2 = s.replace("-", "");
        UUID uuidx = new UUID(
                new BigInteger(s2.substring(0, 16), 16).longValue(),
                new BigInteger(s2.substring(16), 16).longValue());

        byte[] uuid = getIdAsByte(uuidx);
        mManufacturerData.put(0, (byte)0xBE);
        mManufacturerData.put(1, (byte)0xAC);
        for (int i=2; i<=17; i++) {
            mManufacturerData.put(i, uuid[i-2]);
        }
        for (int i=0; i<=17; i++) {
            mManufacturerDataMask.put((byte)0x01);
        }
        mBuilder.setManufacturerData(224, mManufacturerData.array(), mManufacturerDataMask.array());
        mScanFilter = mBuilder.build();
    }

    private void setScanSettings() {
        ScanSettings.Builder mBuilder = new ScanSettings.Builder();
        mBuilder.setReportDelay(0);
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        mScanSettings = mBuilder.build();
    }

    protected ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ScanRecord mScanRecord = result.getScanRecord();
            IntBuffer intBuf =
                    ByteBuffer.wrap(mScanRecord.getManufacturerSpecificData(224))
                            .order(ByteOrder.BIG_ENDIAN)
                            .asIntBuffer();
            int[] array = new int[intBuf.remaining()];
            int[] manufacturerData = array;
            int mRssi = result.getRssi();
            int contents = result.describeContents();

            double accuracy = calculateDistance(contents, mRssi);
            String distance = getDistance(accuracy);
            mTextView.setText(distance);
        }

        public double calculateDistance(int txPower, double rssi) {
            if (rssi == 0) {
                return -1.0; // if we cannot determine accuracy, return -1.
            }
            double ratio = rssi*1.0/txPower;
            if (ratio < 1.0) {
                return Math.pow(ratio,10);
            }
            else {
                double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
                return accuracy;
            }
        }
    };



    private String getDistance(double accuracy) {
        if (accuracy == -1.0) {
            return "Unknown";
        } else if (accuracy < 1) {
            return "Immediate";
        } else if (accuracy < 3) {
            return "Near";
        } else {
            return "Far";
        }
    }

    public byte[] getIdAsByte(UUID uuid)
    {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

}