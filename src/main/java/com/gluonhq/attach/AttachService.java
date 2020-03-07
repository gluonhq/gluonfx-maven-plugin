package com.gluonhq.attach;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class AttachService {

    public static final List<AttachService> GLUON_ATTACH_MODULES = new LinkedList<>();

    private static final String DEPENDENCY_GROUP = "com.gluonhq.attach";

    static {
        for (GluonAttachServices gluonAttachService : GluonAttachServices.values()) {
            String artifactId = gluonAttachService.getServiceName();

            GLUON_ATTACH_MODULES.add(
                    new AttachService(
                            DEPENDENCY_GROUP,
                            artifactId,
                            gluonAttachService.isAndroidSupported(),
                            gluonAttachService.isIosSupported(),
                            gluonAttachService.isDesktopSupported()));
        }
    }

    @Parameter(required = true)
    private String groupId;

    @Parameter(required = true)
    private String artifactId;

    @Parameter
    private boolean androidSupport = true;

    @Parameter
    private boolean iosSupport = true;

    @Parameter
    private boolean desktopSupport = false;

    public AttachService() {}

    public AttachService(
            String groupId,
            String artifactId,
            boolean androidSupport,
            boolean iosSupport,
            boolean desktopSupport) {

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.androidSupport = androidSupport;
        this.iosSupport = iosSupport;
        this.desktopSupport = desktopSupport;
    }

    public String getServiceName() {
        return artifactId.replace('_', '-').toLowerCase(Locale.ROOT);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public boolean isAndroidSupported() {
        return androidSupport;
    }

    public boolean isIosSupported() {
        return iosSupport;
    }

    public boolean isDesktopSupported() {
        return desktopSupport;
    }
}
