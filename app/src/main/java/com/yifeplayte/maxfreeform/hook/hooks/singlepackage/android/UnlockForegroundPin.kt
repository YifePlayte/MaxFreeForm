package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.android

import android.util.Log
import android.view.SurfaceControl
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHooks
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.ObjectUtils.getObjectOrNull
import com.github.kyuubiran.ezxhelper.ObjectUtils.getObjectOrNullAs
import com.github.kyuubiran.ezxhelper.ObjectUtils.getObjectOrNullUntilSuperclassAs
import com.github.kyuubiran.ezxhelper.ObjectUtils.invokeMethodBestMatch
import com.github.kyuubiran.ezxhelper.ObjectUtils.setObject
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.hook.utils.XSharedPreferences.getStringSet
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField
import de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField

object UnlockForegroundPin : BaseHook() {
    override val key = "unlock_foreground_pin"
    override fun hook() = if (IS_HYPER_OS) initForHyperOS() else initForMIUI()
    private val clazzMiuiFreeFormGestureController by lazy {
        loadClass("com.android.server.wm.MiuiFreeFormGestureController")
    }

    /**
     * @author 焕晨HChen
     */
    private fun initForMIUI() {
        val clazzMiuiFreeFormActivityStack =
            loadClass("com.android.server.wm.MiuiFreeFormActivityStack")
        clazzMiuiFreeFormGestureController.methodFinder().filterByName("moveTaskToBack")
            .filterByAssignableParamTypes(clazzMiuiFreeFormActivityStack).first().createHook {
                before { param ->
                    val stack = param.args[0] ?: return@before
                    val mTask = getObjectOrNull(stack, "mTask") ?: return@before
                    val surfaceControl =
                        getObjectOrNullUntilSuperclassAs<SurfaceControl>(mTask, "mSurfaceControl")
                    if (surfaceControl?.isValid == true) {
                        val transaction = SurfaceControl.Transaction()
                        invokeMethodBestMatch(transaction, "hide", null, surfaceControl)
                        transaction.apply()
                        param.result = null
                    }
                }
            }
        val clazzTask = loadClass("com.android.server.wm.Task")
        clazzTask.declaredConstructors.createHooks {
            after {
                setAdditionalInstanceField(it.thisObject, "mLastSurfaceVisibility", true)
            }
        }
        clazzTask.methodFinder().filterByName("prepareSurfaces").filterByParamCount(0).first()
            .createHook {
                after { param ->
                    val thisObject = param.thisObject
                    val taskId = invokeMethodBestMatch(thisObject, "getRootTaskId") as Int
                    val inPinMode =
                        thisObject.objectHelper().getObjectOrNullUntilSuperclass("mWmService")
                            ?.objectHelper()?.getObjectOrNull("mAtmService")?.objectHelper()
                            ?.getObjectOrNull("mMiuiFreeFormManagerService")?.objectHelper()
                            ?.invokeMethodBestMatch("getMiuiFreeFormActivityStack", null, taskId)
                            ?.objectHelper()?.invokeMethodBestMatch("inPinMode") as Boolean?
                            ?: return@after
                    val mSurfaceControl = getObjectOrNullUntilSuperclassAs<SurfaceControl>(
                        thisObject, "mSurfaceControl"
                    ) ?: return@after
                    val transaction = invokeMethodBestMatch(
                        thisObject, "getSyncTransaction"
                    ) as SurfaceControl.Transaction
                    if (inPinMode) {
                        if (thisObject.objectHelper()
                                .invokeMethodBestMatch("isAnimating", null, 7) as Boolean
                        ) return@after
                        transaction.objectHelper()
                            .invokeMethodBestMatch("setVisibility", null, mSurfaceControl, false)
                        setAdditionalInstanceField(thisObject, "mLastSurfaceVisibility", false)
                    } else {
                        if (getAdditionalInstanceField(
                                thisObject, "mLastSurfaceVisibility"
                            ) as Boolean
                        ) return@after
                        transaction.objectHelper()
                            .invokeMethodBestMatch("setVisibility", null, mSurfaceControl, true)
                        setAdditionalInstanceField(thisObject, "mLastSurfaceVisibility", true)
                    }
                }
            }
        clazzMiuiFreeFormGestureController.methodFinder().filterByName("moveTaskToFront")
            .filterByAssignableParamTypes(clazzMiuiFreeFormActivityStack).first()
            .createHook { returnConstant(null) }
        loadClass("com.android.server.wm.MiuiFreeformPinManagerService").methodFinder()
            .filterByName("lambda\$unPinFloatingWindow\$0\$com-android-server-wm-MiuiFreeformPinManagerService")
            .first().createHook {
                replace { param ->
                    val miuiFreeFormActivityStack = param.args[0]
                    val activityRecord = invokeMethodBestMatch(
                        getObjectOrNull(miuiFreeFormActivityStack, "mTask")!!,
                        "getTopNonFinishingActivity"
                    ) ?: return@replace null

                    // 遵循安卓日志
                    Log.i(
                        "MiuiFreeformPinManagerService",
                        "unPinFloatingWindow mffas: $miuiFreeFormActivityStack activityRecord: $activityRecord"
                    )

                    setObject(miuiFreeFormActivityStack, "mEnterVelocityX", param.args[1])
                    setObject(miuiFreeFormActivityStack, "mEnterVelocityY", param.args[2])
                    setObject(miuiFreeFormActivityStack, "mIsEnterClick", param.args[3])
                    setObject(miuiFreeFormActivityStack, "mIsPinFloatingWindowPosInit", false)
                    invokeMethodBestMatch(
                        param.thisObject,
                        "setUpMiuiFreeWindowFloatIconAnimation",
                        null,
                        miuiFreeFormActivityStack,
                        activityRecord,
                        param.args[4],
                        param.args[5]
                    )
                    invokeMethodBestMatch(
                        param.thisObject, "startUnPinAnimation", null, miuiFreeFormActivityStack
                    )
                }
            }
        clazzMiuiFreeFormGestureController.methodFinder()
            .filterByName("lambda\$startFullscreenFromFreeform\$2\$com-android-server-wm-MiuiFreeFormGestureController")
            .first().createHook {
                replace { param ->
                    val miuiFreeFormActivityStack = param.args[0]
                    val mGestureListener = getObjectOrNull(param.thisObject, "mGestureListener")!!
                    val mPinedStartTime =
                        getObjectOrNullAs<Long>(miuiFreeFormActivityStack, "mPinedStartTime") ?: 0L
                    val passedTime =
                        if (mPinedStartTime != 0L) (System.currentTimeMillis() - mPinedStartTime).toFloat() / 1000.0f else 0.0f
                    val stackPackageName =
                        invokeMethodBestMatch(miuiFreeFormActivityStack, "getStackPackageName")
                    val applicationName = invokeMethodBestMatch(
                        miuiFreeFormActivityStack, "getApplicationName"
                    )
                    if (invokeMethodBestMatch(
                            miuiFreeFormActivityStack, "isInFreeFormMode"
                        ) as Boolean
                    ) {
                        invokeMethodBestMatch(
                            mGestureListener,
                            "startFullScreenFromFreeFormAnimation",
                            null,
                            miuiFreeFormActivityStack
                        )
                        val mTrackManager = getObjectOrNull(param.thisObject, "mTrackManager")
                        if (mTrackManager != null) {
                            invokeMethodBestMatch(
                                mTrackManager,
                                "trackSmallWindowPinedQuitEvent",
                                null,
                                stackPackageName,
                                applicationName,
                                passedTime
                            )
                        }
                    } else if (invokeMethodBestMatch(
                            miuiFreeFormActivityStack, "isInMiniFreeFormMode"
                        ) as Boolean
                    ) {
                        invokeMethodBestMatch(
                            mGestureListener,
                            "startFullScreenFromSmallAnimation",
                            null,
                            miuiFreeFormActivityStack
                        )
                        val mTrackManager = getObjectOrNull(param.thisObject, "mTrackManager")
                        if (mTrackManager != null) {
                            invokeMethodBestMatch(
                                mTrackManager,
                                "trackMiniWindowPinedQuitEvent",
                                null,
                                stackPackageName,
                                applicationName,
                                passedTime
                            )
                        }
                    }
                    setObject(miuiFreeFormActivityStack, "mPinedStartTime", 0L)
                    invokeMethodBestMatch(miuiFreeFormActivityStack, "setInPinMode", null, false)
                }
            }
    }

    private fun initForHyperOS() {
        clazzMiuiFreeFormGestureController.methodFinder().filterByName("needForegroundPin").first()
            .createHook {
                before {
                    val packageName =
                        invokeMethodBestMatch(it.args[0], "getStackPackageName") as String
                    val unlockForegroundPinWhitelist =
                        getStringSet("unlock_foreground_pin_whitelist", setOf())
                    it.result = unlockForegroundPinWhitelist.contains(packageName)
                }
            }
    }
}