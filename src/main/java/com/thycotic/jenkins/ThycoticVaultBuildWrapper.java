package com.thycotic.jenkins;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;
import com.thycotic.jenkins.configuration.DevOpsSecretsVaultConfigResolver;
import com.thycotic.jenkins.configuration.DevOpsSecretsVaultConfiguration;
import com.thycotic.jenkins.credentials.ThycoticVaultCredentials;
import com.thycotic.jenkins.model.ThycoticSecret;
import com.thycotic.jenkins.model.ThycoticSecretValue;
import com.thycotic.vault.exceptions.DevOpsSecretsVaultException;
import com.thycotic.vault.secret.BaseSecretService;
import com.thycotic.vault.secret.SecretService;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.tasks.SimpleBuildWrapper;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class ThycoticVaultBuildWrapper extends SimpleBuildWrapper {

    private List<ThycoticSecret> thycoticVaultSecrets;
    private DevOpsSecretsVaultConfiguration configuration;

    private static final Logger LOGGER = Logger.getLogger("com.thycotic.jenkins.ThycoticVaultBuildWrapper");

    @DataBoundConstructor
    public ThycoticVaultBuildWrapper(@CheckForNull List<ThycoticSecret> thycoticVaultSecrets) {
        this.thycoticVaultSecrets = thycoticVaultSecrets;
    }

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath filePath, Launcher launcher, TaskListener taskListener, EnvVars envVars) throws IOException, InterruptedException {
        PrintStream logger = taskListener.getLogger();
        updateConfig(build);
        ThycoticVaultCredentials credential = getCredentials(build);

        if (thycoticVaultSecrets != null && !thycoticVaultSecrets.isEmpty()) {
            try {
                populateSecrets(context, credential);
            } catch (Exception e) {
                e.printStackTrace(logger);
                throw new AbortException(e.getMessage());
            }
        }
    }

    public List<ThycoticSecret> getThycoticVaultSecrets() {
        return thycoticVaultSecrets;
    }

    @DataBoundSetter
    public void setConfiguration(DevOpsSecretsVaultConfiguration configuration) {
        this.configuration = configuration;
    }

    public DevOpsSecretsVaultConfiguration getConfiguration() {
        return configuration;
    }


    private ThycoticVaultCredentials getCredentials(Run build) {
        String id = getConfiguration().getThycoticCredentialId();
        if (StringUtils.isBlank(id)) {
            throw new RuntimeException("The credential id was not configured - please specify the credentials to use.");
        }
        List<ThycoticVaultCredentials> credentials = CredentialsProvider.lookupCredentials(ThycoticVaultCredentials.class, build.getParent(), ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
        ThycoticVaultCredentials credential = CredentialsMatchers.firstOrNull(credentials, new IdMatcher(id));

        if (credential == null) {
            throw new RuntimeException("The credential id was not configured - please specify the credentials to use.");
        }

        return credential;
    }

    private void populateSecrets(Context context, ThycoticVaultCredentials credentials) {
        SecretService secretService = new BaseSecretService(credentials.getVaultClient());

        try {
            for (ThycoticSecret secret : this.thycoticVaultSecrets) {
                Map<String, Object> secretMap = secretService.getSecretMap(secret.getPath());
                for (ThycoticSecretValue secretValue : secret.getSecretValues()) {
                    String env = secretValue.getEnvVar();
                    String value = (String) secretMap.get(secretValue.getKey());
                    context.env(env, value);
                }
            }
        } catch (DevOpsSecretsVaultException e) {
            throw new RuntimeException("Exception calling DevOps Secrets Vault Service", e);
        }
    }

    private void updateConfig(Run<?, ?> build) {
        for (DevOpsSecretsVaultConfigResolver resolver : ExtensionList.lookup(DevOpsSecretsVaultConfigResolver.class)) {
            if (configuration != null) {
                configuration = configuration.mergeWithParent(resolver.forJob(build.getParent()));
            } else {
                configuration = resolver.forJob(build.getParent());
            }
        }
        if (configuration == null) {
            throw new RuntimeException("No configuration found - please configure the DevOps Secrets Vault Plugin.");
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {
        public DescriptorImpl() {
            super(ThycoticVaultBuildWrapper.class);
            load();
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "Thycotic DevOps Secrets Vault Plugin";
        }
    }
}
