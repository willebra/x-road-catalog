# Introduction to X-Road Catalog Lister

The purpose of this module is to provide a web service which lists all the X-Road members and the services they provide 
together with services descriptions.

A class diagram illustrating X-Road Catalog Lister implementation with the `default` and `FI` profiles:

![Catalog Service class diagram](img/class_diagram.png)

See also the [Installation Guide](../doc/xroad_catalog_installation_guide.md) and
[User Guide](../doc/xroad_catalog_user_guide.md).

## Profiles

There are four Spring Boot profiles:

* `default` - a profile used for default operation of X-Road Catalog, without any country-specific features.
* `FI` - an extra profile used in addition to the default profile, which has country-specific (Finland) features, e.g.,
  fetching additional data from a national business registry. Other country-specific profiles can be added if needed.
* `production` - a profile used in the production deployment.
* `sshtest` - a profile used to test SSH tunneling with X-Road Catalog.

## Build

X-Road Catalog Lister can be built by running:

```bash
../gradlew clean build
```

## Build RPM Packages on Non-RedHat Platform

If the `default` profile is used, the `CATALOG_PROFILE` argument can be omitted.

```bash
../gradlew clean build
docker build -t lister-rpm packages/xroad-catalog-lister/docker --build-arg CATALOG_PROFILE=<PROFILE>
docker run -v $PWD/..:/workspace lister-rpm
```

## Run

X-Road Catalog Lister can be run using Gradle:

```bash
../gradlew bootRun
```

or running it from a JAR file:

```bash
java -jar build/libs/xroad-catalog-lister.jar --spring.config.name=lister,catalogdb
```
