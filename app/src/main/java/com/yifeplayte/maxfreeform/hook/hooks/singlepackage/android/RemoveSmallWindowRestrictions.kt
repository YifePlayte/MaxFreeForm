package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.android

import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHooks
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.yifeplayte.maxfreeform.hook.hooks.BaseHook

@Suppress("unused")
object RemoveSmallWindowRestrictions : BaseHook() {
    override val key = "remove_small_window_restrictions"
    override fun hook() {
        runCatching {
            loadClass("android.app.ActivityTaskManager").methodFinder()
                .filterByName("supportsSplitScreen").filterNonAbstract().toList().createHooks {
                    returnConstant(true)
                }
        }

        runCatching {
            loadClass("com.android.server.wm.ActivityTaskManagerService").methodFinder()
                .filterByName("retrieveSettings").filterNonAbstract().toList().createHooks {
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
                }.filterNonAbstract().toList().createHooks {
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
                .filterByName("isForceResizeable").filterNonAbstract().toList().createHooks {
                    returnConstant(true)
                }
        }

        runCatching {
            loadClass("android.util.MiuiMultiWindowAdapter").methodFinder()
                .filter { name.contains("BlackList", true) }
                .filterByAssignableReturnType(MutableList::class.java).filterNonAbstract().toList()
                .apply {
                    createHooks {
                        returnConstant(mutableListOf<String>())
                    }
                }
        }

        // Author: LittleTurtle2333
        runCatching {
            loadClass("com.android.server.wm.Task").methodFinder().filterByName("isResizeable")
                .filterNonAbstract().toList().createHooks {
                    returnConstant(true)
                }
        }

        runCatching {
            loadClass("android.util.MiuiMultiWindowUtils").methodFinder()
                .filterByName("supportFreeform").filterNonAbstract().toList().createHooks {
                    returnConstant(true)
                }
        }
    }
}
