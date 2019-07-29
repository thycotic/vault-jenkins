package com.thycotic.jenkins.plugin;

import com.thycotic.jenkins.credentials.ClientCredentials;
import com.thycotic.jenkins.model.ThycoticSecret;
import com.thycotic.jenkins.model.ThycoticSecretValue;
import com.thycotic.vault.client.BaseClient;
import hudson.Functions;

import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.*;

public abstract class BaseIntegration {
    protected String getPipelineScript(String credentials) {
        String cmd = Functions.isWindows() ? "bat" : "sh";
        if (credentials != null) {
            return "node {\n" +
                    "    // define the thycoticVaultSecrets and the env variables\n" +
                    "    def secretValues = [\n" +
                    "        [$class: 'ThycoticSecretValue', key: 'key', envVar: 'secret']\n" +
                    "    ]\n" +
                    "    def secrets = [\n" +
                    "        [$class: 'ThycoticSecret', path: 'SECRET', secretValues: secretValues]\n" +
                    "    ]\n" +
                    "\n" +
                    "  def configuration = [$class: 'DevOpsSecretsVaultConfiguration',\n" +
                    "                       thycoticCredentialId: '" + credentials + "']\n" +
                    "\n" +
                    "    // inside this block your credentials will be available as env variables\n" +
                    "    wrap([$class: 'ThycoticVaultBuildWrapper', configuration: configuration, thycoticVaultSecrets: secrets]) {\n" +
                    "        " + cmd + " \"echo $secret\"\n" +
                    "    }\n" +
                    "}";
        } else {
            return "node {\n" +
                    "    // define the thycoticVaultSecrets and the env variables\n" +
                    "    def secretValues = [\n" +
                    "        [$class: 'ThycoticSecretValue', key: 'key', envVar: 'secret']\n" +
                    "    ]\n" +
                    "    def secrets = [\n" +
                    "        [$class: 'ThycoticSecret', path: 'SECRET', secretValues: secretValues]\n" +
                    "    ]\n" +
                    "\n" +
                    "    // inside this block your credentials will be available as env variables\n" +
                    "    wrap([$class: 'ThycoticVaultBuildWrapper', thycoticVaultSecrets: secrets]) {\n" +
                    "        " + cmd + " \"echo $secret\"\n" +
                    "    }\n" +
                    "}";
        }
    }

    protected List<ThycoticSecret> getSecrets() {

        return Collections.singletonList(new ThycoticSecret("SECRET", Collections.singletonList(new ThycoticSecretValue("key", "secret"))));
    }

    protected static ClientCredentials createTokenCredential(final String credentialId) {
        ClientCredentials cred = mock(ClientCredentials.class, withSettings().serializable());
        when(cred.getId()).thenReturn(credentialId);
        when(cred.getDescription()).thenReturn("description");
        when(cred.getVaultClient()).thenReturn(getClient());
        return cred;
    }

    protected static BaseClient getClient() {
        BaseClient client = new BaseClient("test", "testId", "testSecret", "devbambe.com");
        client.overrideVaultUrl("http://localhost:3333");
        return client;
    }

    protected void setupStubs() throws Exception {
        stubFor(post(urlEqualTo("/v1/token"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getFakeToken())));

        stubFor(get(urlEqualTo("/v1/secrets/SECRET"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": \"42b4e55a-1c22-40e8-9ed9-d5bc4e1dd326\", \"path\": \"secret\", \"type\": \"string\", \"attributes\": null, \"data\": {\"key\":\"hello\" }}")));

        stubFor(get(urlEqualTo("/v1/secrets/BAD"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(500)));
    }

    protected String getFakeToken() throws Exception {
        return "{ \"accessToken\": \"foo\", \"tokenType\": \"bearer\", \"expiresIn\": 3600, \"refreshToken\": \"bar\"}";
    }
}
