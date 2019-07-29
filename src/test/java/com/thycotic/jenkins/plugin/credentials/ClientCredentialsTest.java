package com.thycotic.jenkins.plugin.credentials;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.thycotic.jenkins.credentials.ClientCredentials;
import hudson.util.Secret;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientCredentialsTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testConstructor() {
        Secret secret = Secret.fromString("clientSecret");
        ClientCredentials creds = new ClientCredentials(CredentialsScope.USER, "abc", "test", "tenant", "clientId", secret, "devbambe.com");

        assertThat(creds.getTenant()).isEqualTo("tenant");
        assertThat(creds.getClientId()).isEqualTo("clientId");
        assertThat(creds.getClientSecret()).isEqualTo(secret);
        assertThat(creds.getVaultClient()).isNotNull();
        assertThat(creds.getBaseURL()).isEqualTo("devbambe.com");
    }
}
