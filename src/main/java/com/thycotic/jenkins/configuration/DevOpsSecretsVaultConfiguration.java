package com.thycotic.jenkins.configuration;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.thycotic.jenkins.credentials.ThycoticVaultCredentials;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.Serializable;
import java.util.List;

public class DevOpsSecretsVaultConfiguration extends AbstractDescribableImpl<DevOpsSecretsVaultConfiguration> implements Serializable {

    private String thycoticCredentialId;


    public DevOpsSecretsVaultConfiguration() {
        // no args constructor
    }

    private DevOpsSecretsVaultConfiguration(DevOpsSecretsVaultConfiguration config) {
        this.thycoticCredentialId = config.getThycoticCredentialId();
    }

    @DataBoundConstructor
    public DevOpsSecretsVaultConfiguration(String thycoticCredentialId) {
        this.thycoticCredentialId = thycoticCredentialId;
    }

    public String getThycoticCredentialId() {
        return thycoticCredentialId;
    }

    @DataBoundSetter
    public void setThycoticCredentialId(String thycoticCredentialId) {
        this.thycoticCredentialId = thycoticCredentialId;
    }

    public DevOpsSecretsVaultConfiguration mergeWithParent(DevOpsSecretsVaultConfiguration parent) {
        if (parent == null) {
            return this;
        }
        DevOpsSecretsVaultConfiguration result = new DevOpsSecretsVaultConfiguration(this);
        if (StringUtils.isBlank(result.getThycoticCredentialId())) {
            result.setThycoticCredentialId(parent.getThycoticCredentialId());
        }
        return result;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<DevOpsSecretsVaultConfiguration> {
        @Override
        public String getDisplayName() {
            return "Thycotic DevOps Secrets Vault Configuration";
        }


        public ListBoxModel doFillThycoticCredentialIdItems(@AncestorInPath Item item, @QueryParameter String uri) {
            // This is needed for folders: credentials bound to a folder are
            // realized through domain requirements
            List<DomainRequirement> domainRequirements = URIRequirementBuilder.fromUri(uri).build();
            return new StandardListBoxModel().includeEmptyValue().includeAs(ACL.SYSTEM, item,
                    ThycoticVaultCredentials.class, domainRequirements);
        }
    }
}