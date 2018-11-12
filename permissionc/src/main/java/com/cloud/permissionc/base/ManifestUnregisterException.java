package com.cloud.permissionc.base;

/**
 * @author xb.zou
 * @date 2018/11/12
 * @desc
 **/
public class ManifestUnregisterException extends RuntimeException {
    public ManifestUnregisterException(String permission) {
        super(permission == null ?
                "在Manifest文件中没有注册任何权限." :
                (permission + ": 没有在Manifest清单文件中注册."));
    }
}
