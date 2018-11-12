package com.cloud.permissionc;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.SparseArray;

import com.cloud.permissionc.base.OnPermissionListener;
import com.cloud.permissionc.base.PermissionCode;
import com.cloud.permissionc.util.PermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author xb.zou
 * @date 2018/11/12
 * @desc
 **/
public class PermissionManager extends Fragment implements Runnable {
    private static final String PERMISSION_GROUP = "permission_group";
    private static final String PERMISSION_CODE = "permission_code";
    private static final String PERMISSION_CONNECT = "permission_connect";

    private static final SparseArray<OnPermissionListener> sContainer = new SparseArray<>();
    /**
     * 是否已经回调了，避免安装权限和悬浮窗同时请求导致的重复回调
     */
    private boolean isBackCall;

    public static PermissionManager newInstant(ArrayList<String> permissions, boolean isConnect) {
        PermissionManager permissionManager = new PermissionManager();
        Bundle bundle = new Bundle();
        //请求码，随机生成
        int requestCode;
        do {
            requestCode = new Random().nextInt(255);
        } while (sContainer.get(requestCode) != null);

        bundle.putInt(PERMISSION_CODE, requestCode);
        bundle.putStringArrayList(PERMISSION_GROUP, permissions);
        bundle.putBoolean(PERMISSION_CONNECT, isConnect);
        permissionManager.setArguments(bundle);

        return permissionManager;
    }

    /**
     * 准备申请权限
     */
    public void prepareRequest(Activity activity, OnPermissionListener listener) {
        sContainer.put(getArguments().getInt(PERMISSION_CODE), listener);
        activity.getFragmentManager()
                .beginTransaction()
                .add(this, activity.getClass().getName())
                .commit();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<String> permissions = getArguments().getStringArrayList(PERMISSION_GROUP);
        if (permissions == null){
            return;
        }

        boolean isInstallEnable = (permissions.contains(PermissionCode.REQUEST_INSTALL_PACKAGES)
                && !PermissionUtil.isHasInstallPermission(getActivity()));
        boolean isAlertEnable = (permissions.contains(PermissionCode.SYSTEM_ALERT_WINDOW)
                && !PermissionUtil.isHasOverlaysPermission(getActivity()));
        if (isInstallEnable || isAlertEnable){
            if (isInstallEnable) {
                //跳转到允许安装未知来源设置页面
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + getActivity().getPackageName()));
                startActivityForResult(intent, getArguments().getInt(PERMISSION_CODE));
            }
            if (isAlertEnable){
                //跳转到悬浮窗设置页面
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getActivity().getPackageName()));
                startActivityForResult(intent, getArguments().getInt(PERMISSION_CODE));
            }
        }

        requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        OnPermissionListener listener = sContainer.get(requestCode);
        if (listener == null){
            return;
        }

        for (int i = 0;i < permissions.length; i++){
            //重新检查安装权限
            if (PermissionCode.REQUEST_INSTALL_PACKAGES.equals(permissions[i])) {
                if (PermissionUtil.isHasInstallPermission(getActivity())) {
                    grantResults[i] = PackageManager.PERMISSION_GRANTED;
                } else {
                    grantResults[i] = PackageManager.PERMISSION_DENIED;
                }
            }

            //重新检查悬浮窗权限
            if (PermissionCode.SYSTEM_ALERT_WINDOW.equals(permissions[i])) {
                if (PermissionUtil.isHasOverlaysPermission(getActivity())) {
                    grantResults[i] = PackageManager.PERMISSION_GRANTED;
                } else {
                    grantResults[i] = PackageManager.PERMISSION_DENIED;
                }
            }

            //重新检查8.0的两个新权限
            if (permissions[i].equals(PermissionCode.ANSWER_PHONE_CALLS)
                    || permissions[i].equals(PermissionCode.READ_PHONE_NUMBERS)) {

                //检查当前的安卓版本是否符合要求
                if (!PermissionUtil.isOverO()) {
                    grantResults[i] = PackageManager.PERMISSION_GRANTED;
                }
            }
        }

        List<String> successPermissions = PermissionUtil.getSuccessPermissions(permissions, grantResults);
        if (successPermissions.size() == permissions.length){
            listener.hasPermission(successPermissions, true);
        } else {
            List<String> failPermissions = PermissionUtil.getFailPermissions(permissions, grantResults);
            if (getArguments().getBoolean(PERMISSION_CONNECT)
                    && PermissionUtil.isRequestDeniedPermission(getActivity(), failPermissions)){
                requestPermission();
                return;
            }

            listener.noPermission(failPermissions,
                    PermissionUtil.checkMorePermanentDenied(getActivity(), failPermissions));

            if (!successPermissions.isEmpty()){
                listener.hasPermission(successPermissions, false);
            }
        }

        //请求完成，删除集合中元素
        sContainer.remove(requestCode);
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!isBackCall && requestCode == getArguments().getInt(PERMISSION_CODE)){
            isBackCall = true;
            //需要延迟执行，不然有些华为机型授权了但是获取不到权限
            getActivity().getWindow().getDecorView().postDelayed(this, 500);
        }
    }

    @Override
    public void run() {
        //请求其他危险权限
        requestPermission();
    }

    /**
     * 请求权限
     */
    private void requestPermission(){
        if (PermissionUtil.isOverM()){
            ArrayList<String> permissions = getArguments().getStringArrayList(PERMISSION_GROUP);
            requestPermissions(permissions.toArray(new String[permissions.size() - 1]),
                    getArguments().getInt(PERMISSION_CODE));
        }
    }


}
