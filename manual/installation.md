# Installation and Setup

DEER is split into two maven submodules: **deer-core** and **deer-cli**.
While deer-core is intended to be used programmatically from other Java applications,
 deer-cli provides a CLI to either run a single configuration or start the DEER server. 

## Generating DEER Server CLI 

To create a runnable jar file, go the the project root and run

```
mvn clean package shade:shade -Dmaven.test.skip=true
```

The runnable jar file will be generated into ` deer-cli/target/deer-cli-${version}.jar`.