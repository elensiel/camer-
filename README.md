# camer-

camer- is an Android camera app built with Kotlin and Jetpack Compose.

## Features

- Capture and review photos
- Switch between front and back cameras
- Adjust zoom and flash
- Choose aspect ratios
- Support multiple image formats
- Customize camera settings

## Download

- [Download Latest APK](https://github.com/elensiel/camer-/releases/latest)
- [View All Versions](https://github.com/elensiel/camer-/releases)

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- CameraX
- Kotlin Coroutines and Flow

## Architecture

The project uses **MVVM (Model-View-ViewModel)** with a layered architecture.

[![Architecture diagram of camer-](https://gitdiagram.com/elensiel/camer-/diagram.png)](https://gitdiagram.com/elensiel/camer-?utm_source=readme&utm_medium=picture)

- **Presentation:** Compose UI and ViewModels
- **Domain:** Business logic, models, and use cases
- **Data:** Repository implementations and data handling

This structure separates responsibilities and helps keep the code maintainable and extensible.

## Engineering Highlights

- **Separation of concerns:** UI, business logic, and data handling are organized into distinct layers.
- **Camera integration:** Uses CameraX for camera controls and photo capture.
- **Image processing:** Supports multiple output formats, including image transcoding.
- **Extensible design:** Uses repository interfaces and use cases to organize application logic.

## Requirements

- Android 8.0 (API 26) or later

## License

This project is licensed under the MIT License. See [LICENSE.txt](LICENSE.txt) for details.
