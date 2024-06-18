package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.android;

import android.os.Handler;
import android.os.Message;
import android.view.SurfaceControl;

import com.github.kyuubiran.ezxhelper.EzXHelper;
import com.github.kyuubiran.ezxhelper.Log;
import com.github.kyuubiran.ezxhelper.ObjectUtils;
import com.yifeplayte.maxfreeform.hook.utils.XSharedPreferences;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class MiuiUnlockForegroundPin {
    private final HandlerHelper handlerHelper = new HandlerHelper();
    private static final int TOP_WINDOW_HAS_DRAWN = 1;
    private static Set<String> sets = XSharedPreferences.INSTANCE.getStringSet("unlock_foreground_pin_whitelist", new HashSet<>());

    /**
     * @author 焕晨HChen
     */
    public void initForMIUI() {
        ClassLoader classLoader = EzXHelper.getClassLoader();
        XposedHelpers.findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController",
                classLoader, "moveTaskToBack",
                "com.android.server.wm.MiuiFreeFormActivityStack",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        sets = XSharedPreferences.INSTANCE.getStringSet("unlock_foreground_pin_whitelist", new HashSet<>());
                        if (sets.stream().anyMatch(pkg ->
                                pkg.equals(getPackageName(param)))) {
                            param.setResult(null);
                        }
                    }
                });

        XposedBridge.hookAllConstructors(
                XposedHelpers.findClass("com.android.server.wm.Task",
                        classLoader), new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        XposedHelpers.setAdditionalInstanceField(param.thisObject,
                                "mLastSurfaceVisibility",
                                false);

                    }
                });

        XposedHelpers.findAndHookMethod("com.android.server.wm.Task", classLoader,
                "prepareSurfaces", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        String pkg = (String) XposedHelpers.callMethod(param.thisObject, "getPackageName");
                        if (pkg != null) {
                            if (sets.stream().noneMatch(p ->
                                    p.equals(pkg))) {
                                return;
                            }
                        }
                        SurfaceControl.Transaction transaction = (SurfaceControl.Transaction)
                                XposedHelpers.callMethod(param.thisObject, "getSyncTransaction");
                        SurfaceControl mSurfaceControl = (SurfaceControl)
                                XposedHelpers.getObjectField(param.thisObject, "mSurfaceControl");
                        int taskId = (int) XposedHelpers.callMethod(param.thisObject, "getRootTaskId");
                        Object mWmService = XposedHelpers.getObjectField(param.thisObject, "mWmService");
                        Object mAtmService = XposedHelpers.getObjectField(mWmService, "mAtmService");
                        Object mMiuiFreeFormManagerService = XposedHelpers.getObjectField(mAtmService, "mMiuiFreeFormManagerService");
                        Object mffas = XposedHelpers.callMethod(mMiuiFreeFormManagerService, "getMiuiFreeFormActivityStack", taskId);
                        boolean isVisible = (boolean) XposedHelpers.callMethod(param.thisObject, "isVisible");
                        boolean isAnimating = (boolean) XposedHelpers.callMethod(param.thisObject, "isAnimating", 7);
                        boolean inPinMode = false;
                        if (mffas != null) {
                            inPinMode = (boolean) XposedHelpers.callMethod(mffas, "inPinMode");
                        }
                        boolean mLastSurfaceVisibility = (boolean) XposedHelpers.getAdditionalInstanceField(param.thisObject, "mLastSurfaceVisibility");
                        if (mSurfaceControl != null && mffas != null && inPinMode) {
                            if (!isAnimating) {
                                XposedHelpers.callMethod(transaction, "setVisibility", mSurfaceControl, false);
                                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLastSurfaceVisibility", false);
                            }
                        } else if (mSurfaceControl != null && mffas != null && !inPinMode) {
                            if (!mLastSurfaceVisibility) {
                                XposedHelpers.callMethod(transaction, "setVisibility", mSurfaceControl, true);
                                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLastSurfaceVisibility", true);
                            }
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController", classLoader,
                "moveTaskToFront",
                "com.android.server.wm.MiuiFreeFormActivityStack",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (sets.stream().anyMatch(p ->
                                p.equals(getPackageName(param)))) {
                            param.setResult(null);
                        }
                    }
                });

        XposedHelpers.findAndHookMethod("com.android.server.wm.MiuiFreeformPinManagerService", classLoader,
                "lambda$unPinFloatingWindow$0$com-android-server-wm-MiuiFreeformPinManagerService",
                "com.android.server.wm.MiuiFreeFormActivityStack",
                float.class, float.class, boolean.class, "com.android.server.wm.DisplayContent",
                "com.android.server.wm.MiuiFreeFormFloatIconInfo",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (sets.stream().anyMatch(p ->
                                p.equals(getPackageName(param))))
                            handlerHelper.sendMessageDelayed(
                                    handlerHelper.obtainMessage(TOP_WINDOW_HAS_DRAWN, param),
                                    150);
                    }
                });

        XposedHelpers.findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController", classLoader,
                "lambda$startFullscreenFromFreeform$2$com-android-server-wm-MiuiFreeFormGestureController",
                "com.android.server.wm.MiuiFreeFormActivityStack",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (sets.stream().anyMatch(p ->
                                p.equals(getPackageName(param))))
                            handlerHelper.sendMessageDelayed(
                                    handlerHelper.obtainMessage(TOP_WINDOW_HAS_DRAWN, param),
                                    150);
                    }
                });
    }

    private String getPackageName(XC_MethodHook.MethodHookParam param) {
        Object mffas = param.args[0];
        if (mffas != null) {
            return (String) XposedHelpers.callMethod(mffas,
                    "getStackPackageName");
        }
        return null;
    }

    private static class HandlerHelper extends Handler {
        @Override
        public void handleMessage(@NotNull Message msg) {
            if (msg.what == TOP_WINDOW_HAS_DRAWN) {
                XC_MethodHook.MethodHookParam param =
                        (XC_MethodHook.MethodHookParam) msg.obj;
                Object mffas = param.args[0];
                XposedHelpers.setObjectField(mffas, "topWindowHasDrawn", true);
                try {
                    Object mLock = ObjectUtils.getObjectOrNull(param.thisObject, "mLock");
                    if (mLock == null) {
                        Object mMiuiFreeformPinManagerService = ObjectUtils.getObjectOrNull(param.thisObject,
                                "mMiuiFreeformPinManagerService");
                        mLock = ObjectUtils.getObjectOrNull(mMiuiFreeformPinManagerService, "mLock");
                    }
                    if (mLock != null) {
                        synchronized (mLock) {
                            mLock.notifyAll();
                        }
                    }
                } catch (Throwable e) {
                    Log.e("MiuiUnlockForegroundPin: HandlerHelper broken in handleMessage()", e);
                }
            }
        }
    }
}

