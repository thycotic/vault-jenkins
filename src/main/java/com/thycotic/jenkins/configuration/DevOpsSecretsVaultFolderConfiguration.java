package com.thycotic.jenkins.configuration;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class DevOpsSecretsVaultFolderConfiguration extends AbstractFolderProperty<AbstractFolder<?>> {
    private final DevOpsSecretsVaultConfiguration configuration;

    public DevOpsSecretsVaultFolderConfiguration() {
        this.configuration = null;
    }

    @DataBoundConstructor
    public DevOpsSecretsVaultFolderConfiguration(DevOpsSecretsVaultConfiguration configuration) {
        this.configuration = configuration;
    }

    public DevOpsSecretsVaultConfiguration getConfiguration() {
        return configuration;
    }

    @Extension
    public static class DescriptorImpl extends AbstractFolderPropertyDescriptor {
    }

    @Extension(ordinal = 100)
    public static class ForJob extends DevOpsSecretsVaultConfigResolver {
        @Nonnull
        @Override
        public DevOpsSecretsVaultConfiguration forJob(@Nonnull Item job) {
            DevOpsSecretsVaultConfiguration resultingConfig = null;
            for (ItemGroup g = job.getParent(); g instanceof AbstractFolder; g = ((AbstractFolder) g).getParent()) {
                DevOpsSecretsVaultFolderConfiguration folderProperty = ((AbstractFolder<?>) g).getProperties().get(DevOpsSecretsVaultFolderConfiguration.class);
                if (folderProperty == null) {
                    continue;
                }
                if (resultingConfig != null) {
                    resultingConfig = resultingConfig.mergeWithParent(folderProperty.getConfiguration());
                } else {
                    resultingConfig = folderProperty.getConfiguration();
                }
            }

            return resultingConfig;
        }
    }
}
