# Cassandra Diagnostics Releasing

This document will explain what to do when new version is ready for release.

## Versioning

We are versioning Cassandra Diagnostics with major.minor.patch versions. If changes included in new release are minor fixes and improvements we increase patch version. If subject of new release is new feature we increase minor version. If we changed extensively (adding new module, change in project structure) we increase major version and do release.

## Releasing

1. Increase new release version

First step is to set new version in parent pom, and in all pom files in each submodule. This guide will follow release from 1.1.1. to 1.1.2. but it can be applyed to any new release we make.

```
mvn versions:set -DnewVersion=1.1.1
mvn versions:commit  
```

After this make new release PR with updated POM.

2. Merge develop into master

We are releasing from master branch. After PR is merged we can safely merge develop in master so we can release to Bintray. Merge dev into master and create release PR on master branch.

3. Release to bintray

At this point master is ready for new release. Pull master, and deploy to Bintray.

```
mvn clean deploy -P extras
```

It is good practice to run unit tests, integration tests and functional tests before releasing. More details on how to run different kind of tests can be seen in README file under Running Integration and Functional Tests section.

4. Sync repo to maven Central

When you go to www.bintray.com on Cassandra Diagnostics project there is maven central tab, and there you can sync Bintray repo with maven central repo. We stage our jars on Bintray but we use Bintray maven central integration to push it to maven central as well.

5. Update release notes on github

Github project has releases section. Here we tag all new releases and write what is included in each of them. Do a short summary what is included in new release.

6. Prepare new snapshot version for development

We need new snapshot version on dev branch so we can continue work. At this point we can create PR with new version:

```
mvn versions:set -DnewVersion=1.1.2-SNAPSHOT
mvn versions:commit  
```
