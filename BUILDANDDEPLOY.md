## Building

Cassandra Diagnostics is a maven project and the project could be built using a single maven command like the following:

```
$ mvn clean package
```

As the result of this, all project's sub-modules would be compiled, unit test run and output artifacts (JARs) generated. In every sub-module directory, there is a `target` directory that contains generated JAR files. For example, in `cassandra-diagnostics-core` there is a `target` directory that contains `cassandra-diagnostics-core-<VERSION>.jar`.

## Running Integration and Functional Tests

In Cassandra Diagnostics projects, integration and functional tests kept separately. They are not activated for the default maven profile (`dev`) and, therefore, not executed in the maven test phase by default.

The `integration-test` profile has to be activated in order to execute integration tests (quite unexpected, right?). This profile is executed by `maven-failsafe-plugin` in maven `verify` phase. The maven command to execute integration tests:

```
$ mvn clean verify -P integration-test
```

The `functional-test` profile has to be activated in order to execute functional tests in the `verify` maven phase. Along with `functional-test` profile, name of the particular functional test should also be provided as a profile. Available functional test are: 

- `basic-ft`
- `tracing-ft`
- `influx`
- `riemann`
- `telegraf`

The maven command for running functional tests is `mvn clean verify -P functional-test,[NAME_OF_THE_FUNCTIONAL_TEST]`. For example to run tests from `basic-ft` profile following command should be run:

```
$ mvn clean verify -P functional-test,basic-ft
```

## Releasing

This document will explain what to do when new version is ready for release.

### Versioning

We are versioning Cassandra Diagnostics with major.minor.patch versions. If changes included in new release are minor fixes and improvements we increase patch version. If subject of new release is new feature or new module we increase minor version. If we changed extensively (change in project structure, breakdown in backward compatibility) we increase major version and do release.

### Releasing

We follow Git branching model explained on this [link](http://nvie.com/posts/a-successful-git-branching-model/). Basically we keep `develop` branch as integration branch for features which is on SNAPSHOT version. After we are satisfied, we make `release-[next-version]` branch out of `develop` branch. We update version to version of next release and we merge that branch to both `develop` and `master`. After we merge we just push both branches omittiong PR flow from GitHub since this is straightforward and we do not need review. Further explanation of build steps is below. 

1. Create release branch

When you are ready to release create `release-[next-version]` branch which will locally serve as release branch. On it do version increase.

2. Increase new release version

First step is to set new version in parent pom, and in all pom files in each submodule. This guide will follow release from 1.1.1. to 1.1.2. but it can be applyed to any new release we make.

```
mvn versions:set -DnewVersion=1.1.2
mvn versions:commit  
```

After this make new release PR with updated POM.

3. Merge release branch into master and develop

We are releasing from master branch. When all work is done on release branch merge it to both develop and master and push changed branches to GitHub. We are ommiting PR for this since this is straightforward and we do not need review PR is providing.

4. Release to bintray

At this point master is ready for new release. Pull master, and deploy to Bintray.

```
mvn clean deploy -P extras
```

It is good practice to run unit tests, integration tests and functional tests before releasing. More details on how to run different kind of tests can be seen in README file under Running Integration and Functional Tests section.

5. Sync repo to maven Central

When you go to www.bintray.com on Cassandra Diagnostics project there is maven central tab, and there you can sync Bintray repo with maven central repo. We stage our jars on Bintray but we use Bintray maven central integration to push it to maven central as well.

6. Update release notes on github

Github project has releases section. Here we tag all new releases and write what is included in each of them. Do a short summary what is included in new release.

7. Prepare new snapshot version for development

We need new snapshot version on dev branch so we can continue work. At this point we can create PR with new version:

```
mvn versions:set -DnewVersion=1.1.3-SNAPSHOT
mvn versions:commit  
```
