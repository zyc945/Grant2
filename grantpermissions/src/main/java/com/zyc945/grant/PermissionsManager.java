package com.zyc945.grant;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A class to help you manage your permissions simply.
 */
public class PermissionsManager {
    public static final String TAG = PermissionsManager.class.getSimpleName();
    private static final Set<String> PENDING_AUTHORIZED_PERMISSIONS = new HashSet<>();
    private static final List<WeakReference<PermissionsResultAction>> PENDING_AUTHORIZED_RESULT_ACTIONS = new ArrayList<>();

    /**
     * permission authorized result
     */
    @IntDef({PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_NOT_FOUND})
    @Retention(RetentionPolicy.SOURCE)
    @interface AuthorizedResult {
    }

    /**
     * same define as the {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     */
    static final int PERMISSION_GRANTED = 0;
    /**
     * same define as the {@link android.content.pm.PackageManager#PERMISSION_DENIED}
     */
    static final int PERMISSION_DENIED = -1;
    /**
     * the permission not defined at current device.
     */
    static final int PERMISSION_NOT_FOUND = -2;

    /**
     * This method uses reflection to read all the permissions in the Manifest class.
     * This is necessary because some permissions do not exist on older versions of Android,
     * since they do not exist, they will be denied when you check whether you have permission
     * which is problematic since a new permission is often added where there was no previous
     * permission required. We initialize a Set of available permissions and check the set
     * when checking if we have permission since we want to know when we are denied a permission
     * because it doesn't exist yet.
     */
    private static final Set<String> SYSTEM_SUPPORT_PERMISSIONS = new HashSet<>();

    static {
        Field[] fields = Manifest.permission.class.getFields();
        for (Field field : fields) {
            String name = null;
            try {
                name = (String) field.get("");
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Could not access field", e);
            }
            SYSTEM_SUPPORT_PERMISSIONS.add(name);
        }
    }

    static String[] getPendingAuthorizedPermissions() {
        return PENDING_AUTHORIZED_PERMISSIONS.toArray(new String[PENDING_AUTHORIZED_PERMISSIONS.size()]);
    }

    /**
     * This method retrieves all the permissions declared in the application's manifest.
     * It returns a non null array of permisions that can be declared.
     *
     * @param activity the Activity necessary to check what permissions we have.
     * @return a non null array of permissions that are declared in the application manifest.
     */
    @NonNull
    private static synchronized String[] getManifestPermissions(@NonNull final Activity activity) {
        PackageInfo packageInfo = null;
        List<String> list = new ArrayList<>(1);
        try {
            Log.d(TAG, activity.getPackageName());
            packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "A problem occurred when retrieving permissions", e);
        }
        if (packageInfo != null) {
            String[] permissions = packageInfo.requestedPermissions;
            if (permissions != null) {
                for (String perm : permissions) {
                    Log.d(TAG, "Manifest contained permission: " + perm);
                    list.add(perm);
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * This method adds the {@link PermissionsResultAction} to the current list
     * of pending actions that will be completed when the permissions are
     * received. The list of permissions passed to this method are registered
     * in the PermissionsResultAction object so that it will be notified of changes
     * made to these permissions.
     *
     * @param permissions the required permissions for the action to be executed.
     * @param action      the action to add to the current list of pending actions.
     */
    private static synchronized void addPendingAction(@NonNull String[] permissions,
                                                      @Nullable PermissionsResultAction action) {
        if (action == null) {
            return;
        }
        action.registerPermissions(permissions);
        PENDING_AUTHORIZED_RESULT_ACTIONS.add(new WeakReference<>(action));
    }

    /**
     * This method removes a pending action from the list of pending actions.
     * It is used for cases where the permission has already been granted, so
     * you immediately wish to remove the pending action from the queue and
     * execute the action.
     *
     * @param action the action to remove
     */
    private static synchronized void removePendingAction(@Nullable PermissionsResultAction action) {
        for (Iterator<WeakReference<PermissionsResultAction>> iterator = PENDING_AUTHORIZED_RESULT_ACTIONS.iterator();
             iterator.hasNext(); ) {
            WeakReference<PermissionsResultAction> weakRef = iterator.next();
            if (weakRef.get() == action || weakRef.get() == null) {
                iterator.remove();
            }
        }
    }

    /**
     * This static method can be used to check whether or not you have a specific permission.
     * It is basically a less verbose method of using {@link ActivityCompat#checkSelfPermission(Context, String)}
     * and will simply return a boolean whether or not you have the permission. If you pass
     * in a null Context object, it will return false as otherwise it cannot check the permission.
     * However, the Activity parameter is nullable so that you can pass in a reference that you
     * are not always sure will be valid or not (e.g. getActivity() from Fragment).
     *
     * @param context    the Context necessary to check the permission
     * @param permission the permission to check
     * @return true if you have been granted the permission, false otherwise
     */
    @SuppressWarnings("unused")
    public static synchronized boolean hasPermission(@Nullable Context context, @NonNull String permission) {
        return context != null && (ActivityCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED || !SYSTEM_SUPPORT_PERMISSIONS.contains(permission));
    }

    /**
     * This static method can be used to check whether or not you have several specific permissions.
     * It is simpler than checking using {@link ActivityCompat#checkSelfPermission(Context, String)}
     * for each permission and will simply return a boolean whether or not you have all the permissions.
     * If you pass in a null Context object, it will return false as otherwise it cannot check the
     * permission. However, the Activity parameter is nullable so that you can pass in a reference
     * that you are not always sure will be valid or not (e.g. getActivity() from Fragment).
     *
     * @param context     the Context necessary to check the permission
     * @param permissions the permissions to check
     * @return true if you have been granted all the permissions, false otherwise
     */
    @SuppressWarnings("unused")
    public static synchronized boolean hasAllPermissions(@Nullable Context context, @NonNull String[] permissions) {
        if (context == null) {
            return false;
        }
        boolean hasAllPermissions = true;
        for (String perm : permissions) {
            hasAllPermissions &= hasPermission(context, perm);
        }
        return hasAllPermissions;
    }

    /**
     * This method will request all the permissions declared in your application manifest
     * for the specified {@link PermissionsResultAction}. The purpose of this method is to enable
     * all permissions to be requested at one shot. The PermissionsResultAction is used to notify
     * you of the user allowing or denying each permission. The Activity and PermissionsResultAction
     * parameters are both annotated Nullable, but this method will not work if the Activity
     * is null. It is only annotated Nullable as a courtesy to prevent crashes in the case
     * that you call this from a Fragment where {@link Fragment#getActivity()} could yield
     * null. Additionally, you will not receive any notification of permissions being granted
     * if you provide a null PermissionsResultAction.
     *
     * @param activity the Activity necessary to request and check permissions.
     * @param action   the PermissionsResultAction used to notify you of permissions being accepted.
     */
    @SuppressWarnings("unused")
    public static synchronized void requestAllManifestPermissionsIfNecessary(final @Nullable Activity activity,
                                                                             final @Nullable PermissionsResultAction action) {
        if (activity == null) {
            return;
        }
        String[] perms = getManifestPermissions(activity);
        requestPermissions(activity, perms, action);
    }

    /**
     * This method should be used to execute a {@link PermissionsResultAction} for the array
     * of permissions passed to this method. This method will request the permissions if
     * they need to be requested (i.e. we don't have permission yet) and will add the
     * PermissionsResultAction to the queue to be notified of permissions being granted or
     * denied. In the case of pre-Android Marshmallow, permissions will be granted immediately.
     * The Activity variable is nullable, but if it is null, the method will fail to execute.
     * This is only nullable as a courtesy for Fragments where getActivity() may yeild null
     * if the Fragment is not currently added to its parent Activity.
     *
     * @param activity    the activity necessary to request the permissions.
     * @param permissions the list of permissions to request for the {@link PermissionsResultAction}.
     * @param action      the PermissionsResultAction to notify when the permissions are granted or denied.
     */
    @SuppressWarnings("unused")
    static synchronized void requestPermissionsIfNecessaryForResult(@Nullable Activity activity,
                                                                           @NonNull String[] permissions,
                                                                           @Nullable PermissionsResultAction action) {
        if (activity == null) {
            return;
        }
        addPendingAction(permissions, action);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            doPermissionWorkBeforeAndroidM(activity, permissions, action);
        } else {
            List<String> permList = getPermissionsListToRequest(activity, permissions, action);
            if (permList.isEmpty()) {
                //if there is no permission to request, there is no reason to keep the action int the list
                removePendingAction(action);
            } else {
                String[] permsToRequest = permList.toArray(new String[permList.size()]);
                PENDING_AUTHORIZED_PERMISSIONS.addAll(permList);

                ActivityCompat.requestPermissions(activity, permsToRequest, 1);
            }
        }
    }

    public static synchronized void requestPermissions(@Nullable Activity activity,
                                                       @NonNull String[] permissions,
                                                       @Nullable PermissionsResultAction action) {
        if (activity == null) {
            return;
        }
        addPendingAction(permissions, action);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            doPermissionWorkBeforeAndroidM(activity, permissions, action);
        } else {
            List<String> permList = getPermissionsListToRequest(activity, permissions, action);
            if (permList.isEmpty()) {
                //if there is no permission to request, there is no reason to keep the action int the list
                removePendingAction(action);
            } else {
                PENDING_AUTHORIZED_PERMISSIONS.addAll(permList);
                activity.startActivity(new Intent(activity, InvisiblePermissionRequestActivity.class));
            }
        }
    }

    /**
     * This method should be used to execute a {@link PermissionsResultAction} for the array
     * of permissions passed to this method. This method will request the permissions if
     * they need to be requested (i.e. we don't have permission yet) and will add the
     * PermissionsResultAction to the queue to be notified of permissions being granted or
     * denied. In the case of pre-Android Marshmallow, permissions will be granted immediately.
     * The Fragment variable is used, but if {@link Fragment#getActivity()} returns null, this method
     * will fail to work as the activity reference is necessary to check for permissions.
     *
     * @param fragment    the fragment necessary to request the permissions.
     * @param permissions the list of permissions to request for the {@link PermissionsResultAction}.
     * @param action      the PermissionsResultAction to notify when the permissions are granted or denied.
     */
    @SuppressWarnings("unused")
    static synchronized void requestPermissionsIfNecessaryForResult(@NonNull Fragment fragment,
                                                                           @NonNull String[] permissions,
                                                                           @Nullable PermissionsResultAction action) {
        Activity activity = fragment.getActivity();
        if (activity == null) {
            return;
        }
        requestPermissionsIfNecessaryForResult(activity, permissions, action);
    }

    /**
     * This method notifies the PermissionsManager that the permissions have change. If you are making
     * the permissions requests using an Activity, then this method should be called from the
     * Activity callback onRequestPermissionsResult() with the variables passed to that method. If
     * you are passing a Fragment to make the permissions request, then you should call this in
     * the {@link Fragment#onRequestPermissionsResult(int, String[], int[])} method.
     * It will notify all the pending PermissionsResultAction objects currently
     * in the queue, and will remove the permissions request from the list of pending requests.
     *
     * @param permissions the permissions that have changed.
     * @param results     the values for each permission.
     */
    @SuppressWarnings("unused")
    public static synchronized void notifyPermissionsChange(@NonNull String[] permissions, @NonNull int[] results) {
        int size = permissions.length;
        if (results.length < size) {
            size = results.length;
        }
        Iterator<WeakReference<PermissionsResultAction>> iterator = PENDING_AUTHORIZED_RESULT_ACTIONS.iterator();
        while (iterator.hasNext()) {
            PermissionsResultAction action = iterator.next().get();
            for (int n = 0; n < size; n++) {
                if (action == null || action.onResult(permissions[n], results[n])) {
                    iterator.remove();
                    break;
                }
            }
        }
        for (int n = 0; n < size; n++) {
            PENDING_AUTHORIZED_PERMISSIONS.remove(permissions[n]);
        }
    }

    /**
     * When request permissions on devices before Android M (Android 6.0, API Level 23)
     * Do the granted or denied work directly according to the permission status
     *
     * @param activity    the activity to check permissions
     * @param permissions the permissions names
     * @param action      the callback work object, containing what we what to do after
     *                    permission check
     */
    private static void doPermissionWorkBeforeAndroidM(@NonNull Activity activity,
                                                       @NonNull String[] permissions,
                                                       @Nullable PermissionsResultAction action) {
        for (String perm : permissions) {
            if (action != null) {
                if (!SYSTEM_SUPPORT_PERMISSIONS.contains(perm)) {
                    action.onResult(perm, PERMISSION_NOT_FOUND);
                } else if (ActivityCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                    action.onResult(perm, PERMISSION_DENIED);
                } else {
                    action.onResult(perm, PERMISSION_GRANTED);
                }
            }
        }
    }

    /**
     * Filter the permissions list:
     * If a permission is not granted, add it to the result list
     * if a permission is granted, do the granted work, do not add it to the result list
     *
     * @param activity    the activity to check permissions
     * @param permissions all the permissions names
     * @param action      the callback work object, containing what we what to do after
     *                    permission check
     * @return a list of permissions names that are not granted yet
     */
    @NonNull
    private static List<String> getPermissionsListToRequest(@NonNull Activity activity,
                                                            @NonNull String[] permissions,
                                                            @Nullable PermissionsResultAction action) {
        List<String> permList = new ArrayList<>(permissions.length);
        for (String perm : permissions) {
            if (!SYSTEM_SUPPORT_PERMISSIONS.contains(perm)) {
                if (action != null) {
                    action.onResult(perm, PERMISSION_NOT_FOUND);
                }
            } else if (ActivityCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                if (!PENDING_AUTHORIZED_PERMISSIONS.contains(perm)) {
                    permList.add(perm);
                }
            } else {
                if (action != null) {
                    action.onResult(perm, PERMISSION_GRANTED);
                }
            }
        }
        return permList;
    }

}
