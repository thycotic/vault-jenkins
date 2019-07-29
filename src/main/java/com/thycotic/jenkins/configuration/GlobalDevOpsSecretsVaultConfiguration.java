package com.thycotic.jenkins.configuration;

import hudson.Extension;
import hudson.model.Item;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

@Extension
public class GlobalDevOpsSecretsVaultConfiguration extends GlobalConfiguration {
    private DevOpsSecretsVaultConfiguration configuration;

    @Nonnull
    public static GlobalDevOpsSecretsVaultConfiguration get() {
        GlobalDevOpsSecretsVaultConfiguration instance = GlobalConfiguration.all().get(GlobalDevOpsSecretsVaultConfiguration.class);
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    public GlobalDevOpsSecretsVaultConfiguration() {
        load();
    }

    public DevOpsSecretsVaultConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        return true;
    }

    @DataBoundSetter
    public void setConfiguration(DevOpsSecretsVaultConfiguration configuration) {
        this.configuration = configuration;
        save();
    }

    @Extension(ordinal = 0)
    public static class ForJob extends DevOpsSecretsVaultConfigResolver {

        @Nonnull
        @Override
        public DevOpsSecretsVaultConfiguration forJob(@Nonnull Item job) {
            return GlobalDevOpsSecretsVaultConfiguration.get().getConfiguration();
        }
    }
}
