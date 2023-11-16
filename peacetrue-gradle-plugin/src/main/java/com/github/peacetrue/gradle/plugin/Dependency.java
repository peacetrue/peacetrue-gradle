package com.github.peacetrue.gradle.plugin;

/**
 * @author peace
 **/
interface Dependency {
    /** 依赖是否启用 */
    boolean enabled();

    /** 依赖 maven 坐标 */
    String gav();
}
