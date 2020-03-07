package com.gluonhq.parameter;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

public class TargetLinkFlagParameter {

    @Parameter
    private List<String> flags;

    public List<String> getFlags() {
        return flags;
    }
}
