package prototype.control.drone.com.dr_contr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARDeviceControllerStreamListener;
import com.parrot.arsdk.arcontroller.ARFeatureARDrone3;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;
import com.parrot.arsdk.arsal.ARSALPrint;

import org.w3c.dom.Text;

import java.util.List;

public class ControlActivity extends AppCompatActivity implements ARDiscoveryServicesDevicesListUpdatedReceiverDelegate , ARDeviceControllerListener {


    private List<ARDiscoveryDeviceService> deviceList;

    private ARDiscoveryServicesDevicesListUpdatedReceiver mArdiscoveryServicesDevicesListUpdatedReceiver;
    private ARDiscoveryDevice device;
    private Button searchDeviceButton;
    private Button landButton;
    private Button takeOffButton;
    private Button backButton;
    private Button leftButton;
    private Button rightButton;
    private Button forwardButton;
    private Button flyLeftButton;
    private Button flyRightButton;
    private Button upButton;
    private Button downButton;
    private boolean firstTakeOff = true;



    private ProgressBar batteryLevel;

    private ARDeviceController deviceController;
    private ARDiscoveryDevice drone;

    private ControlActivity main = null;

    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        this.main = this;
        ARSDK.loadSDKLibs();
        setUpButtons();


    }

    private void setUpButtons()
    {

        Button stopButton = (Button)findViewById(R.id.stopButon);
        if(stopButton != null) {
            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 0);
                    deviceController.getFeatureARDrone3().setPilotingPCMDPitch((byte) 0);
                }
            });
        }


        this.upButton = (Button)findViewById(R.id.upButton);

        this.upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(deviceController != null) {
                        deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 1);
                        deviceController.getFeatureARDrone3().setPilotingPCMDGaz((byte) 50);
                    }
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 0);
                    deviceController.getFeatureARDrone3().setPilotingPCMDGaz((byte) 0);
                }
                return false;
            }
        });

        this.downButton = (Button)findViewById(R.id.downButton);
        this.downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(deviceController != null) {
                        deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 1);
                        deviceController.getFeatureARDrone3().setPilotingPCMDGaz((byte) -50);
                    }
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 0);
                    deviceController.getFeatureARDrone3().setPilotingPCMDGaz((byte) 0);
                }
                return false;
            }
        });
        this.forwardButton = (Button)findViewById(R.id.forwardButton);
        this.forwardButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(deviceController != null) {
                        deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 1);
                        deviceController.getFeatureARDrone3().setPilotingPCMDPitch((byte) 50);
                    }
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 0);
                    deviceController.getFeatureARDrone3().setPilotingPCMDPitch((byte) 0);
                }
                return false;
            }
        });

        this.backButton = (Button)findViewById(R.id.backButton);
        this.backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (deviceController != null) {
                        deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 1);
                        deviceController.getFeatureARDrone3().setPilotingPCMDPitch((byte) -50);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 0);
                    deviceController.getFeatureARDrone3().setPilotingPCMDPitch((byte) 0);
                }
                return false;
            }
        });


        this.flyLeftButton = (Button)findViewById(R.id.flyLeftButton);
        this.flyLeftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (deviceController != null) {
                        deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 1);
                        deviceController.getFeatureARDrone3().setPilotingPCMDRoll((byte)-50);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 0);
                    deviceController.getFeatureARDrone3().setPilotingPCMDRoll((byte) 0);
                }
                return false;
            }
        });

        this.flyRightButton = (Button)findViewById(R.id.flyRightButton);
        this.flyRightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (deviceController != null) {
                        deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 1);
                        deviceController.getFeatureARDrone3().setPilotingPCMDRoll((byte)50);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    deviceController.getFeatureARDrone3().setPilotingPCMDFlag((byte) 0);
                    deviceController.getFeatureARDrone3().setPilotingPCMDRoll((byte) 0);
                }
                return false;
            }
        });

        this.leftButton = (Button)findViewById(R.id.leftButton);
        this.leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (deviceController != null) {
                        deviceController.getFeatureARDrone3().setPilotingPCMDYaw((byte) -50);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    deviceController.getFeatureARDrone3().setPilotingPCMDYaw((byte) 0);
                }
                return false;
            }
        });

        this.rightButton = (Button) findViewById(R.id.rightButton);
        this.rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (deviceController != null) {
                        deviceController.getFeatureARDrone3().setPilotingPCMDYaw((byte) 50);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    deviceController.getFeatureARDrone3().setPilotingPCMDYaw((byte) 0);
                }
                return false;
            }
        });


        this.landButton = (Button) findViewById(R.id.landButton);
        landButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                land();
            }
        });


        this.takeOffButton = (Button) findViewById(R.id.takeOffButton);
        this.takeOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(firstTakeOff) {
                    try {
                        firstTakeOff = false;
                        deviceController = new ARDeviceController(device);
                        deviceController.addListener(main);
                        deviceController.addListener(main);
                        ARCONTROLLER_ERROR_ENUM error = deviceController.start();
                        deviceController.getFeatureARDrone3().sendMediaStreamingVideoEnable((byte)1);


                    } catch (ARControllerException e) {
                        e.printStackTrace();
                    }

                }



                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(getPilotingState() == ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED)                  {
                    Toast.makeText(main, "In landed mode", Toast.LENGTH_SHORT).show();

                    takeoff();

                }
                else {
                    Toast.makeText(main, "done not in landed mode ", Toast.LENGTH_SHORT).show();
                }



            }
        });


        Button searchBtn = (Button) findViewById(R.id.searchDeviceButton);
        if (searchBtn != null) {
            searchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    initDiscoveryService();
                    registerReceivers();
                }
            });
        }
    }

    private void unregisterReceivers() {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());

        localBroadcastMgr.unregisterReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver);
    }

    private void closeServices() {

        if (mArdiscoveryService != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mArdiscoveryService.stop();

                    getApplicationContext().unbindService(mArdiscoveryServiceConnection);
                    mArdiscoveryService = null;
                }
            }).start();
        }
    }

    private ARDiscoveryService mArdiscoveryService;
    private ServiceConnection mArdiscoveryServiceConnection;


    private void land() {
        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = getPilotingState();
        if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING.equals(flyingState) ||
                ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING.equals(flyingState)) {
            ARCONTROLLER_ERROR_ENUM error = deviceController.getFeatureARDrone3().sendPilotingLanding();

            if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK)) {
                Toast.makeText(main, "Error during landing command", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM getPilotingState() {
        ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.eARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_UNKNOWN_ENUM_VALUE;
        if (deviceController != null) {
            try {
                ARControllerDictionary dict = deviceController.getCommandElements(ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED);
                if (dict != null) {
                    ARControllerArgumentDictionary<Object> args = dict.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                    if (args != null) {
                        Integer flyingStateInt = (Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE);
                        flyingState = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(flyingStateInt);
                    }
                }
            } catch (ARControllerException e) {
                e.printStackTrace();
            }

            return flyingState;
        }
        return null;
    }

    private void takeoff() {
        if (ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED.equals(getPilotingState())) {
            ARCONTROLLER_ERROR_ENUM error = deviceController.getFeatureARDrone3().sendPilotingTakeOff();

            if (!error.equals(ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK)) {

            }
        }
    }


    private void initDiscoveryService() {
        // create the service connection
        if (mArdiscoveryServiceConnection == null) {
            mArdiscoveryServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mArdiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();

                    startDiscovery();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mArdiscoveryService = null;
                }
            };
        }

        if (mArdiscoveryService == null) {
            // if the discovery service doesn't exists, bind to it
            Intent i = new Intent(getApplicationContext(), ARDiscoveryService.class);
            getApplicationContext().bindService(i, mArdiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // if the discovery service already exists, start discovery
            startDiscovery();
        }
    }

    private void startDiscovery() {
        if (mArdiscoveryService != null) {
            mArdiscoveryService.start();
        }
    }


    private void registerReceivers() {
        mArdiscoveryServicesDevicesListUpdatedReceiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(this);
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.registerReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }

    @Override
    public void onServicesDevicesListUpdated() {


        if (mArdiscoveryService != null) {
            List<ARDiscoveryDeviceService> deviceList = mArdiscoveryService.getDeviceServicesArray();


            if (deviceList != null && deviceList.size() > 0) {

                TextView deviceTxtV = (TextView) findViewById(R.id.deviceTextView);
                deviceTxtV.setText(deviceList.get(0).getName());



                this.device = createDiscoveryDevice(deviceList.get(0));

                if (device != null) {

                    Toast.makeText(main, "Found a valid device " + device.toString(), Toast.LENGTH_SHORT).show();
                    unregisterReceivers();
                    closeServices();
                } else {
                    Toast.makeText(main, "Device is null" + device.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    private ARDiscoveryDevice createDiscoveryDevice(ARDiscoveryDeviceService service) {
        ARDiscoveryDevice device = null;
        if ((service != null) &&
                (ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_ARDRONE.equals(ARDiscoveryService.getProductFromProductID(service.getProductID())))) {
            try {
                device = new ARDiscoveryDevice();

                ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) service.getDevice();

                device.initWifi(ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_ARDRONE, netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
            } catch (ARDiscoveryException e) {
                e.printStackTrace();

            }
        }

        return device;
    }

    @Override
    public void onStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error) {
        switch (newState) {
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                break;
            case ARCONTROLLER_DEVICE_STATE_STARTING:
                break;
            case ARCONTROLLER_DEVICE_STATE_STOPPING:
                break;

            default:
                break;
        }
    }

    @Override
    public void onExtensionStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error) {

    }

    @Override
    public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary) {


        if (elementDictionary != null) {
            if (commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED) {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    Integer flyingStateInt = (Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE);
                    ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM flyingState = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(flyingStateInt);
                }
            }

        }
    }




}



