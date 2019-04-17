package com.catherine.materialdesignapp.listeners;

import java.util.List;

import androidx.annotation.Nullable;

public interface OnRequestPermissionsListener {
    /**
     * 用户开启权限
     */
    void onGranted();

    /**
     * 用户拒绝打开权限
     */
    void onDenied(@Nullable List<String> deniedPermissions);

    /**
     * 获取权限过程被中断，此处只要重新执行获取权限
     */
    void onRetry();
}
