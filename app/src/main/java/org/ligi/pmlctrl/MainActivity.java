package org.ligi.pmlctrl;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
    private String ourId;
    private InetAddress IPAddress = null;

    class RCV implements Runnable {

        @Override
        public void run() {
            byte[] receiveData = new byte[1024];
            while (true) {

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    client_socket.receive(receivePacket);
                    final String modifiedSentence = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    Log.i("", "Rcv" + modifiedSentence);
                    if (modifiedSentence.startsWith("/uid/")) {
                        ourId = modifiedSentence.substring(5);
                        new Thread(new SND()).start();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class SND implements Runnable {

        @Override
        public void run() {
            while (true) {

                try {

                    //String str="/controller/"+ourId+"/ping/1338";
                    String str = "/controller/" + ourId + "/states/" + buttonStates;

                    byte[] send_data = str.getBytes(Charset.forName("UTF-8"));

                    DatagramPacket send_packet = new DatagramPacket(send_data, str.length(), IPAddress, 1338);
                    client_socket.send(send_packet);

                    Thread.sleep(50);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
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

                    String str = "/controller/new/1338";

                    if (client_socket == null) {
                        client_socket = new DatagramSocket(1338);
                        new Thread(new RCV()).start();
                    }

                    try {
                        IPAddress = InetAddress.getByName("151.217.202.192");

                        byte[] send_data = str.getBytes(Charset.forName("UTF-8"));

                        DatagramPacket send_packet = new DatagramPacket(send_data, str.length(), IPAddress, 1338);
                        client_socket.send(send_packet);

                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {

                        e.printStackTrace();
                    }

                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }

        }

        ).start();


    }

    @InjectView(R.id.up)
    ImageView up;

    @InjectView(R.id.down)
    ImageView down;

    @InjectView(R.id.left)
    ImageView left;

    @InjectView(R.id.right)
    ImageView right;

    @InjectView(R.id.container)
    ViewGroup container;

    String buttonStates = "00000000000000";
    String tempButtonStates = "00000000000000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        getWindow().getDecorView().findViewById(android.R.id.content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                tempButtonStates = "00000000000000";
                for (int i = 0; i < event.getPointerCount(); i++) {
                    checkView(up, event, 0, i);
                    checkView(down, event, 1, i);
                    checkView(left, event, 2, i);
                    checkView(right, event, 3, i);
                }

                buttonStates = tempButtonStates;
                Log.i("", buttonStates);
                return true;
            }
        });


    }

    private void checkView(View v, MotionEvent ev, int position, int index) {
        final Rect rect = new Rect();
        v.getHitRect(rect);

        final boolean contains = rect.contains((int) ev.getX(index) - ((ViewGroup) v.getParent()).getLeft(), (int) ev.getY(index) - ((ViewGroup) v.getParent()).getTop());

        if (contains) {
            if (ev.getAction() == MotionEvent.ACTION_UP) {
                tempButtonStates = tempButtonStates.substring(0, position) + "0" + tempButtonStates.substring(position + 1);
                return;
            }

            tempButtonStates = tempButtonStates.substring(0, position) + "1" + tempButtonStates.substring(position + 1);
        }
    }

}
