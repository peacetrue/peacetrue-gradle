package com.github.peacetrue.gradle.plugin;

import org.gradle.api.Plugin;

/**
 * @author peace
 **/
@Deprecated
interface PluginDependency extends Dependency {
    String getPluginName();
    Class<? extends Plugin> getPluginClass();
}
