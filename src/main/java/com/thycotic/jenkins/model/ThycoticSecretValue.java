package com.thycotic.jenkins.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class ThycoticSecretValue extends AbstractDescribableImpl<ThycoticSecretValue> {
    private String key;
    private String envVar;

    @DataBoundConstructor
    public ThycoticSecretValue(String key, String envVar) {
        this.key = key;
        this.envVar = envVar;
    }

    public String getKey() {
        return key;
    }

    public String getEnvVar() {
        return envVar;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<ThycoticSecretValue> {

        @Override
        public String getDisplayName() {
            return "Thycotic ThycoticSecretValue";
        }

    }
}
