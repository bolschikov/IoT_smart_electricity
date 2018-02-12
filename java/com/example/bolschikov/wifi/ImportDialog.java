package com.example.bolschikov.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

//import android.app.Activity;
public class ImportDialog {

    Activity activity;
    AlertDialog dialog;
    AlertDialog.Builder builder;
    String detailProvader;
    boolean check_wifi = FALSE;




    TextView error;
    String strPassword1;
    boolean flag;

    public ImportDialog(Activity a, String detailProvader) {
        this.activity = a;
        this.detailProvader = detailProvader;
        builder = new AlertDialog.Builder(a);
    }

    public void showDialog(final LayoutInflater li, final WifiManager wifiManager, final Button b_cont) {

        View promptsView = li.inflate(R.layout.pswd, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.EditText_Pwd1);
        alertDialogBuilder.setCancelable(false).setNegativeButton("Go",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                /** DO THE METHOD HERE WHEN PROCEED IS CLICKED*/
                                check_wifi = FALSE;
                                String user_text = (userInput.getText()).toString();
                                WifiConfiguration conf = new WifiConfiguration();
                                conf.SSID = String.format("\"%s\"", detailProvader);
                                conf.preSharedKey = String.format("\"%s\"", user_text);
                                int networkId = wifiManager.getConnectionInfo().getNetworkId();
                                wifiManager.removeNetwork(networkId);
                                int netId = wifiManager.addNetwork(conf);
                                wifiManager.disconnect();
                                check_wifi = wifiManager.enableNetwork(netId, true);
                                wifiManager.reconnect();
                                if (!check_wifi) {
                                    Log.d(user_text,"string is empty");
                                    String message = "The password you have entered is incorrect." + " \n \n" + "Please try again!";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                    builder.setTitle("Error");
                                    builder.setMessage(message);
                                    builder.setPositiveButton("Cancel", null);
                                    builder.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            showDialog(li, wifiManager, b_cont);
                                        }
                                    });

                                    builder.create().show();
                                }
                                else{
                                    b_cont.setEnabled(TRUE);
                                }
                            }
                        })
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.dismiss();
                            }

                        }

                );

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }
}
