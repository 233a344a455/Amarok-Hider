package deltazero.amarok.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import deltazero.amarok.Hider;
import deltazero.amarok.R;
import deltazero.amarok.Utils;

// import static deltazero.amarok.Utils.getIceboxAvailability;

public class MainActivity extends AppCompatActivity {

    public final static String TAG = "Main";

    public Hider hider;
    private MaterialButton btPrimary, btSecondary;
    private TextView tvStatusInfo, tvStatus;
    private String appVersionName;

    private ActivityResultLauncher<Uri> mDirRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        hider = new Hider(this);
        if (hider.getIsHidden()) {
            this.setTheme(R.style.Theme_Amarok_night);
        } else {
            this.setTheme(R.style.Theme_Amarok_day);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init UI
        btPrimary = findViewById(R.id.main_bt_primary);
        btSecondary = findViewById(R.id.main_bt_secondary);
        tvStatus = findViewById(R.id.main_tv_status);
        tvStatusInfo = findViewById(R.id.main_tv_statusinfo);
        updateUi(true);

        // Get app version
        try {
            appVersionName = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // Make compiler happy
        }

        // Process Permissions
        Utils.requestStoragePermission(this);

        // Register file-picker result handler
        mDirRequest = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree(),
                uri -> {
                    if (uri != null) {
                        // call this to persist permission across device reboots
                        getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        // set hide path
                        String path = Environment.getExternalStorageDirectory() + "/" + uri.getPath().split(":")[1];
                        hider.setEncodePath(path);
                        Log.i(TAG, "Set encode path: " + path);
                        Toast.makeText(this, "Set encode path: " + path, Toast.LENGTH_SHORT).show();

                    } else {
                        // request denied by user
                        Log.i(TAG, "Set encode path cancelled");
                    }
                }
        );

    }

    public void buttonDusk(View view) {
        hider.unhide();
        updateUi(false);
    }

    public void buttonDawn(View view) {
        hider.hide();
        updateUi(false);
    }

    public void buttonSetHideApps(View view) {

        if (!hider.getIsHidden()) {
            Toast.makeText(this, R.string.ava_at_night, Toast.LENGTH_SHORT).show();
            return;
        }

//        // Check Icebox availability
//        switch (getIceboxAvailability(this)) {
//            case SUPPORTED:
//                break;
//            case NOT_INSTALLED:
//                Toast.makeText(this, R.string.icebox_not_installed, Toast.LENGTH_LONG).show();
//                return;
//            case NOT_DEVICE_OWNER:
//                Toast.makeText(this, R.string.icebox_not_active, Toast.LENGTH_LONG).show();
//                return;
//            default:
//                Toast.makeText(this, R.string.icebox_not_supported, Toast.LENGTH_LONG).show();
//                return;
//        }

        // Set up the etInput
        final EditText etInput = new EditText(this);
        Set<String> originalPkgNames = hider.getHidePkgNames();
        etInput.setText(String.join("\n", (originalPkgNames != null ? originalPkgNames : new HashSet<String>())));
        etInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.get_app_names_title))
                .setMessage(R.string.get_app_names_info)
                .setView(etInput)
                .setPositiveButton("OK", (dialog, which) -> {
                    String input = etInput.getText().toString();
                    hider.setHidePkgNames(new HashSet<>(Arrays.asList(input.split("\n", -1))));
                    Toast.makeText(this, "Hide App set: " + input, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel())
                .show();
    }

    public void buttonShowAbout(View view) {

        String iceboxAvailability;
        iceboxAvailability = getString(R.string.icebox_not_supported);

//        switch (getIceboxAvailability(this)) {
//            case SUPPORTED:
//                iceboxAvailability = getString(R.string.icebox_available);
//                break;
//            case NOT_INSTALLED:
//                iceboxAvailability = getString(R.string.icebox_not_installed);
//                break;
//            case NOT_DEVICE_OWNER:
//                iceboxAvailability = getString(R.string.icebox_not_active);
//                break;
//            default:
//                iceboxAvailability = getString(R.string.icebox_not_supported);
//        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.about))
                .setMessage(String.format(getString(R.string.app_about), appVersionName, iceboxAvailability))
                .setPositiveButton(getString(R.string.ok), null)
                .show();
    }

    public void buttonSetEncodeFile(View view) {

        if (!XXPermissions.isGranted(this, Permission.MANAGE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, R.string.storage_permission_denied, Toast.LENGTH_LONG).show();
            return;
        }

        if (!hider.getIsHidden()) {
            Toast.makeText(this, R.string.ava_at_night, Toast.LENGTH_SHORT).show();
            return;
        }
        mDirRequest.launch(null);
    }


    public void updateUi(boolean isInitializing) {

        if (!isInitializing) {
            boolean uiIsNight = (tvStatus.getText() == getText(R.string.night_status));
            if (hider.getIsHidden() != uiIsNight) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }

        if (hider.getIsHidden()) {
            // Night

            btPrimary.setText(getText(R.string.dawn));
            btPrimary.setOnClickListener(this::buttonDawn);
            btSecondary.setText(getText(R.string.dusk));
            btSecondary.setOnClickListener(this::buttonDusk);

            tvStatus.setText(getText(R.string.night_status));
            tvStatusInfo.setText(getText(R.string.night_info));
        }

        if (!hider.getIsHidden()) {
            // Day

            btPrimary.setText(getText(R.string.dusk));
            btPrimary.setOnClickListener(this::buttonDusk);
            btSecondary.setText(getText(R.string.dawn));
            btSecondary.setOnClickListener(this::buttonDawn);

            tvStatus.setText(getText(R.string.day_status));
            tvStatusInfo.setText(getText(R.string.day_info));
        }
    }


    @Override
    protected void onResume() {
        updateUi(false);
        super.onResume();
    }

}




