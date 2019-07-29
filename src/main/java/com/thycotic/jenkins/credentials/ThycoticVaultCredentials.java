package com.thycotic.jenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.thycotic.vault.client.IDevOpsSecretsVaultClient;

import javax.annotation.Nonnull;
import java.io.Serializable;

@NameWith(ThycoticVaultCredentials.NameProvider.class)
public interface ThycoticVaultCredentials extends StandardCredentials, Serializable {

    IDevOpsSecretsVaultClient getVaultClient();

    class NameProvider extends CredentialsNameProvider<ThycoticVaultCredentials> {

        @Nonnull
        public String getName(@Nonnull ThycoticVaultCredentials credentials) {
            return credentials.getDescription();
        }
    }
}