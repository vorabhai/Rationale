package xyz.belvi.permissiondialog.Rationale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

import xyz.belvi.permissiondialog.Permission.PermissionDetails;
import xyz.belvi.permissiondialog.Permission.PermissionState;
import xyz.belvi.permissiondialog.Permission.PermissionTracker;
import xyz.belvi.permissiondialog.Permission.SmoothPermission;

/**
 * Created by zone2 on 1/24/17.
 */

public class RationaleBase extends AppCompatActivity implements CallbackReceiver.Receiver {

    public static final int REQUEST_CODE = 0;
    private static final String EXTRAS = "EXTRAS";

    public static void startTransparentBase(Activity context, ArrayList<SmoothPermission> smoothPermissions, int styleRes, boolean buildAnyway) {
        Intent rationaleIntent = new Intent(context, RationaleBase.class);
        Bundle argument = new Bundle();
        argument.putParcelableArrayList(RationaleDialog.SMOOTH_PERMISSIONS, smoothPermissions);
        argument.putInt(RationaleDialog.STYLE_RES, styleRes);
        argument.putBoolean(RationaleDialog.BUILD_ANYWAY, buildAnyway);

        rationaleIntent.putExtra(EXTRAS, argument);
        context.startActivityForResult(rationaleIntent, REQUEST_CODE);
    }


    private boolean isDangerous(String permission) {
        PermissionDetails permissionDetails = new PermissionDetails().getPermissionDetails(this, permission, -1);
        boolean isDangerous = permissionDetails.getProtectionLevel() != PermissionInfo.PROTECTION_NORMAL;
        return isDangerous;

    }

    private boolean buildAnyway(Bundle bundle) {
        return bundle.getBoolean(RationaleDialog.BUILD_ANYWAY);
    }

    private int styleRes(Bundle bundle) {
        return bundle.getInt(RationaleDialog.STYLE_RES);
    }

    private ArrayList<SmoothPermission> getSmoothPermissions(Bundle bundle) {
        return bundle.getParcelableArrayList(RationaleDialog.SMOOTH_PERMISSIONS);
    }

    private static Bundle mBundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            savedInstanceState = getIntent().getBundleExtra(EXTRAS);
            mBundle = getIntent().getBundleExtra(EXTRAS);

            ArrayList<SmoothPermission> smoothPermissions = new ArrayList<>();
            boolean showSettings = showSettings(smoothPermissions, buildAnyway(savedInstanceState));
            Log.e("settings", String.valueOf(showSettings));
            if (smoothPermissions.size() > 0) {
                new RationaleDialog().initialise(smoothPermissions, styleRes(savedInstanceState), showSettings, buildAnyway(savedInstanceState)).show(getSupportFragmentManager(), "");
            } else {
//            RationaleDialog.returnCallback(permissionResolveListener, smoothPermissions, buildAnyway);
            }
        } else {
            mBundle = savedInstanceState;
        }

    }

    private boolean isPermissionGranted(String permission) {
        return (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED);
    }

    private boolean isPermissionPermanentlyDenied(String permission) {
        if (!PermissionTracker.hasRequired(this, permission)) {
            return false;
        }
        return !ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
    }

    private boolean isPermissionDenied(String permission) {
        return (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_DENIED);
    }


    public boolean showSettings(ArrayList<SmoothPermission> sp, boolean buildAnyway) {
        boolean showSettings = false;
        Log.e("show size", "" + getSmoothPermissions(mBundle).size());
        for (SmoothPermission smoothPermission : getSmoothPermissions(mBundle)) {
            if (isDangerous(smoothPermission.getPermission())) {
                if (isPermissionGranted(smoothPermission.getPermission())) {
                    smoothPermission.setState(PermissionState.GRANTED);
                    continue;
                } else if (isPermissionPermanentlyDenied(smoothPermission.getPermission())) {
                    smoothPermission.setState(PermissionState.PERMANENTLY_DENIED);
                    showSettings = true;
                } else if (isPermissionDenied(smoothPermission.getPermission())) {
                    smoothPermission.setState(PermissionState.DENIED);
                    if (!buildAnyway)
                        continue;
                }
                sp.add(smoothPermission);
            }
        }
        return showSettings;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(RationaleDialog.SMOOTH_PERMISSIONS, getSmoothPermissions(getIntent().getBundleExtra(EXTRAS)));
        outState.putInt(RationaleDialog.STYLE_RES, styleRes(getIntent().getBundleExtra(EXTRAS)));
        outState.putBoolean(RationaleDialog.BUILD_ANYWAY, buildAnyway(getIntent().getBundleExtra(EXTRAS)));
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultData != null) {
            ArrayList<SmoothPermission> smoothPermissions = resultData.getParcelableArrayList(RationaleDialog.SMOOTH_PERMISSIONS);
            if (resultCode == RationaleDialog.PERMISSION_RESOLVE) {
//            permissionResolveListener.onResolved(smoothPermissions);
                Log.e("size", "" + smoothPermissions.size());
                setResult(resultCode, new Intent().putExtra("data", resultData));
                finish();
            }
        } else {
            finish();
        }
    }

}