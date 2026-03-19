import 'dart:convert';
import 'package:http/http.dart' as http;
import '../firebase_app.dart';

/// Represents an authenticated user.
class FirebaseUser {
  final String uid;
  final String email;
  final String name;
  final String token;

  const FirebaseUser({
    required this.uid,
    required this.email,
    required this.name,
    required this.token,
  });

  factory FirebaseUser.fromJson(Map<String, dynamic> json) {
    return FirebaseUser(
      uid: json['uid']?.toString() ?? '',
      email: json['email']?.toString() ?? '',
      name: json['name']?.toString() ?? '',
      token: json['token']?.toString() ?? '',
    );
  }

  Map<String, dynamic> toJson() => {
        'uid': uid,
        'email': email,
        'name': name,
        'token': token,
      };

  @override
  String toString() => 'FirebaseUser(uid: $uid, email: $email)';
}

/// Firebase Authentication service.
///
/// Provides sign-up, sign-in, and sign-out functionality.
///
/// Usage:
/// ```dart
/// final auth = FirebaseAuth.instance;
///
/// // Sign up
/// final user = await auth.signUp(
///   email: 'user@example.com',
///   password: 'password123',
///   name: 'John Doe',
/// );
///
/// // Sign in
/// final user = await auth.signIn(
///   email: 'user@example.com',
///   password: 'password123',
/// );
///
/// // Sign out
/// await auth.signOut();
///
/// // Get current user
/// final current = auth.currentUser;
/// ```
class FirebaseAuth {
  static FirebaseAuth? _instance;
  FirebaseUser? _currentUser;

  FirebaseAuth._();

  static FirebaseAuth get instance {
    _instance ??= FirebaseAuth._();
    return _instance!;
  }

  /// The currently signed-in user, or null if not signed in.
  FirebaseUser? get currentUser => _currentUser;

  /// Returns true if a user is currently signed in.
  bool get isSignedIn => _currentUser != null;

  /// Sign up a new user with email and password.
  ///
  /// Throws [FirebaseException] if the email is already taken or if validation fails.
  Future<FirebaseUser> signUp({
    required String email,
    required String password,
    String? name,
  }) async {
    final app = FirebaseApp.instance;
    final resp = await app.httpClient.post(
      app.buildUrl('/api/v1/project/${app.projectId}/auth/register'),
      headers: app.headers,
      body: jsonEncode({
        'email': email,
        'password': password,
        'name': name ?? email,
      }),
    );

    final data = jsonDecode(resp.body) as Map<String, dynamic>;

    if (resp.statusCode == 200) {
      _currentUser = FirebaseUser.fromJson(data);
      return _currentUser!;
    }

    throw FirebaseException(
      data['error']?.toString() ?? 'Sign up failed',
      code: 'auth/sign-up-failed',
    );
  }

  /// Sign in with email and password.
  ///
  /// Throws [FirebaseException] if credentials are invalid.
  Future<FirebaseUser> signIn({
    required String email,
    required String password,
  }) async {
    final app = FirebaseApp.instance;
    final resp = await app.httpClient.post(
      app.buildUrl('/api/v1/project/${app.projectId}/auth/login'),
      headers: app.headers,
      body: jsonEncode({
        'email': email,
        'password': password,
      }),
    );

    final data = jsonDecode(resp.body) as Map<String, dynamic>;

    if (resp.statusCode == 200) {
      _currentUser = FirebaseUser.fromJson(data);
      return _currentUser!;
    }

    throw FirebaseException(
      data['error']?.toString() ?? 'Sign in failed',
      code: 'auth/invalid-credentials',
    );
  }

  /// Sign out the currently signed-in user.
  Future<void> signOut() async {
    _currentUser = null;
  }
}
