package org.apache.maven.plugins.enforcer.report;

import java.util.HashSet;
import java.util.Set;

public class DependencyConvergenceViolation {
    private Dependency dependency;
    private Set<String> versions = new HashSet<String>();

    public Dependency getDependency() {
        return dependency;
    }

    public void setDependency(Dependency dependency) {
        this.dependency = dependency;
    }

    public Set<String> getVersions() {
        return versions;
    }

    public void setVersions(Set<String> versions) {
        this.versions = versions;
    }
}
