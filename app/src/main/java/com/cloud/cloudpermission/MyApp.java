package com.cloud.cloudpermission;

import android.app.Application;

import com.cloud.toastc.ToastUtil;

/**
 * @author xb.zou
 * @date 2018/11/12
 * @desc
 **/
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ToastUtil.initToast(this);
    }
}
