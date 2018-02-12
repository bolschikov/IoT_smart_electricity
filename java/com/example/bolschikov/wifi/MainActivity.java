package com.example.bolschikov.wifi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends Activity implements OnClickListener {
    Button setWifi;
    Button b_continue;
    WifiManager wifiManager;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    List<String> listOfProvider;
    ListAdapter adapter;
    ListView listViwProvider;
    LayoutInflater inflater;
    //String pswd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listOfProvider = new ArrayList<String>();

		/*setting the resources in class*/
        listViwProvider = (ListView) findViewById(R.id.list_view_wifi);
        setWifi = (Button) findViewById(R.id.btn_wifi);
        b_continue = (Button) findViewById(R.id.b_continue);
        b_continue.setEnabled(FALSE);

        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final View layout = inflater.inflate(R.layout.pswd, (ViewGroup) findViewById(R.id.root));
//        final TextView pswd_after_dialog = (TextView) findViewById(R.id.textView);
//        final TextView ssid_after_dialog = (TextView) findViewById(R.id.textView2);

        setWifi.setOnClickListener(this);
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		/*checking wifi connection
		 * if wifi is on searching available wifi provider*/
        if (wifiManager.isWifiEnabled() == true) {
            setWifi.setText("ON");
            scaning();
        }
		/*opening a detail dialog of provider on click   */
        listViwProvider.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ImportDialog action = new ImportDialog(MainActivity.this, (wifiList.get(position)).SSID.toString());
                action.showDialog(inflater, wifiManager, b_continue);
            }
        });
        clc();
    }

    public void clc(){
        b_continue.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, ReceiveMessageActivity.class);
                startActivity(intent);
            }
        });

    }

    private void scaning() {
        // wifi scaned value broadcast receiver
        receiverWifi = new WifiReceiver();
        // Register broadcast receiver
        // Broacast receiver will automatically call when number of wifi
        // connections changed
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();

    }

    /*setting the functionality of ON/OFF button*/
    @Override
    public void onClick(View arg0) {
		/* if wifi is ON set it OFF and set button text "OFF" */
        if (wifiManager.isWifiEnabled() == true) {
            wifiManager.setWifiEnabled(false);
            setWifi.setText("OFF");
            listViwProvider.setVisibility(ListView.GONE);
        }
		/* if wifi is OFF set it ON
		 * set button text "ON"
		 * and scan available wifi provider*/
        else if (wifiManager.isWifiEnabled() == false) {
            wifiManager.setWifiEnabled(true);
            setWifi.setText("ON");
            listViwProvider.setVisibility(ListView.VISIBLE);
            scaning();
        }
    }


    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiverWifi);
    }

    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            wifiList = wifiManager.getScanResults();

			/* sorting of wifi provider based on level */
            Collections.sort(wifiList, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult lhs, ScanResult rhs) {
                    return (lhs.level > rhs.level ? -1
                            : (lhs.level == rhs.level ? 0 : 1));
                }
            });
            listOfProvider.clear();
            String providerName;
            for (int i = 0; i < wifiList.size(); i++) {
				/* to get SSID and BSSID of wifi provider*/
                providerName = (wifiList.get(i).SSID).toString()
                        +"\n"+(wifiList.get(i).BSSID).toString();
                listOfProvider.add(providerName);
            }
			/*setting list of all wifi provider in a List*/
            adapter = new ListAdapter(MainActivity.this, listOfProvider);
            listViwProvider.setAdapter(adapter);

            adapter.notifyDataSetChanged();
        }
    }
}
