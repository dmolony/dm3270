# dm3270-lib

This is a trimmed down version of the [dm3270 emulator](https://github.com/dmolony/dm3270) to be used as TN3270 client library.

In particular it removes all references to JavaFX (which is not required to use code as lib and is not included by default in some [OpenJDK](http://openjdk.java.net/) distributions), and keeps only logic for simple terminal interaction.
Additionally it includes some basic refactor (not too deep refactor to keep some traceability to original code) to simplify code. 

## Usage

To use the library is required [JRE8+](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).

Include the library as dependency:

```xml
<dependency>
    <groupId>us.abstracta</groupId>
    <artifactId>dm3270-lib</artifactId>
    <version>0.1</version>
</dependency>
```

>Check latest version of the library in [releases](https://github.com/abstracta/dm3270/releases) for `-lib` releases.

And then use provided API. An example of such usage can be found in [ManualTestClient](src/test/java/com/bytezone/dm3270/ManualTestClient.java).

## Build

To build the project is required [JDK8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), [maven](https://maven.apache.org/) 3.3+.

Then just run `mvn clean install` and the library will be built and installed in local maven repository.

### Release

To release the plugin just tag (with the version id and using [semantic versioning](https://semver.org/)) the commit to release and push it to GitHub. Travis will take care of the rest and you will be able to find the artifact in [maven central](https://search.maven.org/).