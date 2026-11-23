pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    // ffmpeg-kit artifacts are no longer served by Maven Central directly;
    // this mirror still hosts the published 6.0 LTS AARs.
    maven {
      url = uri("https://maven.aliyun.com/repository/central")
      content { includeGroup("com.arthenica") }
    }
    mavenCentral()
  }
}

rootProject.name = "clips"

include(":app")
