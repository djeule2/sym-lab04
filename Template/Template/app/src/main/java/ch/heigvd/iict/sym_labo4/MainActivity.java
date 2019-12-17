package ch.heigvd.iict.sym_labo4;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //events
        findViewById(R.id.nav_4).setOnClickListener((view) -> {
            Intent i = new Intent(MainActivity.this, CompassActivity.class);
            startActivity(i);
        });
        findViewById(R.id.nav_5).setOnClickListener((view) -> {
            startBleActivity();
        });

        //Android 23+ we need to ask form user permission at runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            askForRuntimePermissions();
        }
    }

    @TargetApi(23)
    private void askForRuntimePermissions() {
        // Android M+ Permission check
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can use Bluetooth Low Energy");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener((dialog) -> {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
            });
            builder.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "fine location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener((dialog) -> {
                        //mandatory for this laboratory !
                    });
                    builder.show();
                }
                return;
            }
        }

        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    protected void startBleActivity() {
        Intent i = new Intent(MainActivity.this, BleActivity.class);
        startActivity(i);
    }

}
