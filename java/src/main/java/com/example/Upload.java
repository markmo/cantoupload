package com.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONObject;

public class Upload {

    private static final String OAUTH_BASE_URL = "https://oauth.canto.global";
    private static final String SITE_BASEURL = "https://XXX.canto.global";
    private static final String APP_ID = System.getenv("APP_ID");
    private static final String APP_SECRET = System.getenv("APP_SECRET");

    public static void main(String[] args) {
        try {
            // Get access token
            String tokenUrl = String.format("%s/oauth/api/oauth2/token?app_id=%s&app_secret=%s&grant_type=client_credentials", OAUTH_BASE_URL, APP_ID, APP_SECRET);
            JSONObject tokenResponse = sendPostRequest(tokenUrl, null);
            String accessToken = tokenResponse.getString("accessToken");
            String tokenType = tokenResponse.getString("tokenType");

            // Get upload settings
            String uploadSettingsUrl = SITE_BASEURL + "/api/v1/upload/setting";
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", tokenType + " " + accessToken);
            JSONObject uploadSettingsResponse = sendGetRequest(uploadSettingsUrl, headers);

            // Upload file
            String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/3/38/EE-logo-yellow.png";
            String uploadUrl = uploadSettingsResponse.getString("url");

            Map<String, String> formData = new HashMap<>();
            formData.put("key", uploadSettingsResponse.getString("key"));
            formData.put("acl", uploadSettingsResponse.getString("acl"));
            formData.put("AWSAccessKeyId", uploadSettingsResponse.getString("AWSAccessKeyId"));
            formData.put("Policy", uploadSettingsResponse.getString("Policy"));
            formData.put("Signature", uploadSettingsResponse.getString("Signature"));
            formData.put("x-amz-meta-file_name", "EE-logo-yellow.png");
            formData.put("x-amz-meta-tag", "");
            formData.put("x-amz-meta-scheme", "");
            formData.put("x-amz-meta-id", "");
            formData.put("x-amz-meta-album_id", "G1BAM");

            // Fetch image
            byte[] imageBytes = fetchImage(imageUrl);

            // Send file upload request
            int responseCode = sendMultipartPostRequest(uploadUrl, formData, imageBytes);
            System.out.println("Response Code: " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONObject sendPostRequest(String requestUrl, Map<String, String> headers) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        conn.setDoOutput(true);
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return new JSONObject(readStream(conn.getInputStream()));
        } else {
            throw new IOException("Failed to get response, HTTP response code: " + responseCode);
        }
    }

    private static JSONObject sendGetRequest(String requestUrl, Map<String, String> headers) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return new JSONObject(readStream(conn.getInputStream()));
        } else {
            throw new IOException("Failed to get response, HTTP response code: " + responseCode);
        }
    }

    private static String readStream(InputStream inputStream) throws IOException {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    private static byte[] fetchImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    private static int sendMultipartPostRequest(String requestUrl, Map<String, String> formData, byte[] fileData) throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        String CRLF = "\r\n";
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

        for (Map.Entry<String, String> entry : formData.entrySet()) {
            writer.append("--").append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append(CRLF);
            writer.append("Content-Type: text/plain; charset=UTF-8").append(CRLF).append(CRLF);
            writer.append(entry.getValue()).append(CRLF);
            writer.flush();
        }

        writer.append("--").append(boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"EE-logo-yellow.png\"").append(CRLF);
        writer.append("Content-Type: image/png").append(CRLF);
        writer.append("Content-Transfer-Encoding: binary").append(CRLF).append(CRLF);
        writer.flush();

        os.write(fileData);
        os.flush();

        writer.append(CRLF).append("--").append(boundary).append("--").append(CRLF);
        writer.flush();
        writer.close();
        os.close();

        return conn.getResponseCode();
    }
}