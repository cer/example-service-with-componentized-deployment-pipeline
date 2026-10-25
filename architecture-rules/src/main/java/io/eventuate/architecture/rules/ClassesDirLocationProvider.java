package io.eventuate.architecture.rules;

import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.LocationProvider;

import java.nio.file.Path;
import java.util.Set;

// The main classes directory of the project under analysis
public class ClassesDirLocationProvider implements LocationProvider {

    @Override
    public Set<Location> get(Class<?> testClass) {
        return Set.of(Location.of(Path.of(RoleContext.fromSystemProperties().classesDir())));
    }
}
