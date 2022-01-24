/*
 * Copyright © 2017-2022 Ocado (Ocava)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ocadotechnology.config;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

class ConfigUsageChecker {
    private final HashSet<String> accessedProperties = new HashSet<>();
    private final HashSet<String> definedProperties = new HashSet<>();

    PropertiesAccessor checkAccessTo(Properties properties) {
        definedProperties.addAll(properties.stringPropertyNames());
        return key -> {
            accessedProperties.add(key);
            return properties.getProperty(key);
        };
    }

    Set<String> getUnrecognisedProperties() {
        return Sets.union(getUnrecognisedNonPrefixedProperties(), getUnrecognisedPrefixedProperties());
    }

    private Set<String> getUnrecognisedNonPrefixedProperties() {
        ImmutableSet<String> definedNonPrefixedProperties = definedProperties.stream()
                .filter(p -> !p.contains("@"))
                .collect(ImmutableSet.toImmutableSet());

        ImmutableSet<String> accessedNonPrefixedProperties = accessedProperties.stream()
                .filter(p -> !p.contains("@"))
                .collect(ImmutableSet.toImmutableSet());

        return Sets.difference(definedNonPrefixedProperties, accessedNonPrefixedProperties);
    }

    private Set<String> getUnrecognisedPrefixedProperties() {
        ImmutableSet<String> definedNonPrefixedProperties = accessedProperties.stream()
                .filter(p -> !p.contains("@"))
                .collect(ImmutableSet.toImmutableSet());
        return definedProperties.stream()
                .filter(p -> unrecognisedPrefixedProperty(p, definedNonPrefixedProperties))
                .collect(ImmutableSet.toImmutableSet());
    }

    private boolean unrecognisedPrefixedProperty(String property, Set<String> definedNonPrefixedProperties) {
        if (!property.contains("@")) {
            return false;
        }
        String propertyFromPrefixedProperty = extractPropertyFromPrefixedProperty(property);
        return !definedNonPrefixedProperties.contains(propertyFromPrefixedProperty);
    }

    private String extractPropertyFromPrefixedProperty(String property) {
        return property.contains("@") ?
                property.substring(property.lastIndexOf("@") + 1) :
                property;
    }
}
