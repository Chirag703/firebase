# Firebase Console SDK for Flutter

A Flutter SDK for integrating with the [Firebase Console](../README.md) Spring Boot backend.

## Installation

Add to your `pubspec.yaml`:

```yaml
dependencies:
  firebase_console_sdk:
    path: ./firebase_console_sdk
```

## Quick Start

### Initialize

```dart
import 'package:firebase_console_sdk/firebase_console_sdk.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await FirebaseApp.initialize(
    apiKey: 'YOUR_API_KEY',        // from Firebase Console → Project → Overview
    projectId: 'your-project-id', // from Firebase Console → Project → Settings
    baseUrl: 'http://localhost:8080',
  );

  runApp(MyApp());
}
```

## Authentication

```dart
final auth = FirebaseAuth.instance;

// Sign Up
final user = await auth.signUp(
  email: 'user@example.com',
  password: 'password123',
  name: 'John Doe',
);
print('Created user: ${user.uid}');

// Sign In
final user = await auth.signIn(
  email: 'user@example.com',
  password: 'password123',
);
print('Token: ${user.token}');

// Current user
final current = auth.currentUser;
print('Signed in: ${auth.isSignedIn}');

// Sign Out
await auth.signOut();
```

## Firestore Database

```dart
final db = FirebaseDatabase.instance;

// Read a document
final data = await db.get('users', 'user_123');
if (data != null) {
  print(data['name']);
}

// Write a document
await db.set('users', 'user_123', {
  'name': 'Alice',
  'email': 'alice@example.com',
  'createdAt': DateTime.now().toIso8601String(),
});

// Push (auto-ID)
final id = await db.push('messages', {
  'text': 'Hello World',
  'author': 'Alice',
});
print('New message ID: $id');

// Update specific fields
await db.update('users', 'user_123', {'lastSeen': DateTime.now().toIso8601String()});

// Delete
await db.delete('users', 'user_123');

// List collection
final docs = await db.list('users');
```

## Cloud Storage

```dart
final storage = FirebaseStorage.instance;

// Upload file
final bytes = await File('photo.jpg').readAsBytes();
final result = await storage.uploadData(
  path: 'images/photo_${DateTime.now().millisecondsSinceEpoch}.jpg',
  data: bytes,
  contentType: 'image/jpeg',
);
print('Download URL: ${result.downloadUrl}');

// Get download URL
final url = await storage.getDownloadUrl('images/photo.jpg');

// Delete file
await storage.delete('images/photo.jpg');
```

## Cloud Messaging

```dart
final messaging = FirebaseMessaging.instance;

// Get device token
final token = await messaging.getToken();
print('Device token: $token');

// Subscribe to topic
await messaging.subscribeToTopic('news');
await messaging.subscribeToTopic('promotions');

// Unsubscribe
await messaging.unsubscribeFromTopic('promotions');

// Send notification (admin use)
await messaging.send(NotificationPayload(
  title: 'New Update!',
  body: 'Version 2.0 is now available.',
  topic: 'all',
  data: {'version': '2.0', 'url': 'https://example.com'},
));
```

## Error Handling

All methods throw `FirebaseException` on failure:

```dart
try {
  await auth.signIn(email: 'bad@email.com', password: 'wrong');
} on FirebaseException catch (e) {
  print('Error [${e.code}]: ${e.message}');
}
```

## API Reference

| Class | Description |
|-------|-------------|
| `FirebaseApp` | Core initialization and configuration |
| `FirebaseAuth` | User authentication (sign up, sign in, sign out) |
| `FirebaseDatabase` | Firestore-like database CRUD operations |
| `FirebaseStorage` | File upload, download URL, delete |
| `FirebaseMessaging` | Push notification topics and sending |
| `FirebaseException` | Base exception class |

## License

MIT
