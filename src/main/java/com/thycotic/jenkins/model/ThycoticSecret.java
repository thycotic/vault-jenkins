package com.thycotic.jenkins.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

public class ThycoticSecret extends AbstractDescribableImpl<ThycoticSecret> {

    private String path;
    private List<ThycoticSecretValue> secretValues;

    @DataBoundConstructor
    public ThycoticSecret(String path, List<ThycoticSecretValue> secretValues) {
        this.path = path;
        this.secretValues = secretValues;
    }

    public String getPath() {
        return path;
    }

    public List<ThycoticSecretValue> getSecretValues() {
        return secretValues;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<ThycoticSecret> {

        @Override
        public String getDisplayName() {
            return "Thycotic ThycoticSecret";
        }

    }
}
