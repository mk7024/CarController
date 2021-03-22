package com.mk7024.carcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private final String DEVICE_ADDRESS = "1C:BF:C0:1C:39:14"; //连接的设备的蓝牙MAC地址
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //蓝牙串口服务专用UUID

    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    final String stopValue = "0";
    final String northValue = "1";
    final String southValue = "2";
    final String westValue = "3";
    final String eastValue = "4";
    private TextView isConnected;

    Button north_button, south_button, east_button, west_button, connect_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        north_button = (Button) findViewById(R.id.north);
        north_button.setOnClickListener(this);
        north_button.setOnTouchListener(this);
        south_button = (Button) findViewById(R.id.south);
        south_button.setOnClickListener(this);
        south_button.setOnTouchListener(this);
        east_button = (Button) findViewById(R.id.east);
        east_button.setOnClickListener(this);
        east_button.setOnTouchListener(this);
        west_button = (Button) findViewById(R.id.west);
        west_button.setOnClickListener(this);
        west_button.setOnTouchListener(this);
        connect_button = (Button) findViewById(R.id.connect);
        connect_button.setOnClickListener(this);
        isConnected = (TextView) findViewById(R.id.isConnected);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.connect && device == null) {
            Toast.makeText(getApplicationContext(), "请先连接设备!", Toast.LENGTH_SHORT).show();
        }


        switch (v.getId()) {
            case R.id.connect:
                if(outputStream != null){
                    Toast.makeText(getApplicationContext(),"你已经连接成功!",Toast.LENGTH_SHORT).show();
                    return;
                }
                isConnected.setText("连接中...");
                isConnected.setTextColor(Color.CYAN);
                if(InitConnection()){
                    connect();
                }
                break;
                default:

        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.connect) {
            return false;
        }
        if (event.getAction() == ACTION_UP) {
            sendCodes(stopValue);
            return true;
        }
        switch (v.getId()) {
            case R.id.north:
                if(event.getAction() == ACTION_DOWN){
                    sendCodes(northValue);
                }
                break;
            case R.id.south:
                if(event.getAction() == ACTION_DOWN){
                    sendCodes(southValue);
                }
                break;
            case R.id.east:
                if(event.getAction() == ACTION_DOWN){
                    sendCodes(eastValue);
                }
                break;
            case R.id.west:
                if(event.getAction() == ACTION_DOWN){
                    sendCodes(westValue);
                }
                break;
        }
        return true;
    }

    private void sendCodes(String values) {
        try {

            switch (values) {
                case northValue:
                    String test = northValue + " " + System.currentTimeMillis();
                    outputStream.write(test.getBytes());
//                    outputStream.write(northValue.getBytes());
                    break;
                case southValue:
                    outputStream.write(southValue.getBytes());
                    break;
                case eastValue:
                    outputStream.write(eastValue.getBytes());
                    break;
                case westValue:
                    outputStream.write(westValue.getBytes());
                    break;
                case stopValue:
                    outputStream.write(stopValue.getBytes());
                    break;
            }
            outputStream.flush();
            isConnected.setText("发送:" + values);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean InitConnection(){
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            Toast.makeText(getApplicationContext(), "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return found;
        }
        if(!bluetoothAdapter.isEnabled()){
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter,0);
            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty()) //Checks for paired bluetooth devices
        {
            Toast.makeText(getApplicationContext(), "请先配对设备", Toast.LENGTH_SHORT).show();
        }
        else
        {
            for(BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }
        isConnected.setText("设备" + found);
        return found;
    }
    private boolean connect(){
        boolean connected = true;
        isConnected.setText("连接中");
        try
        {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID); //Creates a socket to handle the outgoing connection
            socket.connect();

            isConnected.setTextColor(Color.GREEN);
            isConnected.setText("连接成功");
        }
        catch(IOException e)
        {
            e.printStackTrace();
            connected = false;
        }

        if(connected)
        {
            try
            {
                outputStream = socket.getOutputStream(); //gets the output stream of the socket
                isConnected.setText("可发送信息");
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }


        return connected;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }