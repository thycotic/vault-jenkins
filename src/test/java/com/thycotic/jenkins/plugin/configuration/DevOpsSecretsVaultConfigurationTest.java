package com.thycotic.jenkins.plugin.configuration;

import com.thycotic.jenkins.configuration.DevOpsSecretsVaultConfiguration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DevOpsSecretsVaultConfigurationTest {

    @Test
    public void testMerge() {
        DevOpsSecretsVaultConfiguration config = new DevOpsSecretsVaultConfiguration();
        DevOpsSecretsVaultConfiguration parent = new DevOpsSecretsVaultConfiguration("abc");

        assertThat(config.getThycoticCredentialId()).isNullOrEmpty();
        config = config.mergeWithParent(parent);

        assertThat(config.getThycoticCredentialId()).isEqualTo("abc");
    }
}
