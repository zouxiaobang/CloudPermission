package com.cloud.cloudpermission;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cloud.permissionc.EasyPermission;
import com.cloud.permissionc.base.OnPermissionListener;
import com.cloud.permissionc.base.PermissionCode;
import com.cloud.toastc.ToastUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void requestStorage(View view){
        EasyPermission.with(this)
                .permissions(PermissionCode.Group.STORAGE, PermissionCode.Group.CONTACTS)
                .permissions(PermissionCode.CAMERA)
                .request(new OnPermissionListener() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll){
                            ToastUtil.show("获取所有的权限成功");
                        } else {
                            ToastUtil.show("有部分权限没有获取成功");
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            ToastUtil.show("被永久拒绝授权，请手动授予权限");
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            EasyPermission.gotoPermissionSettings(MainActivity.this);
                        }else {
                            ToastUtil.show("获取权限失败");
                        }
                    }
                });
    }

    public void isRequest(View view){
        if (EasyPermission.isHasPermission(this, PermissionCode.Group.STORAGE)){
            ToastUtil.show("外存权限已经被授予");
        } else {
            ToastUtil.show("外存权限没有被授予");
        }
    }

    public void gotoSettings(View view){
        EasyPermission.gotoPermissionSettings(this, true);
    }
}
