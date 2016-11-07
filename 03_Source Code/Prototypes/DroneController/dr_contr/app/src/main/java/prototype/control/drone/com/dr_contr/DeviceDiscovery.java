package prototype.control.drone.com.dr_contr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.util.List;
import java.util.Observable;

public class DeviceDiscovery extends AppCompatActivity implements ARDiscoveryServicesDevicesListUpdatedReceiverDelegate {

    private ARDiscoveryService mArdiscoveryService;
    private ServiceConnection mArdiscoveryServiceConnection;
    private ARDiscoveryServicesDevicesListUpdatedReceiver mArdiscoveryServicesDevicesListUpdatedReceiver;
    private ControlActivity main;
    private ARDiscoveryDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
    public void startDiscovery(){
        initDiscoveryService();
        registerReceivers();
    }

    public DeviceDiscovery(ControlActivity main){
        this.main = main;
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
            Intent i = new Intent(main.getApplicationContext(), ARDiscoveryService.class);
            main.getApplicationContext().bindService(i, mArdiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // if the discovery service already exists, start discovery
            startDiscovery();
        }
    }

    private void registerReceivers() {
        mArdiscoveryServicesDevicesListUpdatedReceiver = new ARDiscoveryServicesDevicesListUpdatedReceiver(main);
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(main.getApplicationContext());
        localBroadcastMgr.registerReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver, new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
    }


    @Override
    public void onServicesDevicesListUpdated() {
        if (mArdiscoveryService != null) {
            List<ARDiscoveryDeviceService> deviceList = mArdiscoveryService.getDeviceServicesArray();


            if (deviceList != null && deviceList.size() > 0) {

                this.device = createDiscoveryDevice(deviceList.get(0));
             //   main.setDevice(device);

                if (device != null) {
                    unregisterReceivers();
                    closeServices();
                } else {

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
    private void unregisterReceivers() {
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(main.getApplicationContext());

        localBroadcastMgr.unregisterReceiver(mArdiscoveryServicesDevicesListUpdatedReceiver);
    }

    private void closeServices() {

        if (mArdiscoveryService != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mArdiscoveryService.stop();

                    main.getApplicationContext().unbindService(mArdiscoveryServiceConnection);
                    mArdiscoveryService = null;
                }
            }).start();
        }
    }


}
