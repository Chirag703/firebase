import 'dart:convert';
import 'dart:typed_data';
import '../firebase_app.dart';

/// Result of a storage upload operation.
class StorageUploadResult {
  final String path;
  final String downloadUrl;
  final int size;

  const StorageUploadResult({
    required this.path,
    required this.downloadUrl,
    required this.size,
  });

  factory StorageUploadResult.fromJson(Map<String, dynamic> json) {
    return StorageUploadResult(
      path: json['path']?.toString() ?? '',
      downloadUrl: json['downloadUrl']?.toString() ?? '',
      size: (json['size'] as num?)?.toInt() ?? 0,
    );
  }

  @override
  String toString() => 'StorageUploadResult(path: $path, size: $size)';
}

/// Firebase Cloud Storage service.
///
/// Provides file upload, download URL retrieval, and deletion.
///
/// Usage:
/// ```dart
/// final storage = FirebaseStorage.instance;
///
/// // Upload bytes
/// final result = await storage.uploadData(
///   path: 'images/profile_abc.jpg',
///   data: imageBytes,
///   contentType: 'image/jpeg',
/// );
/// print(result.downloadUrl);
///
/// // Get download URL
/// final url = await storage.getDownloadUrl('images/profile_abc.jpg');
///
/// // Delete a file
/// await storage.delete('images/profile_abc.jpg');
/// ```
class FirebaseStorage {
  static FirebaseStorage? _instance;

  FirebaseStorage._();

  static FirebaseStorage get instance {
    _instance ??= FirebaseStorage._();
    return _instance!;
  }

  String _storagePath(String path) {
    final app = FirebaseApp.instance;
    final encoded = Uri.encodeComponent(path);
    return '/api/v1/project/${app.projectId}/storage/$encoded';
  }

  /// Upload raw bytes to the given storage path.
  ///
  /// [path] - The storage path, e.g. 'images/avatar.png'
  /// [data] - The file bytes to upload
  /// [contentType] - MIME type, e.g. 'image/jpeg'
  Future<StorageUploadResult> uploadData({
    required String path,
    required Uint8List data,
    String contentType = 'application/octet-stream',
  }) async {
    final app = FirebaseApp.instance;
    final headers = {
      ...app.headers,
      'Content-Type': contentType,
    };
    headers.remove('Content-Type'); // let multipart set it

    final request = await app.httpClient.post(
      app.buildUrl(_storagePath(path)),
      headers: {
        ...app.headers,
        'X-Content-Type': contentType,
      },
      body: data,
    );

    if (request.statusCode == 200 || request.statusCode == 201) {
      final body = jsonDecode(request.body) as Map<String, dynamic>;
      return StorageUploadResult.fromJson(body);
    }

    throw FirebaseException(
      'Upload failed: ${request.statusCode}',
      code: 'storage/upload-failed',
    );
  }

  /// Get a download URL for a stored file.
  Future<String> getDownloadUrl(String path) async {
    final app = FirebaseApp.instance;
    final resp = await app.httpClient.get(
      app.buildUrl('${_storagePath(path)}?action=getUrl'),
      headers: app.headers,
    );

    if (resp.statusCode == 200) {
      final body = jsonDecode(resp.body) as Map<String, dynamic>;
      return body['url']?.toString() ?? '';
    }

    throw FirebaseException(
      'Failed to get download URL: ${resp.statusCode}',
      code: 'storage/url-failed',
    );
  }

  /// Delete a file at the given path.
  Future<void> delete(String path) async {
    final app = FirebaseApp.instance;
    final resp = await app.httpClient.delete(
      app.buildUrl(_storagePath(path)),
      headers: app.headers,
    );

    if (resp.statusCode != 200 && resp.statusCode != 204) {
      throw FirebaseException(
        'Delete failed: ${resp.statusCode}',
        code: 'storage/delete-failed',
      );
    }
  }
}
