package org.apache.maven.plugins.enforcer.report;

import java.util.ArrayList;
import java.util.List;

public class DependencyConvergenceReport {
    private List<DependencyConvergenceViolation> dependencyConvergences = new ArrayList<DependencyConvergenceViolation>();

    public List<DependencyConvergenceViolation> getDependencyConvergencesViolations() {
        return dependencyConvergences;
    }

    public void setDependencyConvergencesViolations(List<DependencyConvergenceViolation> dependencyConvergences) {
        this.dependencyConvergences = dependencyConvergences;
    }
}
