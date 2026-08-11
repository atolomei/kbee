package com.novamens.kbee.dependencies;

import java.util.Map;

public interface ObjectWithDependency {
    Map<String, Dependency> getDependencies();
}
