# dm3270-lib

This is a trimmed down version of the [dm3270 emulator](https://github.com/dmolony/dm3270) to be used as TN3270 client library.

In particular it removes all references to JavaFX (which is not required to use code as lib and is not included by default in some [OpenJDK](http://openjdk.java.net/) distributions), and keeps only logic for simple terminal interaction.
Additionally it includes some basic refactor (not too deep refactor to keep some traceability to original code) to simplify code. 

## Usage

To use the library is required [JRE8+](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).

Include the library as maven dependency in `pom.xml`:

```xml
<dependency>
    <groupId>us.abstracta</groupId>
    <artifactId>dm3270-lib</artifactId>
    <version>0.1</version>
</dependency>
```

>Check latest version of the library in [releases](https://github.com/abstracta/dm3270/releases) for `-lib` releases.

And then use provided API. An example of such usage can be found in [TerminalClientTest](src/test/java/com/bytezone/dm3270/TerminalClientTest.java).

## Build

To build the project is required [JDK8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), [maven](https://maven.apache.org/) 3.3+.

Then just run `mvn clean install` and the library will be built and installed in the local maven repository.

## Release

To release the project, define the version to be released by checking included changes since last release and following [semantic versioning](https://semver.org/). 
Then, create a [release](https://github.com/abstracta/dm3270/releases) (including `v` as prefix and `-lib` as suffix of the version, e.g. `v0.1-lib`), this will trigger a Travis build which will publish the jars to maven central repository (and make it general available to be used as maven dependency projects) in around 10 mins and can be found in [maven central search](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22us.abstracta%22%20AND%20a%3A%22dm3270-lib%22) after up to 2 hours.
