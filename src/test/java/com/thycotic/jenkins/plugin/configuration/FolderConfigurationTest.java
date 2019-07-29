package com.thycotic.jenkins.plugin.configuration;

import com.thycotic.jenkins.configuration.DevOpsSecretsVaultConfiguration;
import com.thycotic.jenkins.configuration.DevOpsSecretsVaultFolderConfiguration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FolderConfigurationTest {

    @Test
    public void testDefaultConstructor() {
        DevOpsSecretsVaultFolderConfiguration config = new DevOpsSecretsVaultFolderConfiguration();
        assertThat(config.getConfiguration()).isNull();
    }

    @Test
    public void testConfigConstructor() {
        DevOpsSecretsVaultConfiguration parentConfig = new DevOpsSecretsVaultConfiguration("abc");
        DevOpsSecretsVaultFolderConfiguration config = new DevOpsSecretsVaultFolderConfiguration(parentConfig);
        assertThat(config.getConfiguration()).isNotNull();
    }
}
