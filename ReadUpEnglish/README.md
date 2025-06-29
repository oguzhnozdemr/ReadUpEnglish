# 📚 ReadUp English

A modern Android application designed to enhance English learning through book reading. Built with Jetpack Compose and following MVVM architecture patterns.

## ✨ Features

### 📖 Core Reading Features
- **Book Browsing**: Discover and explore a vast collection of English books
- **Smart Reading**: Read books with optimized text formatting and user-friendly interface
- **Reading Progress**: Track your reading progress with percentage indicators
- **Bookmarks**: Save your reading position and continue where you left off
- **Favorite Books**: Mark and manage your favorite books for quick access

### 🚀 Performance & Caching
- **Two-Level Caching System**: Intelligent memory and database caching for instant book loading
- **Offline Reading**: Read downloaded books without internet connection
- **Smart Loading**: Books load instantly after first access with cache-first approach
- **Bandwidth Optimization**: Significantly reduced network usage through efficient caching

### 📚 Advanced Features
- **History Books**: Browse books by historical topics and themes
- **Cross-Navigation Persistence**: Content remains available when navigating between screens
- **Debug Tools**: Built-in cache management and performance monitoring

## 🛠️ Tech Stack

### Core Technologies
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern Android UI toolkit
- **MVVM Architecture** - Clean architecture pattern

### Libraries & Frameworks
- **Hilt** - Dependency injection
- **Retrofit** - REST API communication
- **Room Database** - Local data persistence
- **Navigation Compose** - App navigation
- **Coil** - Image loading and caching
- **RxJava** - Reactive programming
- **Coroutines** - Asynchronous programming

### API & Backend
- **RESTful API** integration for book content
- **Gson** - JSON serialization/deserialization
- **OkHttp** - HTTP client with logging interceptor

## 🏗️ Architecture

```
BookDetailScreen -> BookViewModel -> BookRepository -> BookContentCache
                                                    -> Memory Cache (HashMap)
                                                    -> Database Cache (Room)
```

### Key Components
- **Repository Pattern**: Clean separation of data sources
- **Singleton Cache Manager**: Efficient content caching across app lifecycle
- **Database Layer**: Persistent storage with Room database
- **Network Layer**: Robust API communication with retry mechanisms

## 💾 Caching System

The app implements a sophisticated two-level caching system:

1. **Memory Cache**: Fast in-memory storage for instant access
2. **Database Cache**: Persistent SQLite storage that survives app restarts
3. **Cache-First Approach**: Always checks cache before network requests
4. **Smart Fallback**: Loads from network when cache is unavailable

### Benefits
- ⚡ **Instant Loading**: Previously accessed books load immediately
- 💾 **Offline Access**: Read books without internet connection  
- 📱 **Battery Efficient**: Reduced network requests save battery life
- 🔄 **Cross-Session Persistence**: Content available after app restart

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- Android SDK API 26 (Android 8.0) or higher
- Kotlin 1.9.22 or later

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/ReadUpEnglish.git
   cd ReadUpEnglish
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Build and Run**
   - Wait for Gradle sync to complete
   - Connect an Android device or start an emulator
   - Click "Run" or press `Ctrl+R`

### Configuration
- No additional configuration required
- The app will automatically set up the database on first run
- Cache system initializes automatically

## 📱 Screenshots

[Add screenshots of your app here]

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Oğuzhan Özdemir**
- GitHub: [@yourusername](https://github.com/yourusername)

## 🙏 Acknowledgments

- Thanks to all contributors who helped make this project possible
- Special thanks to the open-source community for the amazing libraries used in this project

---

⭐ Star this repository if you find it helpful! 