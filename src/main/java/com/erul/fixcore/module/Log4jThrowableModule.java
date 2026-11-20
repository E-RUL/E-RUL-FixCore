package com.erul.fixcore.module;

import com.erul.fixcore.FixKind;
import com.erul.fixcore.FixModule;

/**
 * Environment fix: log4j 2.0-beta9's {@code ThrowableProxy.loadClass} only catches {@code Exception},
 * while RFB throws {@code NoClassDefFoundError}/{@code NoClassDefFoundError} (an {@code Error}) while
 * rendering a stack trace. Printing an error then kills the game and masks the real cause.
 *
 * <p>Cannot be shipped in a mod jar, because log4j-core is instantiated by the bootstrap long before
 * FML mods are constructed.
 */
public class Log4jThrowableModule implements FixModule {

    @Override
    public String id() {
        return "log4j-throwable";
    }

    @Override
    public String title() {
        return "log4j 堆栈渲染二次崩溃修复";
    }

    @Override
    public FixKind kind() {
        return FixKind.ENV;
    }

    @Override
    public String[] requiredMods() {
        return new String[0];
    }

    @Override
    public String note() {
        return "替换 libraries/.../log4j-core-2.0-beta9-fixed.jar 内的 ThrowableProxy（catch Throwable）";
    }
}
