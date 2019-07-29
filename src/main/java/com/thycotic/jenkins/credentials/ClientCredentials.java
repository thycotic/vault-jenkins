package com.thycotic.jenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.thycotic.vault.client.BaseClient;
import com.thycotic.vault.client.IDevOpsSecretsVaultClient;
import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class ClientCredentials extends BaseStandardCredentials implements ThycoticVaultCredentials {

    private final @Nonnull
    Secret clientSecret;

    private final @Nonnull
    String clientId;

    private final @Nonnull
    String tenant;

    private final @Nonnull
    String baseURL;

    private String urlOverride;

    @DataBoundConstructor
    public ClientCredentials(@CheckForNull CredentialsScope scope, @CheckForNull String id, @CheckForNull String description, @Nonnull String tenant, @Nonnull String clientId, @Nonnull Secret clientSecret, @Nonnull String baseURL) {
        super(scope, id, description);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tenant = tenant;
        this.baseURL = baseURL;
    }

    public String getClientId() {
        return clientId;
    }

    public Secret getClientSecret() {
        return clientSecret;
    }

    public String getTenant() {
        return tenant;
    }

    public String getBaseURL() {
        return baseURL;
    }

    @Override
    public IDevOpsSecretsVaultClient getVaultClient() {
        return new BaseClient(this.tenant, this.clientId, this.clientSecret.getPlainText(), this.baseURL);
    }

    @Extension
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

        @Override
        public String getDisplayName() {
            return "Thycotic Vault Client Credentials";
        }

    }
}
