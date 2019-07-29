package com.thycotic.jenkins.plugin;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainCredentials;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.thycotic.jenkins.configuration.DevOpsSecretsVaultConfiguration;
import com.thycotic.jenkins.configuration.GlobalDevOpsSecretsVaultConfiguration;
import hudson.model.FreeStyleProject;
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

import static com.thycotic.jenkins.plugin.DevOpsSecretsVaultIntegrationTest.GLOBAL_CREDENTIALS_ID_1;
import static com.thycotic.jenkins.plugin.DevOpsSecretsVaultIntegrationTest.GLOBAL_CREDENTIALS_ID_2;

public class DevOpsSecretsVaultFolderIntegrationTest extends BaseIntegration {

    private static final String FOLDER_1_CREDENTIALS_ID = "folder1";
    private static final String FOLDER_2_CREDENTIALS_ID = "folder2";

    private Credentials GLOBAL_CREDENTIAL_1;
    private Credentials GLOBAL_CREDENTIAL_2;

    private Credentials FOLDER_1_CREDENTIAL;
    private Credentials FOLDER_2_CREDENTIAL;

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(3333);

    private FreeStyleProject projectInFolder1;
    private FreeStyleProject projectInFolder2;
    private Folder folder1;
    private Folder folder2;

    @Before
    public void setupJenkins() throws Exception {
        GlobalDevOpsSecretsVaultConfiguration globalConfig = GlobalConfiguration.all().get(GlobalDevOpsSecretsVaultConfiguration.class);
        globalConfig.setConfiguration(new DevOpsSecretsVaultConfiguration(GLOBAL_CREDENTIALS_ID_1));

        globalConfig.save();

        FOLDER_1_CREDENTIAL = createTokenCredential(FOLDER_1_CREDENTIALS_ID);
        FOLDER_2_CREDENTIAL = createTokenCredential(FOLDER_2_CREDENTIALS_ID);

        FolderCredentialsProvider.FolderCredentialsProperty folder1CredProperty = new FolderCredentialsProvider.FolderCredentialsProperty(
                new DomainCredentials[]{new DomainCredentials(Domain.global(), Arrays.asList(FOLDER_1_CREDENTIAL))});

        FolderCredentialsProvider.FolderCredentialsProperty folder2CredProperty = new FolderCredentialsProvider.FolderCredentialsProperty(
                new DomainCredentials[]{new DomainCredentials(Domain.global(), Arrays.asList(FOLDER_2_CREDENTIAL))});

        GLOBAL_CREDENTIAL_1 = createTokenCredential(GLOBAL_CREDENTIALS_ID_1);
        GLOBAL_CREDENTIAL_2 = createTokenCredential(GLOBAL_CREDENTIALS_ID_2);

        SystemCredentialsProvider.getInstance().setDomainCredentialsMap(Collections.singletonMap(Domain.global(), Arrays
                .asList(GLOBAL_CREDENTIAL_1, GLOBAL_CREDENTIAL_2)));

        this.folder1 = jenkins.createProject(Folder.class, "folder1");
        this.folder2 = jenkins.createProject(Folder.class, "folder2");

        folder1.addProperty(folder1CredProperty);
        folder2.addProperty(folder2CredProperty);


        projectInFolder1 = this.folder1.createProject(FreeStyleProject.class, "projectInFolder1");
        projectInFolder2 = this.folder2.createProject(FreeStyleProject.class, "projectInFolder2");

        setupStubs();
    }

    @Test
    public void testPipeline() throws Exception {
        WorkflowJob project = folder1.createProject(WorkflowJob.class, "pipeline");
        project.setDefinition(new CpsFlowDefinition(getPipelineScript(null), true));

        // Enqueue a build of the Pipeline, wait for it to complete, and assert success
        WorkflowRun build = this.jenkins.buildAndAssertSuccess(project);

        // Assert that the console log contains the output we expect
        this.jenkins.assertLogContains("hello", build);
    }


}
