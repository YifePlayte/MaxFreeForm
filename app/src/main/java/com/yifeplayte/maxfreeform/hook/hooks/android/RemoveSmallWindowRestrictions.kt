package com.yifeplayte.maxfreeform.hook.hooks.android

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHooks
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook

object RemoveSmallWindowRestrictions : BaseHook() {
    override fun init() {
        runCatching {
            loadClass("com.android.server.wm.ActivityTaskManagerService").methodFinder()
                .filterByName("retrieveSettings").toList().createHooks {
                    after {
                        it.thisObject.objectHelper()
                            .setObject("mDevEnableNonResizableMultiWindow", true)
                    }
                }
        }

        runCatching {
            loadClass("com.android.server.wm.WindowManagerService\$SettingsObserver").methodFinder()
                .filter {
                    name in setOf("updateDevEnableNonResizableMultiWindow", "onChange")
                }.toList().createHooks {
                    after {
                        val this0 = it.thisObject.objectHelper().getObjectOrNull("this\$0")!!
                        val mAtmService = this0.objectHelper().getObjectOrNull("mAtmService")!!
                        mAtmService.objectHelper()
                            .setObject("mDevEnableNonResizableMultiWindow", true)
                    }
                }
        }

        runCatching {
            loadClass("android.util.MiuiMultiWindowUtils").methodFinder()
                .filterByName("isForceResizeable").first().createHook {
                    returnConstant(true)
                }
        }

        runCatching {
            loadClass("android.util.MiuiMultiWindowAdapter").methodFinder().filter {
                name.contains("BlackList", true)
            }.filterByAssignableReturnType(MutableList::class.java).toList().apply {
                createHooks {
                    returnConstant(mutableListOf<String>())
                }
            }
        }

        // Author: LittleTurtle2333
        runCatching {
            loadClass("com.android.server.wm.Task").methodFinder().filterByName("isResizeable")
                .first().createHook {
                    returnConstant(true)
                }
        }

        runCatching {
            loadClass("android.util.MiuiMultiWindowUtils").methodFinder()
                .filterByName("supportFreeform").first().createHook {
                    returnConstant(true)
                }
        }
    }
}
