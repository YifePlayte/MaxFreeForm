package com.yifeplayte.maxfreeform.hook.hooks.singlepackage.android

import com.yifeplayte.maxfreeform.hook.hooks.BaseHook
import io.github.lingqiqi5211.ezhooktool.core.findAllMethods
import io.github.lingqiqi5211.ezhooktool.core.findMethodOrNull
import io.github.lingqiqi5211.ezhooktool.core.loadClass
import io.github.lingqiqi5211.ezhooktool.xposed.dsl.createHook

@Suppress("unused")
object UnlockFreeformQuantityLimit : BaseHook() {
    override val key = "unlock_freeform_quantity_limit"
    override val isEnabled = true

    /*
     * 游戏小窗另有一条与小窗数量上限无关的互斥逻辑：新增游戏小窗时会把已有游戏小窗
     * 直接退出（桌面模式下则是踢掉最底层游戏窗），判定依据是
     * MiuiFreeFormActivityStack.isGameApp()。该方法在系统框架里共 5 个调用点，
     * 只有 needForegroundPin 依赖它给游戏窗前台挂起资格，必须保持原值，
     * 因此按调用栈区分，命中下面这些调用点的才按非游戏返回。
     */
    private val GAME_LIMIT_CALLERS = setOf(
        // 非桌面模式：新增游戏窗时踢掉其它游戏窗，以及遍历已有窗时的游戏判定
        "com.android.server.wm.MiuiFreeFormStackDisplayStrategy#onMiuiFreeFormStasckAdded",
        // 桌面模式：游戏窗计数、踢掉最底层游戏窗
        "com.android.server.wm.MiuiFreeFormManagerService#getGameFreeFormCount",
    )

    // lambda 名字带编译器分配的序号，只比对前缀
    private val GAME_LIMIT_CALLER_PREFIXES = setOf(
        "com.android.server.wm.MiuiFreeFormManagerService" +
                "#lambda\$getBottomGameFreeFormActivityStack",
    )

    private fun isGameLimitCaller(): Boolean {
        val stack = Throwable().stackTrace
        for (frame in stack) {
            val caller = frame.className + "#" + frame.methodName
            if (caller in GAME_LIMIT_CALLERS) return true
            if (GAME_LIMIT_CALLER_PREFIXES.any { caller.startsWith(it) }) return true
        }
        return false
    }

    override fun hook() {
        val clazzMiuiFreeFormStackDisplayStrategy =
            loadClass("com.android.server.wm.MiuiFreeFormStackDisplayStrategy")
        clazzMiuiFreeFormStackDisplayStrategy.findAllMethods { notAbstract() }
            .filter {
                it.name == "getMaxMiuiFreeFormStackCount" ||
                        it.name == "getMaxMiuiFreeFormStackCountForFlashBack"
            }.forEach { method ->
            method.createHook { returnConstant(256) }
        }
        clazzMiuiFreeFormStackDisplayStrategy
            .findMethodOrNull { name("shouldStopStartFreeform") }
            ?.createHook { returnConstant(false) }

        runCatching {
            loadClass("com.android.server.wm.MiuiFreeFormActivityStack")
                .findMethodOrNull { name("isGameApp"); notAbstract() }
                ?.createHook {
                    after { param ->
                        // 原方法已判定为非游戏，无需再区分调用来源
                        if (param.result != true) return@after
                        if (isGameLimitCaller()) param.result = false
                    }
                }
        }
    }
}
