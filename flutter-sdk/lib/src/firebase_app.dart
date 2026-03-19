import 'package:http/http.dart' as http;

/// Core Firebase App configuration and initialization.
class FirebaseApp {
  static FirebaseApp? _instance;

  final String apiKey;
  final String projectId;
  final String baseUrl;
  final http.Client _client;

  FirebaseApp._({
    required this.apiKey,
    required this.projectId,
    required this.baseUrl,
    http.Client? client,
  }) : _client = client ?? http.Client();

  /// The singleton instance after initialization.
  static FirebaseApp get instance {
    if (_instance == null) {
      throw StateError(
        'FirebaseApp not initialized. Call FirebaseApp.initialize() first.',
      );
    }
    return _instance!;
  }

  /// Initialize the Firebase Console SDK.
  ///
  /// [apiKey] - Your project API key from the Firebase Console.
  /// [projectId] - Your project ID.
  /// [baseUrl] - Base URL of your Firebase Console backend (e.g. http://localhost:8080).
  static Future<FirebaseApp> initialize({
    required String apiKey,
    required String projectId,
    required String baseUrl,
    http.Client? client,
  }) async {
    _instance = FirebaseApp._(
      apiKey: apiKey,
      projectId: projectId,
      baseUrl: baseUrl.endsWith('/') ? baseUrl.substring(0, baseUrl.length - 1) : baseUrl,
      client: client,
    );
    return _instance!;
  }

  /// Build base headers for API requests.
  Map<String, String> get headers => {
        'Content-Type': 'application/json',
        'X-API-Key': apiKey,
      };

  /// Build full API URL.
  Uri buildUrl(String path) {
    return Uri.parse('$baseUrl$path');
  }

  http.Client get httpClient => _client;

  /// Fetch project configuration from the server.
  Future<Map<String, dynamic>> getConfig() async {
    final resp = await _client.get(
      buildUrl('/api/v1/project/$projectId/config'),
      headers: headers,
    );
    if (resp.statusCode == 200) {
      return _parseJson(resp.body);
    }
    throw FirebaseException('Failed to fetch config: ${resp.statusCode}');
  }

  static Map<String, dynamic> _parseJson(String body) {
    // Simple JSON parse - in production use dart:convert
    throw UnimplementedError('Use dart:convert in real implementation');
  }
}

/// Base exception for Firebase Console SDK errors.
class FirebaseException implements Exception {
  final String message;
  final String? code;

  const FirebaseException(this.message, {this.code});

  @override
  String toString() => 'FirebaseException[$code]: $message';
}
