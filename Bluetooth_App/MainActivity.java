package seniordesign.lucknell.com.seniordesign;


import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "bluetoothmodule2"; // for logging purposes

    private Button DataButton, GraphButton, bluetooth_connect_btn, settings;
    private Switch charging;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private BluetoothDevice device;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private TextView myLabel, myLabel2, txtCharge;
    private Thread workerThread, thread;
    private volatile boolean stopWorker;
    private boolean success = false, connected, listening = false, graphing = false, fake;
    private byte[] readBuffer;
    private int readBufferPosition, counter = 0, even = 0;
    private double x, y, y2, y3, volt;
    private String name;
    private Runnable rgraph;
    private Handler hgraph = new Handler();
    private ArrayList<String> graphdata = new ArrayList<String>();
    double fvolt = 7.1, fcurrent = 1204, fmilliwatt = 456;
    private GraphView graph, graph2, graph3;
    private Random rand = new Random();
    private LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>();
    private LineGraphSeries<DataPoint> series3 = new LineGraphSeries<DataPoint>();

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module
    private static String address = "00:18:EF:00:1F:00";

    public void stop() {
        hgraph.removeCallbacks(rgraph);
    }

    public void restart() {
        hgraph.removeCallbacks(rgraph);
        hgraph.postDelayed(rgraph, 300);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        name = sharedPref.getString("key_name", "Alex Summers");
        address = sharedPref.getString("1235", "00:18:EF:00:1F:00");
        fake = sharedPref.getBoolean("key_fake", false);
        validateMac(address);
        setContentView(R.layout.activity_main);
        DataButton = findViewById(R.id.dataBtn);
        GraphButton = findViewById(R.id.graphBtn);
        bluetooth_connect_btn = findViewById(R.id.connectBtn);
        settings = findViewById(R.id.settingsBtn);
        myLabel = findViewById(R.id.txtArduino);
        myLabel2 = findViewById(R.id.txtArduino2);
        txtCharge = findViewById(R.id.txtcharging);
        charging = findViewById(R.id.switch1);
        graph = (GraphView) findViewById(R.id.graph);
        graph2 = (GraphView) findViewById(R.id.graph2);
        graph3 = (GraphView) findViewById(R.id.graph3);
        charging.setVisibility(View.INVISIBLE);
        DataButton.setVisibility(View.INVISIBLE);
        GraphButton.setVisibility(View.INVISIBLE);

        makeGraph();
        counter++;

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        Toast.makeText(getBaseContext(), "Welcome Back " + name, Toast.LENGTH_LONG).show();
        ArrayList<String> s = new ArrayList<String>();
        for (BluetoothDevice bt : pairedDevices)
            s.add(bt.getName());


        checkBTState();
        chargingState();
        DataButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!listening) {
                    //sendData("1");
                    if (success) {
                        beginListenForData();
                        listening = true;
                        DataButton.setText("Reset Graphs");
                        Toast.makeText(getBaseContext(), "Checking for data...", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getBaseContext(), "Please connect first.", Toast.LENGTH_SHORT).show();
                } else {
                    DataButton.setText("Check for Data");
                    Toast.makeText(getBaseContext(), "Resetting Graphs", Toast.LENGTH_SHORT).show();
                    resetGraph();
                }
            }
        });

        GraphButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (success) {
                    if (!graphing) {
                        // GraphButton.setText("Stop Graphs");
                        graphing = true;
                        rgraph = new Runnable() {
                            @Override
                            public void run() {
                                generateData();
                                hgraph.postDelayed(this, 300);
                            }
                        };
                        hgraph.postDelayed(rgraph, 300);
                        GraphButton.setEnabled(false);
                        //GraphButton.setVisibility(View.INVISIBLE);
                        Toast.makeText(getBaseContext(), "Graph generated.", Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(getBaseContext(), "Please connect first.", Toast.LENGTH_SHORT).show();
            }
        });


        settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getBaseContext(), "Mac is " + address , Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, SettingsPrefActivity.class));
            }
        });

        bluetooth_connect_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BTinit()) {
                    //bluetooth_connect_btn.setEnabled(false);
                    bluetooth_connect_btn.setText("Connecting...");
                    bluetooth_connect_btn.setEnabled(false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BTconnect();
                        }
                    }, 100);

                    //bluetooth_connect_btn.setEnabled(true);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "...onResume - try connect...");
        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        if (BTinit() && success) {
            try {
                btSocket = createBluetoothSocket(device);
            } catch (IOException e1) {
                errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
            }
            // Discovery is resource intensive.  Make sure it isn't going on
            // when you attempt to connect and pass your message.
            btAdapter.cancelDiscovery();

            // Establish the connection.  This will block until it connects.
            Log.d(TAG, "...Connecting...");
            try {
                btSocket.connect();
                Log.d(TAG, "...Connection ok...");
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                }
            }
            // Create a data stream so we can talk to server.
            Log.d(TAG, "...Create Socket...");

            try {
                mmOutputStream = btSocket.getOutputStream();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");
        if (BTinit() && success) {
            if (mmOutputStream != null) {
                try {
                    mmOutputStream.flush();
                } catch (IOException e) {
                    errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
                }
            }

            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
            }
        }
    }

    private  void resetGraph()
    {
        listening = false;
        stopWorker = true;
        x = 0;
        y = 0;
        y3 = 0;
        y2 = 0;
        series = new LineGraphSeries<DataPoint>();
        series2 = new LineGraphSeries<DataPoint>();
        series3 = new LineGraphSeries<DataPoint>();
        graph.removeAllSeries();
        graph2.removeAllSeries();
        graph3.removeAllSeries();
        graphdata.clear();
        makeGraph();
    }

    private void chargingState() {

        final boolean stopThread = false;
        thread = new Thread(new Runnable() {
            public void run() {
                while (!thread.isInterrupted() && !stopThread) if (charging.isChecked()) {
                    sendData("W");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    sendData("G");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

    }

    private void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        //Toast.makeText(getBaseContext(), fake+ "", Toast.LENGTH_SHORT).show();
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable;
                        try {
                            bytesAvailable = mmInputStream.available();
                        } catch (NullPointerException ex) {
                            bytesAvailable = 0;
                        }
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    final String[] dataset;
                                    if (fake) {
                                        even = rand.nextInt(7);
                                        if (even % 2 == 0)
                                            fvolt += 0.000009;
                                        else
                                            fvolt -= 0.000009;
                                        if (even % 4 == 0)
                                            fcurrent += 0.00037;
                                        else
                                            fcurrent -= 0.00037;
                                        if (even % 6 == 0)
                                            fmilliwatt += 0.0000043;
                                        else
                                            fmilliwatt -= 0.0000043;
                                    }
                                    if (!fake) {
                                        dataset = data.split(",");
                                        graphdata.add(data);
                                    } else {
                                        String temp = "1," + fvolt + "," + fcurrent + "," + fmilliwatt;
                                        dataset = temp.split(",");
                                        graphdata.add(temp);
                                    }

                                    readBufferPosition = 0;

                                    volt = Double.parseDouble(dataset[1]);
                                    final double per = map(volt, 6.02, 7.4, 0, 100);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            chargingStatus(dataset[2], dataset[3]);
                                        }
                                    });
                                    handler.post(new Runnable() {
                                        public void run() {
                                            String status;
                                            if (dataset[0].equals("0"))
                                                status = "Disconnected";
                                            else
                                                status = "Connected";
                                            String display = "Status: \nVoltage:\nCurrent:\nmilliWatt:\nPercentage :";
                                            String display2 = status +
                                                    "\n" + String.format("%.2f", Math.abs(Double.parseDouble(dataset[1]))) + "V\n" +
                                                    String.format("%.2f", Math.abs(Double.parseDouble(dataset[2]))) + "mA\n" +
                                                    String.format("%.2f", Math.abs(Double.parseDouble(dataset[3]))) + "mWh\n" +
                                                    String.format("%.2f", per) + "%";
                                            myLabel2.setText(display2);
                                            myLabel.setText(display);
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }


    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    public boolean BTinit() {
        boolean found = false;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) //Checks if the device supports bluetooth
        {
            Toast.makeText(getApplicationContext(), "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
        }

        if (!bluetoothAdapter.isEnabled()) //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        if (bondedDevices.isEmpty()) //Checks for paired bluetooth devices
        {
            Toast.makeText(getApplicationContext(), "Please pair the device first or enable Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
                if (iterator.getAddress().equals(address)) {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    private void generateData() {
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setFixedPosition(1, 1);
        graph2.getLegendRenderer().setVisible(true);
        graph2.getLegendRenderer().setFixedPosition(1, 1);
        graph3.getLegendRenderer().setVisible(true);
        graph3.getLegendRenderer().setFixedPosition(1, 1);
        if (!fake) {
            for (int i = 0; i < graphdata.size(); i++) {
                x = x + 0.5;
                y = Double.parseDouble((graphdata.get(i).split(","))[3]);
                series.appendData(new DataPoint(x, y), true, 10);
                y2 = Double.parseDouble((graphdata.get(i).split(","))[2]);
                series2.appendData(new DataPoint(x, y2), true, 10);
                y3 = Double.parseDouble((graphdata.get(i).split(","))[1]);
                series3.appendData(new DataPoint(x, y3), true, 10);

            }
        } else {
            for (int i = 0; i < graphdata.size(); i++) {
                x = x + 0.5;
                y = Double.parseDouble((graphdata.get(i).split(","))[3]);
                series.appendData(new DataPoint(x, y), true, 10);
                y2 = Double.parseDouble((graphdata.get(i).split(","))[2]);
                series2.appendData(new DataPoint(x, y2), true, 10);
                y3 = Double.parseDouble((graphdata.get(i).split(","))[1]);
                series3.appendData(new DataPoint(x, y3), true, 10);

            }

        }
        graphdata.clear();
        graph.removeAllSeries();
        graph2.removeAllSeries();
        graph3.removeAllSeries();
        graph.addSeries(series);
        graph2.addSeries(series2);
        graph3.addSeries(series3);
        if (x - 5.5 < 0) {
            graph.getViewport().setMinX(0);
            graph2.getViewport().setMinX(0);
            graph3.getViewport().setMinX(0);

        } else {
            graph.getViewport().setMinX(x - 5.5);
            graph2.getViewport().setMinX(x - 5.5);
            graph3.getViewport().setMinX(x - 5.5);
        }
        graph.getViewport().setMaxX(x + 1.5);
        graph.getViewport().setXAxisBoundsManual(true);
        graph = (GraphView) findViewById(R.id.graph);
        graph2.getViewport().setMaxX(x + 1.5);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2 = (GraphView) findViewById(R.id.graph2);
        graph3.getViewport().setMaxX(x + 1.5);
        graph3.getViewport().setXAxisBoundsManual(true);
        graph3 = (GraphView) findViewById(R.id.graph3);

        if(x > 650) {
            resetGraph();
            beginListenForData();
        }
    }

    private boolean validateMac(String mac) {
        String[] addr = mac.split(":");
        if (addr.length != 6) {
            Toast.makeText(getBaseContext(), "Mac address from the settings" +
                    " was entered wrong. Using Default", Toast.LENGTH_LONG).show();
            address = "00:18:EF:00:1F:00";
        }
        for (String s : addr)
            if (s.length() != 2) {
                Toast.makeText(getBaseContext(), "Mac address from the settings" +
                        " was entered wrong. Using Default", Toast.LENGTH_LONG).show();
                address = "00:18:EF:00:1F:00";
            }
        return true;
    }

    private void makeGraph() {
        graph = (GraphView) findViewById(R.id.graph);
        graph2 = (GraphView) findViewById(R.id.graph2);
        graph3 = (GraphView) findViewById(R.id.graph3);

        series.setColor(Color.RED);
        series3.setColor(Color.GREEN);
        series.setDrawDataPoints(true);
        series2.setDrawDataPoints(true);
        series3.setDrawDataPoints(true);
        series.setTitle("Milliwatt");
        series2.setTitle("Current");
        series3.setTitle("Voltage");
        graph.setTitle("MilliWatts");
        graph2.setTitle("Current");
        graph3.setTitle("Voltage");

        if (counter < 1) {
            x = 0.0;
        }
    }

    public boolean BTconnect() {
        connected = true;
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID); //Creates a socket to handle the outgoing connection
            btSocket.connect();
            mmInputStream = btSocket.getInputStream();
            mmOutputStream = btSocket.getOutputStream();
            success = true;
            charging.setVisibility(View.VISIBLE);
            DataButton.setVisibility(View.VISIBLE);
            GraphButton.setVisibility(View.VISIBLE);
            bluetooth_connect_btn.setEnabled(true);
            bluetooth_connect_btn.setText("Disconnect");
            Toast.makeText(getApplicationContext(),
                    "Connection to bluetooth device successful", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;

            DataButton.setVisibility(View.INVISIBLE);
            GraphButton.setVisibility(View.INVISIBLE);
            charging.setVisibility(View.INVISIBLE);
            bluetooth_connect_btn.setEnabled(true);
            bluetooth_connect_btn.setText("Connect");
        }

        if (connected) {
            try {
                mmOutputStream = btSocket.getOutputStream(); //gets the output stream of the socket
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return connected;
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }


    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();
        if (connected && success) {
            Log.d(TAG, "...Send data: " + message + "...");

            try {
                mmOutputStream.write(msgBuffer);
            } catch (IOException e) {
                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                // errorExit("Fatal Error", msg);
            }
        }
    }

    public void chargingStatus(String current, String mW) {

        if (Math.abs(Double.parseDouble(current)) > 35 && Math.abs(Double.parseDouble(mW)) > 270 ) {
             txtCharge.setText("Charging");
        } else {
            txtCharge.setText("Discharging");

        }

    }

    public double map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

}
