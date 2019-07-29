package com.thycotic.jenkins.configuration;

import hudson.ExtensionPoint;
import hudson.model.Item;

import javax.annotation.Nonnull;

public abstract class DevOpsSecretsVaultConfigResolver implements ExtensionPoint {
    public abstract @Nonnull
    DevOpsSecretsVaultConfiguration forJob(@Nonnull Item job);
}
