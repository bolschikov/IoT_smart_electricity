package com.example.bolschikov.wifi;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class ReceiveMessageActivity extends Activity {

    private Socket socket;
    private static final String SERVER_IP = "192.168.10.1";
    private static final int SERVERPORT = 5555;
    Button b_tostart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_message);
        b_tostart = (Button) findViewById(R.id.b_tostart);
        b_tostart.setEnabled(FALSE);
        new Thread(new ClientThread()).start();

    }

    public void onClick(View view) {
        try {
            Spinner dev = (Spinner)findViewById(R.id.devices);
            String choice = String.valueOf(dev.getSelectedItem());
            int num_dev = 0;
            switch(choice){
                case "microwave oven": num_dev = 0;
                    break;
                case "refrigerator": num_dev = 1;
                    break;
                case "electric kettle": num_dev = 2;
                    break;
                case "iron": num_dev = 3;
                    break;
                case "electric radiator": num_dev = 4;
                    break;
                case "electric stove": num_dev = 5;
            }
            PrintWriter out_dev = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out_dev.println(num_dev);
            EditText et = (EditText) findViewById(R.id.sendingData);
            String str = et.getText().toString();
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            int result = Integer.parseInt(str);
            //int size = sizeOf(result);
            out.println(result);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        b_tostart.setEnabled(TRUE);
            b_tostart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ReceiveMessageActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}