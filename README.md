# DevOps Secrets Vault Jenkins Plugin
This plugin allows access to the Thycotic Vault API to access secrets used in the build process.
Use of the is plugin must be associated with a licensed version of the Thycotic Vault.

## Limitations
Currently this plugin only supports `json` formatted secrets within the vault

## Building and Running

This plugin requires the use of Java 8 along with Maven 3.5+

### Helpful Commands
- Create plugin - `mvn clean install`
- Run tests - `mvn clean test`
- Run Plugin Locally - `mvn hpi:run`


### Example Usage in Jenkins

```
node {
    // define the thycoticVaultSecrets and the env variables
    def secretValues = [
        [$class: 'ThycoticSecretValue', key: 'key', envVar: 'secret']
    ]
    def secrets = [
        [$class: 'ThycoticSecret', path: 'SECRET', secretValues: secretValues]
    ]

    def configuration = [$class: 'DevOpsSecretsVaultConfiguration',
                       thycoticCredentialId: '"credentials"']

    // inside this block your credentials will be available as env variables
    wrap([$class: 'ThycoticVaultBuildWrapper', configuration: configuration, thycoticVaultSecrets: secrets]) {
        bat "echo $secret"
    }
}
```