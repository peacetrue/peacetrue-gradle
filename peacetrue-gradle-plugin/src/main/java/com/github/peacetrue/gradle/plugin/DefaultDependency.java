package com.github.peacetrue.gradle.plugin;

import java.util.Map;
import java.util.Objects;

/**
 * @author peace
 **/
class DefaultDependency implements Dependency {

    private final Map<String, ?> properties;
    private final String name;
    private final String defaultVersion;
    private final boolean defaultEnabled;
    private final String versionProperty;
    private final String switchProperty;
    private final boolean supportSnapshot;

    public DefaultDependency(Map<String, ?> properties, String name, String defaultVersion, boolean defaultEnabled, String versionProperty, String switchProperty, boolean supportSnapshot) {
        this.properties = properties;
        this.name = name;
        this.defaultVersion = defaultVersion;
        this.defaultEnabled = defaultEnabled;
        this.versionProperty = versionProperty;
        this.switchProperty = switchProperty;
        this.supportSnapshot = supportSnapshot;
    }

    @Override
    public boolean enabled() {
        if (!properties.containsKey(switchProperty)) return defaultEnabled;
        return "true".equals(properties.get(switchProperty));
    }

    @Override
    public String gav() {
        String gav = name + ":" + Objects.toString(properties.get(versionProperty), defaultVersion);
        if (supportSnapshot && properties.containsKey("tailSnapshot")) gav += properties.get("tailSnapshot");
        return gav;
    }
}
