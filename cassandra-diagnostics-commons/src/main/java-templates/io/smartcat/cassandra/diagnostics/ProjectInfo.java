package io.smartcat.cassandra.diagnostics;

/**
 * Helper class that contains project-wide information.
 */
public final class ProjectInfo {

    private ProjectInfo() {
    }

    /**
     * Project version injected during maven build.
     */
    public static final String VERSION = "${project.parent.version}";
}
