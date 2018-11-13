# CloudPermission
## 特点
1、PermissionCode中已经封装好常用的权限以及权限组Group
2、通过回调的方式来通知调用者权限是否都赋予，省去原生代码的复杂度
3、提供查询某些（或某个）权限是否已经被赋予的功能
4、提供前往应用设置页面的功能
5、内部实现对API23、API26做相应处理，低于API23的不需要进行权限赋予
6、判断AndroidManifest.xml中权限的申请
## 用法
### 在项目的gradle文件中：
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

### 在模块的gradle文件中：
```
dependencies {
    implementation 'com.github.zouxiaobang:CloudPermission:Tag'
}
```

### 在java类中使用
```
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
```
注：这里需要在AndroidManifest中进行申请：
```
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.WRITE_CONTACTS"/>
    <uses-permission android:name="android.permission.READ_CONTACTS"/>
    <uses-permission android:name="android.permission.GET_ACCOUNTS"/>
```
