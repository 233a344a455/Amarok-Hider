package deltazero.amarok;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

//import com.catchingnow.icebox.sdk_client.IceBox;

public class Utils {
    public static void requestStoragePermission(Context context) {
        if (XXPermissions.isGranted(context, Permission.MANAGE_EXTERNAL_STORAGE))
            return;

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.storage_permission_request_title)
                .setMessage(R.string.storage_permission_request_message)
                .setPositiveButton("OK", (dialog, which) -> {

                    // Request permissions
                    XXPermissions.with(context)
                            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                            .request(new OnPermissionCallback() {
                                @Override
                                public void onGranted(List<String> permissions, boolean all) {
                                    Log.d("Permission", "Granted: MANAGE_EXTERNAL_STORAGE");
                                }

                                @Override
                                public void onDenied(List<String> permissions, boolean never) {
                                    Log.w("Permission", "User denied: MANAGE_EXTERNAL_STORAGE");
                                    Toast.makeText(context, R.string.storage_permission_denied, Toast.LENGTH_LONG).show();
                                }
                            });

                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();


    }

//
//    private static int PERMISSION_REQUEST_CODE = 100;
//
//    private boolean checkStoragePermission(Context context) {
//        if (SDK_INT >= Build.VERSION_CODES.R) {
//            // For android 11+
//            return Environment.isExternalStorageManager();
//        } else {
//            int read = ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE);
//            int write = ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE);
//            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
//        }
//    }
//
//    private void requestStoragePermission(Context context) {
//        if (SDK_INT >= Build.VERSION_CODES.R) {
//            try {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//                intent.addCategory("android.intent.category.DEFAULT");
//                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
//                startActivityForResult(intent, 2296);
//            } catch (Exception e) {
//                Intent intent = new Intent();
//                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                startActivityForResult(intent, 2296);
//            }
//        } else {
//            //below android 11
//            ActivityCompat.requestPermissions(context, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
//        }
//    }

//    static public IceBox.SilentInstallSupport getIceboxAvailability(Context context) {
//        try {
//            context.getPackageManager().getPackageInfo("com.catchingnow.icebox", 0);
//            return IceBox.querySupportSilentInstall(context);
//        } catch (PackageManager.NameNotFoundException e) {
//            return IceBox.SilentInstallSupport.NOT_INSTALLED;
//        }
//    }
}
