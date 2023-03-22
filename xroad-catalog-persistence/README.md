# Introduction to X-Road Catalog Persistence

The purpose of this module is to persist and read persisted data. The module is used by the Collector and Lister modules.

1. A class diagram illustrating X-Road Catalog Persistence with the `default` and `FI` profiles:

![Class diagram](img/class_diagram.png)

See also the [User Guide](../xroad_catalog_user_guide.md)

## Profiles

There are four Spring Boot profiles:

* `default` - a profile used for default operation of X-Road Catalog, without any country-specific features.
* `FI` - an extra profile used in addition to the default profile, which has country-specific (Finland) features, e.g.,
  fetching additional data from a national business registry. Other country-specific profiles can be added if needed.
* `production` - a profile used in the production deployment.
* `sshtest` - a profile used to test SSH tunneling with X-Road Catalog.

## Create database

The database required for X-Road Catalog can be created with the following:

```bash
sudo -u postgres psql --file=src/main/sql/init_database.sql
```

The command for creating the database tables depends on the profile (`default` or `FI`) that is used:

- When the `default` profile is used:
  ```bash
  sudo -u postgres psql --file=src/main/sql/create_tables.sql
  ```
- When the `FI` profile is used:
  ```bash
  sudo -u postgres psql --file=src/main/sql/create_tables_fi.sql
  ```

Also note that these scripts will be run automatically ([X-Road Catalog Collector spec](../xroad-catalog-collector/packages/xroad-catalog-collector/redhat/SPECS/xroad-catalog-collector.spec)) 
when the `xroad-catalog-collector` service ([X-Road Catalog Collector service](../xroad-catalog-collector/packages/xroad-catalog-collector/redhat/SOURCES/xroad-catalog-collector.service)) 
is installed to the target server.

## Build

X-Road persistence can be built with:

```bash
../gradlew clean build
```

## Run

X-Road persistence can be run with Gradle:

```bash
../gradlew bootRun
```

or run from a JAR file:

```bash
../gradlew bootRun -Dspring.profiles.active=default
```
