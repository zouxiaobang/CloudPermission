package com.cloud.permissionc.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import com.cloud.permissionc.base.ManifestUnregisterException;
import com.cloud.permissionc.base.PermissionCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author xb.zou
 * @date 2018/11/12
 * @desc
 **/
public class PermissionUtil {
    /**
     * 获取Manifest文件中申请请求的权限
     */
    public static List<String> getPermissionsByManifest(Context context){
        PackageManager pm = context.getPackageManager();
        try {
            return Arrays.asList(pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检测target version是否符合要求
     */
    public static void checkTargetSdkVersion(Context context, List<String> permissions){
        if (permissions.contains(PermissionCode.REQUEST_INSTALL_PACKAGES)
                || permissions.contains(PermissionCode.ANSWER_PHONE_CALLS)
                || permissions.contains(PermissionCode.READ_PHONE_NUMBERS)){
            if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.O){
                throw new RuntimeException("您请求了8.0的权限，需要将target version设置为26或以上.");
            }
        } else {
            if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.M){
                throw new RuntimeException("请求权限需要在target version23或以上使用.");
            }
        }
    }

    /**
     * 获取授权失败的权限
     */
    public static List<String> getFailPermissions(Context context, List<String> permissions){
        //如果为API23以下，直接返回
        if (!isOverM()){
            return null;
        }

        ArrayList<String> failPermissions = new ArrayList<>();
        for (String permission: permissions){
            //检测安装权限
            if (permission.equals(PermissionCode.REQUEST_INSTALL_PACKAGES)){
                if (!isHasInstallPermission(context)){
                    failPermissions.add(permission);
                }

                continue;
            }

            //检测悬浮框权限
            if (permission.equals(PermissionCode.SYSTEM_ALERT_WINDOW)){
                if (!isHasOverlaysPermission(context)){
                    failPermissions.add(permission);
                }

                continue;
            }

            //检测两个8.0的新权限
            if (permission.equals(PermissionCode.ANSWER_PHONE_CALLS)
                    ||permission.equals(PermissionCode.READ_PHONE_NUMBERS)){
                if (!isOverO()){
                    continue;
                }
            }

            //将授权失败的权限添加进去
            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED){
                failPermissions.add(permission);
            }
        }

        return failPermissions;
    }

    /**
     * 获取授权成功的权限
     */
    public static List<String> getSuccessPermissions(String[] permissions, int[] grantResults) {
        List<String> succeedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {

            //把授予过的权限加入到集合中，-1表示没有授予，0表示已经授予
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                succeedPermissions.add(permissions[i]);
            }
        }
        return succeedPermissions;
    }

    /**
     * 获取授权失败的权限
     */
    public static List<String> getFailPermissions(String[] permissions, int[] grantResults) {
        List<String> failPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {

            //把没有授予过的权限加入到集合中，-1表示没有授予，0表示已经授予
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                failPermissions.add(permissions[i]);
            }
        }
        return failPermissions;
    }

    /**
     * 是否还能继续申请没有授予的权限
     */
    public static boolean isRequestDeniedPermission(Activity activity, List<String> failPermissions) {
        for (String permission : failPermissions) {
            //检查是否还有权限还能继续申请的（这里指没有被授予的权限但是也没有被永久拒绝的）
            if (!checkSinglePermanentDenied(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkMorePermanentDenied(Activity activity, List<String> permissions) {

        for (String permission : permissions) {

            if (checkSinglePermanentDenied(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检测权限是否都在Manifest文件中注册
     */
    public static void checkPermissionsInManifest(Activity activity, List<String> permissions) {
        List<String> manifest = getPermissionsByManifest(activity);
        if (manifest != null && manifest.size() != 0){
            for (String permission: permissions){
                if (!manifest.contains(permission)){
                    throw new ManifestUnregisterException(permission);
                }
            }
        } else {
            throw new ManifestUnregisterException(null);
        }
    }


    /**
     * 当前版本是否在23及以上
     */
    public static boolean isOverM(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 当前版本是否在26及以上
     */
    public static boolean isOverO(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * 是否拥有安装权限的请求能力
     */
    public static boolean isHasInstallPermission(Context context){
        if (isOverO()){
            return context.getPackageManager().canRequestPackageInstalls();
        }

        return true;
    }

    /**
     * 是否拥有悬浮框权限的请求能力
     */
    public static boolean isHasOverlaysPermission(Context context){
        if (isOverM()){
            return Settings.canDrawOverlays(context);
        }

        return true;
    }

    /***
     * 检查某个权限是否被永久拒绝
     * @param activity
     * @param permission
     * @return
     */
    private static boolean checkSinglePermanentDenied(Activity activity, String permission) {
        //安装权限和浮窗权限不算，本身申请方式和危险权限申请方式不同，因为没有永久拒绝的选项，所以这里返回false
        if (permission.equals(PermissionCode.REQUEST_INSTALL_PACKAGES)
                || permission.equals(PermissionCode.SYSTEM_ALERT_WINDOW)) {
            return false;
        }

        //检测8.0的两个新权限
        if (permission.equals(PermissionCode.ANSWER_PHONE_CALLS)
                || permission.equals(PermissionCode.READ_PHONE_NUMBERS)) {

            //检查当前的安卓版本是否符合要求
            if (!isOverO()) {
                return false;
            }
        }

        if (PermissionUtil.isOverM()) {
            if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED  &&
                    !activity.shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }
        return false;
    }
}
