import 'dart:convert';
import '../firebase_app.dart';

/// Firebase Firestore Database service.
///
/// Provides read, write, push, and delete operations on collections.
///
/// Usage:
/// ```dart
/// final db = FirebaseDatabase.instance;
///
/// // Read a document
/// final doc = await db.get('users', 'user_123');
///
/// // Write a document
/// await db.set('users', 'user_123', {'name': 'Alice', 'age': 30});
///
/// // Push a new document (auto-generated ID)
/// final id = await db.push('messages', {'text': 'Hello!'});
///
/// // Delete a document
/// await db.delete('users', 'user_123');
///
/// // List a collection
/// final docs = await db.list('users');
/// ```
class FirebaseDatabase {
  static FirebaseDatabase? _instance;

  FirebaseDatabase._();

  static FirebaseDatabase get instance {
    _instance ??= FirebaseDatabase._();
    return _instance!;
  }

  String _path(String collection, [String? docId]) {
    final app = FirebaseApp.instance;
    final base = '/api/v1/project/${app.projectId}/db/$collection';
    return docId != null ? '$base/$docId' : base;
  }

  /// Read a document from a collection.
  ///
  /// Returns null if the document does not exist.
  Future<Map<String, dynamic>?> get(String collection, String docId) async {
    final app = FirebaseApp.instance;
    final resp = await app.httpClient.get(
      app.buildUrl(_path(collection, docId)),
      headers: app.headers,
    );

    if (resp.statusCode == 200) {
      return jsonDecode(resp.body) as Map<String, dynamic>;
    }
    if (resp.statusCode == 404) return null;

    throw FirebaseException(
      'Failed to read document: ${resp.statusCode}',
      code: 'database/read-failed',
    );
  }

  /// Write (create or overwrite) a document in a collection.
  Future<void> set(
    String collection,
    String docId,
    Map<String, dynamic> data,
  ) async {
    final app = FirebaseApp.instance;
    final resp = await app.httpClient.put(
      app.buildUrl(_path(collection, docId)),
      headers: app.headers,
      body: jsonEncode(data),
    );

    if (resp.statusCode != 200 && resp.statusCode != 201) {
      throw FirebaseException(
        'Failed to write document: ${resp.statusCode}',
        code: 'database/write-failed',
      );
    }
  }

  /// Push a new document with an auto-generated ID. Returns the new document ID.
  Future<String> push(String collection, Map<String, dynamic> data) async {
    final app = FirebaseApp.instance;
    final resp = await app.httpClient.post(
      app.buildUrl(_path(collection)),
      headers: app.headers,
      body: jsonEncode(data),
    );

    if (resp.statusCode == 200 || resp.statusCode == 201) {
      final body = jsonDecode(resp.body) as Map<String, dynamic>;
      return body['id']?.toString() ?? '';
    }

    throw FirebaseException(
      'Failed to push document: ${resp.statusCode}',
      code: 'database/push-failed',
    );
  }

  /// Update specific fields of an existing document.
  Future<void> update(
    String collection,
    String docId,
    Map<String, dynamic> data,
  ) async {
    final app = FirebaseApp.instance;
    final resp = await app.httpClient.patch(
      app.buildUrl(_path(collection, docId)),
      headers: app.headers,
      body: jsonEncode(data),
    );

    if (resp.statusCode != 200) {
      throw FirebaseException(
        'Failed to update document: ${resp.statusCode}',
        code: 'database/update-failed',
      );
    }
  }

  /// Delete a document from a collection.
  Future<void> delete(String collection, String docId) async {
    final app = FirebaseApp.instance;
    final resp = await app.httpClient.delete(
      app.buildUrl(_path(collection, docId)),
      headers: app.headers,
    );

    if (resp.statusCode != 200 && resp.statusCode != 204) {
      throw FirebaseException(
        'Failed to delete document: ${resp.statusCode}',
        code: 'database/delete-failed',
      );
    }
  }

  /// List all documents in a collection.
  Future<List<Map<String, dynamic>>> list(String collection) async {
    final app = FirebaseApp.instance;
    final resp = await app.httpClient.get(
      app.buildUrl(_path(collection)),
      headers: app.headers,
    );

    if (resp.statusCode == 200) {
      final body = jsonDecode(resp.body);
      if (body is List) {
        return body.cast<Map<String, dynamic>>();
      }
      return [];
    }

    throw FirebaseException(
      'Failed to list collection: ${resp.statusCode}',
      code: 'database/list-failed',
    );
  }
}
