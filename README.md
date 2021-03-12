# EB4j

EPWING/Ebook access library.

You can obtain EB4J library from either Github Packages or Gitlab packages.

## Use eb4j library for your project with github


### Gradle

```
repositories {
    maven {
        url "https://maven.pkg.github.com/eb4j/eb4j"
        credentials {
            username = 'github username'
            password = 'personal access token'
        }
    }
}
dependencies {
    implementation 'io.github.eb4j:eb4j:2.1.5'
}
```

### Gradle(kts)

```
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/eb4j/eb4j")
        credentials {
            username = 'github username'
            password = 'personal access token'
        }
    }
}
dependencies {
    implementation('io.github.eb4j:eb4j:2.1.5')
}
```

### Maven

```
A Maven `settings.xml` file:
<servers>
  <server>
    <id>github</id>
    <username>github user</username>
    <password>TOKEN</password>
  </server>
</servers>

<repositories>
    <repository>
      <id>github</id>
      <url>https://maven.pkg.github.com/eb4j/eb4j</url>
    </repository>
</repositories>
 <dependencies>
    <dependency>
      <groupId>io.github.eb4j</groupId>
      <artifactId>eb4j</artifactId>
      <version>2.1.5</version>
      <type>jar</type>
    </dependency>
    ...
  </dependencies>
```

## Use eb4j library for your project with gitlab

You can access gitlab packages with your private access token.

### Gradle

Create `~/.gradle/gradle.properties` file and put authentication credential
```
gitLabPrivateToken=REPLACE_WITH_YOUR_PERSONAL_ACCESS_TOKEN
```

and then

```
repositories {
    maven {
        url "https://gitlab.com/api/v4/groups/dictzip/-/packages/maven"
        credentials(HttpHeaderCredentials) {
            name = 'Private-Token'
            value = gitLabPrivateToken
        }
        authentication {
            header(HttpHeaderAuthentication)
        }
    }
}
dependencies {
    implementation 'io.github.eb4j:eb4j:2.1.5'
}
```

### Gradle(kts)

```
repositories {
    maven {
        url = uri("https://gitlab.com/api/v4/groups/dictzip/-/packages/maven")
        credentials(HttpHeaderCredentials) {
            name = 'Private-Token'
            value = gitLabPrivateToken
        }
        authentication {
            header(HttpHeaderAuthentication)
        }
    }
}
dependencies {
    implementation('io.github.eb4j:eb4j:2.1.5')
}
```


## Build

EB4j uses Gradle for build system. You can build library and utilities
by typing command (in Mac/Linux/Unix):

```
$ ./gradlew build
```

or (in Windows):

```
C:> gradlew.bat build
```

You will find generated archive files at

```
eb4j-core/build/libs/eb4j-core-<version>.jar
eb4j-tools/build/distributions/eb4j-tools-<version>.tgz
eb4j-tools/build/distributions/eb4j-tools-<version>.zip
```

## Contribution

As usual of other projects hosted on GitHub, we are welcome
forking source and send modification as a Pull Request.
It is recommended to post an issue before sending a patch,
and share your opinions and/or problems.

## Copyrights and License

EB4j, an access library for EPWING/Ebook.

Copyright(C) 2003-2010 Hisaya FUKUMOTO

Copyright(C) 2016 Hiroshi Miura, Aaron Madlon-Kay

Copyright(C) 2020-2021 Hiroshi Miura

This library is free software; you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the Free
Software Foundation; either version 2.1 of the License, or (at your option) any
later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along
with this library; if not, write to the Free Software Foundation, Inc.,
59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

## Why forked and welcome contribution

Here is a fork project from http://eb4j.osdn.jp which has discontinued
its development in 2010.

Here is a new place for eb4j project in order to maintain it by community
basis, to accept patches and comments, and to improve library.

You are welcome to contribute here by pushing PRs, leaving comments or
join as development member.
