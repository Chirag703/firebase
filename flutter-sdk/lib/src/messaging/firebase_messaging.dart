import 'dart:convert';
import '../firebase_app.dart';

/// A push notification payload.
class NotificationPayload {
  final String title;
  final String body;
  final String? topic;
  final String? deviceToken;
  final Map<String, String>? data;

  const NotificationPayload({
    required this.title,
    required this.body,
    this.topic,
    this.deviceToken,
    this.data,
  });

  Map<String, dynamic> toJson() => {
        'title': title,
        'body': body,
        if (topic != null) 'topic': topic,
        if (deviceToken != null) 'deviceToken': deviceToken,
        if (data != null) 'data': data,
      };
}

/// Firebase Cloud Messaging service.
///
/// Provides topic subscription and notification sending.
///
/// Usage:
/// ```dart
/// final messaging = FirebaseMessaging.instance;
///
/// // Subscribe to a topic
/// await messaging.subscribeToTopic('news');
///
/// // Send a notification (requires server-side key)
/// await messaging.send(NotificationPayload(
///   title: 'Hello!',
///   body: 'New update available',
///   topic: 'all',
/// ));
/// ```
class FirebaseMessaging {
  static FirebaseMessaging? _instance;
  String? _deviceToken;
  final Set<String> _subscribedTopics = {};

  FirebaseMessaging._();

  static FirebaseMessaging get instance {
    _instance ??= FirebaseMessaging._();
    return _instance!;
  }

  /// The current device registration token.
  String? get token => _deviceToken;

  /// Topics the device is subscribed to.
  Set<String> get subscribedTopics => Set.unmodifiable(_subscribedTopics);

  /// Get the FCM registration token for this device.
  ///
  /// Returns a simulated token in this SDK implementation.
  Future<String?> getToken() async {
    _deviceToken ??= 'flutter_token_${DateTime.now().millisecondsSinceEpoch}';
    return _deviceToken;
  }

  /// Subscribe to a messaging topic.
  ///
  /// [topic] - Topic name, e.g. 'news', 'promotions'
  Future<void> subscribeToTopic(String topic) async {
    final app = FirebaseApp.instance;
    final resp = await app.httpClient.post(
      app.buildUrl('/api/v1/project/${app.projectId}/messaging/subscribe'),
      headers: app.headers,
      body: jsonEncode({
        'topic': topic,
        'token': await getToken(),
      }),
    );

    if (resp.statusCode == 200 || resp.statusCode == 201) {
      _subscribedTopics.add(topic);
      return;
    }

    // Gracefully handle if endpoint not yet implemented
    if (resp.statusCode == 404 || resp.statusCode == 405) {
      _subscribedTopics.add(topic);
      return;
    }

    throw FirebaseException(
      'Failed to subscribe to topic: ${resp.statusCode}',
      code: 'messaging/subscribe-failed',
    );
  }

  /// Unsubscribe from a messaging topic.
  Future<void> unsubscribeFromTopic(String topic) async {
    final app = FirebaseApp.instance;
    final resp = await app.httpClient.post(
      app.buildUrl('/api/v1/project/${app.projectId}/messaging/unsubscribe'),
      headers: app.headers,
      body: jsonEncode({
        'topic': topic,
        'token': await getToken(),
      }),
    );

    if (resp.statusCode == 200 || resp.statusCode == 404 || resp.statusCode == 405) {
      _subscribedTopics.remove(topic);
      return;
    }

    throw FirebaseException(
      'Failed to unsubscribe from topic: ${resp.statusCode}',
      code: 'messaging/unsubscribe-failed',
    );
  }

  /// Send a notification (requires appropriate API key permissions).
  Future<void> send(NotificationPayload payload) async {
    final app = FirebaseApp.instance;
    final resp = await app.httpClient.post(
      app.buildUrl('/api/v1/project/${app.projectId}/messaging/send'),
      headers: app.headers,
      body: jsonEncode(payload.toJson()),
    );

    if (resp.statusCode != 200 && resp.statusCode != 201) {
      final body = jsonDecode(resp.body) as Map<String, dynamic>? ?? {};
      throw FirebaseException(
        body['error']?.toString() ?? 'Failed to send notification',
        code: 'messaging/send-failed',
      );
    }
  }
}
