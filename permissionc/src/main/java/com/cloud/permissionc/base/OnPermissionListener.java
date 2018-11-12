package com.cloud.permissionc.base;

import java.util.List;

/**
 * @author xb.zou
 * @date 2018/11/12
 * @desc 权限的请求回调
 **/
public interface OnPermissionListener {
    /**
     * 有权限被授予时回调
     * @param granted   请求成功的权限组
     * @param isAll     是否全部都被授权成功
     */
    void hasPermission(List<String> granted, boolean isAll);

    /**
     * 有权限被拒绝时回调
     * @param denied    请求失败的权限组
     * @param quick     是否有某个权限被永久拒绝
     */
    void noPermission(List<String> denied, boolean quick);
}
