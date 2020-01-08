package ch.heigvd.iict.sym_labo4.viewmodels;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.data.Data;

public class BleOperationsViewModel extends AndroidViewModel {

    private static final String TAG = BleOperationsViewModel.class.getSimpleName();

    private MySymBleManager ble = null;
    private BluetoothGatt mConnection = null;

    //live data - observer
    private final MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>();
    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    // LiveData sur la température donnée par le périphérique
    private final MutableLiveData<Integer> mTemperature = new MutableLiveData<>();
    public LiveData<Integer> getTemperature() {
        return mTemperature;
    }

    // LiveData sur le nombre de boutons pressés du périphérique
    private final MutableLiveData<Integer> mCounter = new MutableLiveData<>();
    public LiveData<Integer> getCounter() {
        return mCounter;
    }

    // LiveData sur l'horloge du périphérique
    private final MutableLiveData<String> mTime = new MutableLiveData<>();
    public LiveData<String> getTime() {
        return mTime;
    }
    //references to the Services and Characteristics of the SYM Pixl
    private BluetoothGattService timeService = null, symService = null;
    private BluetoothGattCharacteristic currentTimeChar = null, integerChar = null, temperatureChar = null, buttonClickChar = null;

    public BleOperationsViewModel(Application application) {
        super(application);
        this.mIsConnected.setValue(false); //to be sure that it's never null
        this.ble = new MySymBleManager();
        this.ble.setGattCallbacks(this.bleManagerCallbacks);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared");
        this.ble.disconnect();
    }

    public void connect(BluetoothDevice device) {
        Log.d(TAG, "User request connection to: " + device);
        if(!mIsConnected.getValue()) {
            this.ble.connect(device)
                    .retry(1, 100)
                    .useAutoConnect(false)
                    .enqueue();
        }
    }

    public void disconnect() {
        Log.d(TAG, "User request disconnection");
        this.ble.disconnect();
        if(mConnection != null) {
            mConnection.disconnect();
        }
    }

    // Récupère la température sur le périphérique
    public boolean readTemperature() {
        if(!isConnected().getValue() || temperatureChar == null) return false;
        return ble.readTemperature();
    }

    // Envoie un entier sur le périphérique
    public boolean sendInteger(int value) {
        if(!isConnected().getValue() || integerChar == null) return false;
        return ble.sendInteger(value);
    }

    // Met à jour l'heure sur le périphérique
    public boolean updateTime() {
        if(!isConnected().getValue() || currentTimeChar == null) return false;
        return ble.updateTime();
    }

    private BleManagerCallbacks bleManagerCallbacks = new BleManagerCallbacks() {
        @Override
        public void onDeviceConnecting(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceConnecting");
            mIsConnected.setValue(false);
        }

        @Override
        public void onDeviceConnected(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceConnected");
            mIsConnected.setValue(true);
        }

        @Override
        public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceDisconnecting");
            mIsConnected.setValue(false);
        }

        @Override
        public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceDisconnected");
            mIsConnected.setValue(false);
        }

        @Override
        public void onLinkLossOccurred(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onLinkLossOccurred");
        }

        @Override
        public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {
            Log.d(TAG, "onServicesDiscovered");
        }

        @Override
        public void onDeviceReady(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceReady");
        }

        @Override
        public void onBondingRequired(@NonNull BluetoothDevice device) {
            Log.w(TAG, "onBondingRequired");
        }

        @Override
        public void onBonded(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onBonded");
        }

        @Override
        public void onBondingFailed(@NonNull BluetoothDevice device) {
            Log.e(TAG, "onBondingFailed");
        }

        @Override
        public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {
            Log.e(TAG, "onError:" + errorCode);
        }

        @Override
        public void onDeviceNotSupported(@NonNull BluetoothDevice device) {
            Log.e(TAG, "onDeviceNotSupported");
            Toast.makeText(getApplication(), "Device not supported", Toast.LENGTH_SHORT).show();
        }
    };

    /*
     *  This class is used to implement the protocol to communicate with the BLE device
     */
    private class MySymBleManager extends BleManager<BleManagerCallbacks> {
        // Services UUID
        private final UUID TIMESERVICE_UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
        private final UUID SYMSERVICE_UUID = UUID.fromString("3c0a1000-281d-4b48-b2a7-f15579a1c38f");

        // Caractéristiques UUID
        private final UUID CURRENTTIME_CHAR = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");
        private final UUID INTEGER_CHAR = UUID.fromString("3c0a1001-281d-4b48-b2a7-f15579a1c38f");
        private final UUID TEMPERATURE_CHAR = UUID.fromString("3c0a1002-281d-4b48-b2a7-f15579a1c38f");
        private final UUID BUTTONS_CHAR = UUID.fromString("3c0a1003-281d-4b48-b2a7-f15579a1c38f");

        private MySymBleManager() {
            super(getApplication());
        }

        @Override
        public BleManagerGattCallback getGattCallback() { return mGattCallback; }

        /**
         * BluetoothGatt callbacks object.
         */
        private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {
            @Override
            public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
                mConnection = gatt; //trick to force disconnection
                Log.d(TAG, "isRequiredServiceSupported - discovered services:");

                timeService = gatt.getService(TIMESERVICE_UUID);
                symService = gatt.getService(SYMSERVICE_UUID);
                if (timeService != null) {
                    currentTimeChar = timeService.getCharacteristic(CURRENTTIME_CHAR);
                }
                if (symService != null) {
                    integerChar = symService.getCharacteristic(INTEGER_CHAR);
                    temperatureChar = symService.getCharacteristic(TEMPERATURE_CHAR);
                    buttonClickChar = symService.getCharacteristic(BUTTONS_CHAR);
                }
                // Validate properties
                boolean readTemperature = false;
                if (temperatureChar != null) {
                    readTemperature = (temperatureChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0;
                }
                boolean notifyButtons = false;
                if (buttonClickChar != null) {
                    final int properties = buttonClickChar.getProperties();
                    notifyButtons = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
                }
                boolean writeInteger = false;
                if (integerChar != null) {
                    final int properties = integerChar.getProperties();
                    writeInteger = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
                    integerChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                }
                boolean notifyTime = false;
                boolean writeTime = false;
                if (currentTimeChar != null) {
                    final int propertiesNotify = currentTimeChar.getProperties();
                    notifyTime = (propertiesNotify & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;

                    final int propertiesWrite = integerChar.getProperties();
                    writeTime = (propertiesWrite & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
                    integerChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                }

                // Return true if all required services have been found
                return currentTimeChar != null && integerChar != null && temperatureChar != null && buttonClickChar != null
                        && readTemperature && notifyButtons && writeInteger && notifyTime && writeTime;
            }

            @Override
            protected void initialize() {
                // Enregistrement des notifications sur le nombre de boutons pressés
                setNotificationCallback(buttonClickChar).with((device, data) ->
                        mCounter.setValue(data.getIntValue(Data.FORMAT_UINT8, 0))
                );
                enableNotifications(buttonClickChar).enqueue();

                // Enregistrement des notifications sur l'heure du périphérique
                setNotificationCallback(currentTimeChar).with((device, data) -> {
                    int year        = data.getIntValue(Data.FORMAT_UINT16, 0);
                    int month       = data.getIntValue(Data.FORMAT_UINT8, 2);
                    int dayOfMonth  = data.getIntValue(Data.FORMAT_UINT8, 3);
                    int hour        = data.getIntValue(Data.FORMAT_UINT8, 4);
                    int minutes     = data.getIntValue(Data.FORMAT_UINT8, 5);
                    int seconds     = data.getIntValue(Data.FORMAT_UINT8, 6);

                    DateFormat formatter = new SimpleDateFormat("E MMM d yyyy, HH:mm:ss");
                    GregorianCalendar c = new GregorianCalendar(year, month - 1, dayOfMonth, hour, minutes, seconds);
                    mTime.setValue(formatter.format(c.getTime()));
                });
                enableNotifications(currentTimeChar).enqueue();
            }

            @Override
            protected void onDeviceDisconnected() {
                //we reset services and characteristics
                timeService = null;
                currentTimeChar = null;

                symService = null;
                integerChar = null;
                temperatureChar = null;
                buttonClickChar = null;
            }
        };

        // On récupère la température via lecture de la caractéristique Temperature
        public boolean readTemperature() {
            if (temperatureChar != null) {
                readCharacteristic(temperatureChar).with((device, data) -> {
                    mTemperature.setValue(data.getIntValue(Data.FORMAT_UINT16, 0) / 10);
                }).enqueue();
                return true;
            }
            return false;
        }

        // Envoi d'un entier par écriture sur la caractéristique int
        public boolean sendInteger(int value) {
            if (integerChar == null) return false;

            ByteBuffer bb = ByteBuffer.allocate(4); // On crée un Uint32
            bb.putInt(value);
            bb.order(ByteOrder.LITTLE_ENDIAN); // On le transforme en LITTLE ENDIAN
            writeCharacteristic(integerChar, bb.array()).enqueue();
            return true;
        }

        // Mise à jour de l'heure sur le périphérique via écriture sur la caractéristique Current Time
        public boolean updateTime() {
            if (currentTimeChar == null) return false;

            Calendar time = new GregorianCalendar();
            byte[] field = new byte[10];

            int year = time.get(Calendar.YEAR);
            field[0] = (byte) (year & 0xFF);
            field[1] = (byte) ((year >> 8) & 0xFF);
            field[2] = (byte) (time.get(Calendar.MONTH) + 1);
            field[3] = (byte) time.get(Calendar.DATE);
            field[4] = (byte) time.get(Calendar.HOUR_OF_DAY);
            field[5] = (byte) time.get(Calendar.MINUTE);
            field[6] = (byte) time.get(Calendar.SECOND);
            field[7] = (byte) Calendar.DAY_OF_WEEK;
            field[8] = (byte) (time.get(Calendar.MILLISECOND) / 256);
            field[9] = 0;

            writeCharacteristic(currentTimeChar, field).enqueue();
            return true;
        }
    }
}
