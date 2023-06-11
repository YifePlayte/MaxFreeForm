package com.yifeplayte.maxfreeform.hook.hooks.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Intent
import android.view.View.OnClickListener
import com.github.kyuubiran.ezxhelper.ClassUtils.getStaticObjectOrNullAs
import com.github.kyuubiran.ezxhelper.ClassUtils.invokeStaticMethodBestMatch
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.ClassUtils.setStaticObject
import com.github.kyuubiran.ezxhelper.EzXHelper.appContext
import com.github.kyuubiran.ezxhelper.EzXHelper.hostPackageName
import com.github.kyuubiran.ezxhelper.EzXHelper.initAppContext
import com.github.kyuubiran.ezxhelper.EzXHelper.moduleRes
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHooks
import com.github.kyuubiran.ezxhelper.ObjectUtils.invokeMethodBestMatch
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import com.yifeplayte.maxfreeform.hook.utils.Build.IS_TABLET

object AddFreeformShortcut : BaseHook() {
    override fun init() {
        if (IS_TABLET) initForPad() else initForPhone()
        loadClass("com.miui.home.launcher.shortcuts.SystemShortcutMenu").methodFinder()
            .filterByName("getMaxShortcutItemCount").toList().createHooks {
                after { param ->
                    param.result = 6
                }
            }
        loadClass("com.miui.home.launcher.shortcuts.AppShortcutMenu").methodFinder()
            .filterByName("getMaxShortcutItemCount").toList().createHooks {
                after { param ->
                    param.result = 6
                }
            }
        loadClass("com.miui.home.launcher.shortcuts.ShortcutMenuItem").methodFinder().filterByName("getShortTitle")
            .toList().createHooks {
                after { param ->
                    param.result = when (param.result) {
                        "应用信息" -> "信息"
                        "新建窗口" -> "多开"
                        else -> param.result
                    }
                }
            }
    }

    private fun initForPad() {
        loadClass("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem\$MultipleSmallWindowShortcutMenuItem").methodFinder()
            .filterByName("isValid").first().createHook { returnConstant(true) }
        loadClass("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem\$SmallWindowShortcutMenuItem").methodFinder()
            .filterByName("isValid").first().createHook { returnConstant(true) }
    }

    @SuppressLint("DiscouragedApi")
    private fun initForPhone() {
        val clazzSystemShortcutMenuItem = loadClass("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem")

        Activity::class.java.methodFinder().filterByName("onCreate").toList().createHooks {
            after {
                initAppContext(it.thisObject as Activity)
            }
        }
        loadClass("com.miui.home.launcher.util.ViewDarkModeHelper").methodFinder()
            .filterByName("onConfigurationChanged").toList().createHooks {
                after {
                    invokeStaticMethodBestMatch(
                        clazzSystemShortcutMenuItem, "createAllSystemShortcutMenuItems"
                    )
                }
            }
        loadClass("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem\$AppDetailsShortcutMenuItem").methodFinder()
            .filterByName("getOnClickListener").toList().createHooks {
                before { param ->
                    val shortTitle = invokeMethodBestMatch(param.thisObject, "getShortTitle")
                    when (shortTitle) {
                        in setOf(moduleRes.getString(R.string.freeform), moduleRes.getString(R.string.new_task)) -> {
                            param.result = OnClickListener { view ->
                                val context = view.context
                                val componentName = invokeMethodBestMatch(
                                    param.thisObject, "getComponentName"
                                ) as ComponentName
                                val intent = Intent().apply {
                                    action = "android.intent.action.MAIN"
                                    component = componentName
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    if (shortTitle == moduleRes.getString(R.string.new_task)) {
                                        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                                    }
                                }
                                invokeStaticMethodBestMatch(
                                    loadClass("com.miui.launcher.utils.ActivityUtilsCompat"),
                                    "makeFreeformActivityOptions",
                                    null,
                                    context,
                                    componentName.packageName
                                )?.let {
                                    context.startActivity(intent, (it as ActivityOptions).toBundle())
                                }
                            }
                        }
                    }
                }
            }
        clazzSystemShortcutMenuItem.methodFinder().filterByName("createAllSystemShortcutMenuItems").toList()
            .createHooks {
                after {
                    val mAllSystemShortcutMenuItems = getStaticObjectOrNullAs<List<Any>>(
                        clazzSystemShortcutMenuItem, "sAllSystemShortcutMenuItems"
                    )!!
                    val mSmallWindowInstance =
                        loadClass("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem\$AppDetailsShortcutMenuItem").newInstance()
                            .apply {
                                invokeMethodBestMatch(
                                    this, "setShortTitle", null, moduleRes.getString(R.string.freeform)
                                )
                                invokeMethodBestMatch(this, "setIconDrawable", null, appContext.let {
                                    it.getDrawable(
                                        it.resources.getIdentifier("ic_task_small_window", "drawable", hostPackageName)
                                    )
                                })
                            }
                    val mNewTaskInstance =
                        loadClass("com.miui.home.launcher.shortcuts.SystemShortcutMenuItem\$AppDetailsShortcutMenuItem").newInstance()
                            .apply {
                                invokeMethodBestMatch(
                                    this, "setShortTitle", null, moduleRes.getString(R.string.new_task)
                                )
                                invokeMethodBestMatch(this, "setIconDrawable", null, appContext.let {
                                    it.getDrawable(
                                        it.resources.getIdentifier("ic_task_add_pair", "drawable", hostPackageName)
                                    )
                                })
                            }
                    val sAllSystemShortcutMenuItems = ArrayList<Any>().apply {
                        add(mSmallWindowInstance)
                        add(mNewTaskInstance)
                        addAll(mAllSystemShortcutMenuItems)
                    }
                    setStaticObject(
                        clazzSystemShortcutMenuItem, "sAllSystemShortcutMenuItems", sAllSystemShortcutMenuItems
                    )
                }
            }
    }
}