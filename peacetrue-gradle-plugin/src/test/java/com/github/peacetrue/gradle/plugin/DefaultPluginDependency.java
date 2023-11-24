package com.github.peacetrue.gradle.plugin;

import org.gradle.api.Plugin;

import java.util.Map;

/**
 * @author peace
 **/
@Deprecated
class DefaultPluginDependency extends DefaultDependency implements PluginDependency {

    private final String pluginName;
    private final Class<? extends Plugin> pluginClass;

    DefaultPluginDependency(Map<String, ?> properties, String name, String defaultVersion, boolean defaultEnabled, String versionProperty, String switchProperty, String pluginName, Class<? extends Plugin> pluginClass) {
        super(properties, name, defaultVersion, defaultEnabled, versionProperty, switchProperty, false);
        this.pluginName = pluginName;
        this.pluginClass = pluginClass;
    }

    @Override
    public String getPluginName() {
        return pluginName;
    }

    @Override
    public Class<? extends Plugin> getPluginClass() {
        return pluginClass;
    }
}
