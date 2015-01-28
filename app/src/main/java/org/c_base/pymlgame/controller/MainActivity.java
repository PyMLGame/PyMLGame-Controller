package org.c_base.pymlgame.controller;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.os.Vibrator;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity {
    private DatagramSocket client_socket;
    private String uid;
    private InetAddress ipaddress = null;
    private int port = 1338;

    class Receiver implements Runnable {
        @Override
        public void run() {
            byte[] receiveData = new byte[1024];
            while(true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    client_socket.receive(receivePacket);
                    final String modifiedSentence = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    Log.i("PyMLGCtlr", "Receiver" + modifiedSentence);
                    if(modifiedSentence.startsWith("/uid/")) {
                        uid = modifiedSentence.substring(5);
                        new Thread(new Sender()).start();
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Sender implements Runnable {
        @Override
        public void run() {
            while(true) {
                try {
                    String cmd = "/controller/" + uid + "/states/" + buttonStates;
                    byte[] send_data = cmd.getBytes(Charset.forName("UTF-8"));
                    DatagramPacket send_packet = new DatagramPacket(send_data, cmd.length(), ipaddress, 1338);
                    client_socket.send(send_packet);
                    Thread.sleep(50);
                } catch(IOException e) {
                    e.printStackTrace();
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @OnClick(R.id.connect)
    void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                String str = "/controller/new/" + port;
                    if(client_socket == null) {
                        client_socket = new DatagramSocket(port);
                        new Thread(new Receiver()).start();
                    }
                    try {
                        ipaddress = InetAddress.getByName(ip.getText().toString());
                        byte[] send_data = str.getBytes(Charset.forName("UTF-8"));
                        DatagramPacket send_packet = new DatagramPacket(send_data, str.length(), ipaddress, port);
                        client_socket.send(send_packet);
                    } catch(UnknownHostException e) {
                        e.printStackTrace();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    @InjectView(R.id.ip)
    EditText ip;

    @InjectView(R.id.up)
    ImageView up;

    @InjectView(R.id.down)
    ImageView down;

    @InjectView(R.id.left)
    ImageView left;

    @InjectView(R.id.right)
    ImageView right;

    @InjectView(R.id.buttonA)
    ImageView buttonA;

    @InjectView(R.id.buttonB)
    ImageView buttonB;

    @InjectView(R.id.buttonX)
    ImageView buttonX;

    @InjectView(R.id.buttonY)
    ImageView buttonY;

    @InjectView(R.id.buttonStart)
    ImageView buttonStart;

    @InjectView(R.id.buttonSelect)
    ImageView buttonSelect;

    @InjectView(R.id.buttonMenu)
    ImageView buttonMenu;

    Vibrator vibrator;

    String buttonStates = "00000000000000";
    String tempButtonStates = "00000000000000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        ButterKnife.inject(this);

        getWindow().getDecorView().findViewById(android.R.id.content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            tempButtonStates = "00000000000000";
            for(int i = 0; i < event.getPointerCount(); i++) {
                checkView(up, event, 0, i);
                checkView(down, event, 1, i);
                checkView(left, event, 2, i);
                checkView(right, event, 3, i);
                checkView(buttonA, event, 4, i);
                checkView(buttonB, event, 5, i);
                checkView(buttonX, event, 6, i);
                checkView(buttonY, event, 7, i);
                checkView(buttonStart, event, 8, i);
                checkView(buttonSelect, event, 9, i);
                checkView(buttonMenu, event, 10, i);
                checkView(buttonMenu, event, 11, i);
                checkView(buttonMenu, event, 12, i);
                checkView(buttonMenu, event, 13, i);
            }
            buttonStates = tempButtonStates;
            Log.i("PyMLGCtlr", buttonStates);
            return true;
            }
        });
    }

    private void checkView(View v, MotionEvent ev, int position, int index) {
        final Rect rect = new Rect();
        v.getHitRect(rect);
        final boolean contains = rect.contains((int) ev.getX(index) - ((ViewGroup) v.getParent()).getLeft(), (int) ev.getY(index) - ((ViewGroup) v.getParent()).getTop());
        if(contains) {
            if(ev.getAction() == MotionEvent.ACTION_UP) {
                tempButtonStates = tempButtonStates.substring(0, position) + "0" + tempButtonStates.substring(position + 1);
            }
            else {
                if (vibrator.hasVibrator() && buttonStates.charAt(position) == '0') {
                    vibrator.vibrate(20);
                }
                tempButtonStates = tempButtonStates.substring(0, position) + "1" + tempButtonStates.substring(position + 1);
            }
        }
    }
}