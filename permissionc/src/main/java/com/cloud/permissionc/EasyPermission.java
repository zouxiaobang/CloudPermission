package com.cloud.permissionc;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.cloud.permissionc.base.OnPermissionListener;
import com.cloud.permissionc.util.PermissionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author xb.zou
 * @date 2018/11/12
 * @desc 权限请求使用类
 **/
public class EasyPermission {
    /**
     * 当前Activity
     */
    private Activity mActivity;
    /**
     * 要请求的所有权限
     */
    private List<String> mPermissions = new ArrayList<>();
    /**
     * 授权失败时是否继续请求
     */
    private boolean isConnect;


    /**
     * 私有构造方法
     */
    private EasyPermission(Activity activity) {
        mActivity = activity;
    }

    /**
     * 设置请求对象
     */
    public static EasyPermission with(Activity activity) {
        return new EasyPermission(activity);
    }

    /**
     * 添加请求的权限
     */
    public EasyPermission permissions(String... permissions) {
        mPermissions.addAll(Arrays.asList(permissions));
        return this;
    }

    /**
     * 添加请求的权限组
     */
    public EasyPermission permissions(String[]... permissions) {
        for (String[] group : permissions) {
            mPermissions.addAll(Arrays.asList(group));
        }
        return this;
    }

    /**
     * 添加请求的权限组
     */
    public EasyPermission permissions(List<String> permissions) {
        mPermissions.addAll(permissions);
        return this;
    }

    /**
     * 设置请求失败时是否继续请求
     */
    public EasyPermission requestConnect(boolean isConnect) {
        this.isConnect = isConnect;
        return this;
    }

    public void request(OnPermissionListener listener) {
        if (mActivity == null) {
            throw new IllegalArgumentException("Activity参数不能为空.");
        }

        if (listener == null) {
            throw new IllegalArgumentException("OnPermissionListener回调不能为空.");
        }

        //如果没有指定请求的权限，就从Manifest文件中获取
        if (mPermissions == null || mPermissions.size() == 0) {
            mPermissions = PermissionUtil.getPermissionsByManifest(mActivity);
        }
        //Manifest文件中也没有权限，则抛异常
        if (mPermissions == null || mPermissions.size() == 0) {
            throw new IllegalArgumentException("请求的权限不能为空.");
        }

        Log.d("xb.zou", "request: 111111");
        PermissionUtil.checkTargetSdkVersion(mActivity, mPermissions);
        Log.d("xb.zou", "request: 222222");
        List<String> failPermissions = PermissionUtil.getFailPermissions(mActivity, mPermissions);
        if (failPermissions == null || failPermissions.size() == 0) {
            //权限都授权过
            listener.hasPermission(mPermissions, true);
        } else {
            PermissionUtil.checkPermissionsInManifest(mActivity, mPermissions);
            //申请没有授予过的权限
            PermissionManager
                    .newInstant((new ArrayList<>(mPermissions)), isConnect)
                    .prepareRequest(mActivity, listener);
        }
    }

    /**
     * 检测某些权限是否已经被授予
     */
    public static boolean isHasPermission(Context context, String... permissions) {
        List<String> failPermissions = PermissionUtil.getFailPermissions(context, Arrays.asList(permissions));
        return failPermissions == null || failPermissions.size() == 0;
    }

    /**
     * 检测某些权限是否已经被授予
     */
    public static boolean isHasPermission(Context context, String[]... permissions) {
        List<String> permissionList = new ArrayList<>();
        for (String[] group : permissions) {
            permissionList.addAll(Arrays.asList(group));
        }

        List<String> failPermissions = PermissionUtil.getFailPermissions(context, permissionList);
        return failPermissions == null || failPermissions.size() == 0;
    }

    /**
     * 跳转到权限设置页面
     */
    public static void gotoPermissionSettings(Context context) {
        PermissionSettingPage.start(context, false);
    }

    /**
     * 跳转到权限设置页面
     */
    public static void gotoPermissionSettings(Context context, boolean newTask) {
        PermissionSettingPage.start(context, newTask);
    }
}
