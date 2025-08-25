# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TedImagePicker is an image/video picker library for Android. It supports both single and multi-selection with various customization options.

## Build Commands

### Build Project
```bash
./gradlew build
```

### Debug Build & Install
```bash
./gradlew assembleDebug
./gradlew installDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Testing
```bash
# Note: Currently no test framework is configured
# To add tests, configure testing framework in build.gradle files
```

### Publishing
```bash
# Local Maven repository
./gradlew publishToMavenLocal

# Maven Central (requires proper credentials)
./gradlew publishToMavenCentral

# Sign all publications (automated via vanniktech.maven.publish plugin)
```

### Clean Build
```bash
./gradlew clean
```

## Architecture

### Module Structure
- `app/`: Sample app module (library usage example)
- `tedimagepicker/`: Main library module

### Key Components

#### Builder Pattern
- `TedImagePicker`: Listener-style builder
- `TedRxImagePicker`: RxJava-style builder  
- `TedImagePickerBaseBuilder`: Base builder for common configurations

#### Core Activities
- `TedImagePickerActivity`: Main image picker activity
- `TedImageZoomActivity`: Image zoom view activity

#### Adapters
- `MediaAdapter`: Media grid adapter
- `AlbumAdapter`: Album list adapter  
- `SelectedMediaAdapter`: Selected media adapter

#### Model Classes
- `Media`: Media file information
- `Album`: Album information

### Key Features
- DataBinding usage (required in all modules)
- RxJava2 support
- Image loading with Glide
- Permission management via TedPermission
- Material Design components

## Required Setup

### DataBinding Setup
DataBinding must be enabled in all consuming projects:

```gradle
dataBinding {
    enabled = true
}
```

### Target SDK
- minSdkVersion: 17
- compileSdkVersion: 34
- targetSdkVersion: 34 (Android 14 support)
- Java 17 compatibility
- Gradle: 8.11.1
- Kotlin: 2.0.21

## Development Workflow

### Prerequisites
All consuming projects **MUST** enable DataBinding in their `build.gradle`:
```gradle
dataBinding {
    enabled = true
}
```

### Code Architecture Patterns
- **Builder Pattern**: Three builders for different use cases
  - `TedImagePicker`: Callback-based API
  - `TedRxImagePicker`: RxJava2 reactive API
  - `TedImagePickerBaseBuilder`: Shared configuration base
  
- **Adapter Pattern**: Header-enabled adapters with selection tracking
  - All adapters extend `BaseSimpleHeaderAdapter` for camera tile support
  - Uses RecyclerView Selection API for multi-selection

### Dependency Management
Key dependencies managed in `dependencies.gradle`:
- RxJava2: 2.2.8 + RxAndroid: 2.1.1
- Glide: 4.12.0 (with KAPT annotation processing)
- TedPermission: 3.3.0 (RxJava2 variant)
- Material Components: 1.1.0-alpha07

## Development Notes

### Kotlin Usage
- Kotlin 2.0.21
- Parcelize plugin utilization
- KAPT for annotation processing (DataBinding, Glide)

### Permission Handling
- READ_MEDIA_IMAGES, READ_MEDIA_VIDEO (API 33+)
- READ_EXTERNAL_STORAGE (lower versions)
- CAMERA (when camera features are used)
- Handled via TedPermission library integration

### Performance Considerations
- Glide memory optimization
- RecyclerView ViewHolder pattern with selection tracking
- Proper image sizing for large image handling
- FastScroller implementation for large galleries

### Publishing Configuration
- Uses `com.vanniktech.maven.publish` plugin v0.27.0
- Publishes to Maven Central (Sonatype S01)
- Version: 1.6.1
- Automatic signing enabled
- Consumer ProGuard rules in `consumer-rules.pro`

## Important Implementation Notes

### Multi-Selection Behavior
- Currently implementing drag-to-multi-select feature (feature/drag-multi-select branch)
- Selection state managed via `SelectionTracker` from RecyclerView Selection library
- Selected items tracked in `MediaAdapter.selectedUriList`

### Key Files for Major Features
- `MediaAdapter.kt`: Core grid adapter with selection logic and camera tile
- `TedImagePickerActivity.kt`: Main picker UI and lifecycle management
- `TedImagePickerBaseBuilder.kt`: Configuration builder with extensive customization options
- `PartialAccessManageBottomSheet.kt`: Android 14 partial access permission handling

### Critical Development Patterns
- All UI uses DataBinding - layouts in `layout/` directory use `<layout>` root tags
- Image loading exclusively through Glide with proper memory management
- Permission requests handled via TedPermission library (no manual permission code)
- RxJava2 patterns available via `TedRxImagePicker` for reactive programming