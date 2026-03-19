/// Firebase Console SDK for Flutter
///
/// A client SDK that connects to the Firebase Console Spring Boot backend.
///
/// Usage:
/// ```dart
/// import 'package:firebase_console_sdk/firebase_console_sdk.dart';
///
/// await FirebaseApp.initialize(
///   apiKey: 'YOUR_API_KEY',
///   projectId: 'your-project-id',
///   baseUrl: 'http://localhost:8080',
/// );
/// ```
library firebase_console_sdk;

export 'src/firebase_app.dart';
export 'src/auth/firebase_auth.dart';
export 'src/database/firebase_database.dart';
export 'src/storage/firebase_storage.dart';
export 'src/messaging/firebase_messaging.dart';
