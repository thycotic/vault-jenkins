package com.thycotic.jenkins.plugin;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.thycotic.jenkins.ThycoticVaultBuildWrapper;
import com.thycotic.jenkins.configuration.DevOpsSecretsVaultConfiguration;
import com.thycotic.jenkins.configuration.GlobalDevOpsSecretsVaultConfiguration;
import com.thycotic.jenkins.model.ThycoticSecret;
import com.thycotic.jenkins.model.ThycoticSecretValue;
import hudson.Functions;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.BatchFile;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Arrays;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DevOpsSecretsVaultIntegrationTest extends BaseIntegration {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(3333);

    public static final String GLOBAL_CREDENTIALS_ID_1 = "global-1";
    public static final String GLOBAL_CREDENTIALS_ID_2 = "global-2";

    private Credentials GLOBAL_CREDENTIAL_1;
    private Credentials GLOBAL_CREDENTIAL_2;

    private FreeStyleProject project;

    @Before
    public void init() throws Exception {
        GlobalDevOpsSecretsVaultConfiguration globalConfig = GlobalConfiguration.all().get(GlobalDevOpsSecretsVaultConfiguration.class);
        globalConfig.setConfiguration(new DevOpsSecretsVaultConfiguration(GLOBAL_CREDENTIALS_ID_1));

        globalConfig.save();


        GLOBAL_CREDENTIAL_1 = createTokenCredential(GLOBAL_CREDENTIALS_ID_1);
        GLOBAL_CREDENTIAL_2 = createTokenCredential(GLOBAL_CREDENTIALS_ID_2);

        SystemCredentialsProvider.getInstance().setDomainCredentialsMap(Collections.singletonMap(Domain.global(), Arrays
                .asList(GLOBAL_CREDENTIAL_1, GLOBAL_CREDENTIAL_2)));

        this.project = jenkins.createFreeStyleProject("test");

        setupStubs();
    }

    @Test
    public void testGlobalConfiguration() throws Exception {
        ThycoticVaultBuildWrapper wrapper = new ThycoticVaultBuildWrapper(getSecrets());
        final String command = "echo $secret";
        this.project.getBuildWrappersList().add(wrapper);

        Builder step = Functions.isWindows() ? new BatchFile(command) : new Shell(command);
        this.project.getBuildersList().add(step);

        FreeStyleBuild build = this.jenkins.buildAndAssertSuccess(this.project);
        assertThat(wrapper.getConfiguration().getThycoticCredentialId(), is(GLOBAL_CREDENTIALS_ID_1));
        this.jenkins.assertBuildStatus(Result.SUCCESS, build);
        verify(getRequestedFor(urlEqualTo("/v1/secrets/SECRET")));
    }

    @Test
    public void testPipeline() throws Exception {
        WorkflowJob project = this.jenkins.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(getPipelineScript(GLOBAL_CREDENTIALS_ID_1), true));

        // Enqueue a build of the Pipeline, wait for it to complete, and assert success
        WorkflowRun build = this.jenkins.buildAndAssertSuccess(project);

        // Assert that the console log contains the output we expect
        this.jenkins.assertLogContains("hello", build);
    }

    @Test
    public void testPipelineNoCredentials() throws Exception {
        WorkflowJob project = this.jenkins.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition(getPipelineScript(null), true));

        // Enqueue a build of the Pipeline, wait for it to complete, and assert success
        WorkflowRun build = this.jenkins.buildAndAssertSuccess(project);

        // Assert that the console log contains the output we expect
        this.jenkins.assertLogContains("hello", build);
    }

    @Test
    public void testSecretFailure() throws Exception {
        ThycoticVaultBuildWrapper wrapper = new ThycoticVaultBuildWrapper(Collections.singletonList(new ThycoticSecret("BAD", Collections.singletonList(new ThycoticSecretValue("key", "$secret")))));
        final String command = "echo $secret";
        this.project.getBuildWrappersList().add(wrapper);

        Builder step = Functions.isWindows() ? new BatchFile(command) : new Shell(command);
        this.project.getBuildersList().add(step);

        FreeStyleBuild build = this.project.scheduleBuild2(0).get();
        assertThat(wrapper.getConfiguration().getThycoticCredentialId(), is(GLOBAL_CREDENTIALS_ID_1));
        this.jenkins.assertBuildStatus(Result.FAILURE, build);
        verify(getRequestedFor(urlEqualTo("/v1/secrets/BAD")));
    }
}
