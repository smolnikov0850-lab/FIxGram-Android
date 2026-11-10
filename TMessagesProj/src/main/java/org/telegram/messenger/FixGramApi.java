
package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FixGramApi {

    private static final String BASE_URL =
            "https://fixgram-api.smolnikov0850.workers.dev";

    private static final String PREFS_NAME = "fixgram_auth";
    private static final String TOKEN_KEY = "session_token";

    public interface Callback {
        void onResult(boolean success, String response);
    }

    public interface AuthCallback {
        void onResult(boolean success, JSONObject data, String error);
    }

    // Проверка подключения к серверу
    public static void checkHealth(Callback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection)
                        new URL(BASE_URL + "/api/health").openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int status = connection.getResponseCode();
                String result = readResponse(connection, status);

                JSONObject json = new JSONObject(result);

                boolean success = status == 200
                        && "ok".equals(json.optString("status"));

                callback.onResult(success, result);

            } catch (Exception e) {
                callback.onResult(false, e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    // Регистрация нового аккаунта
    public static void register(
            Context context,
            String username,
            String displayName,
            String password,
            AuthCallback callback
    ) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username.trim().toLowerCase());
            body.put("display_name", displayName.trim());
            body.put("password", password);

            request("POST", "/api/register", body, null,
                    (success, data, error) -> {
                        if (success) {
                            saveToken(context, data.optString("token"));
                        }
                        callback.onResult(success, data, error);
                    });

        } catch (Exception e) {
            deliver(callback, false, null, e.getMessage());
        }
    }

    // Вход в существующий аккаунт
    public static void login(
            Context context,
            String username,
            String password,
            AuthCallback callback
    ) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username.trim().toLowerCase());
            body.put("password", password);

            request("POST", "/api/login", body, null,
                    (success, data, error) -> {
                        if (success) {
                            saveToken(context, data.optString("token"));
                        }
                        callback.onResult(success, data, error);
                    });

        } catch (Exception e) {
            deliver(callback, false, null, e.getMessage());
        }
    }

    // Проверка сохранённой сессии
    public static void getMe(String token, AuthCallback callback) {
        request("GET", "/api/me", null, token, callback);
    }

    // Сохранение токена на устройстве
    public static void saveToken(Context context, String token) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(TOKEN_KEY, token)
                .apply();
    }

    // Получение сохранённого токена
    public static String getSavedToken(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(TOKEN_KEY, null);
    }

    // Выход из аккаунта на устройстве
    public static void clearToken(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(TOKEN_KEY)
                .apply();
    }

    // Общий HTTP-запрос к FIxGram API
    private static void request(
            String method,
            String path,
            JSONObject body,
            String token,
            AuthCallback callback
    ) {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection)
                        new URL(BASE_URL + path).openConnection();

                connection.setRequestMethod(method);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty(
                        "Accept", "application/json");

                if (body != null) {
                    connection.setDoOutput(true);
                    connection.setRequestProperty(
                            "Content-Type", "application/json; charset=UTF-8");

                    byte[] bytes = body.toString()
                            .getBytes(StandardCharsets.UTF_8);

                    try (OutputStream output = connection.getOutputStream()) {
                        output.write(bytes);
                    }
                }

                if (token != null && !token.isEmpty()) {
                    connection.setRequestProperty(
                            "Authorization", "Bearer " + token);
                }

                int status = connection.getResponseCode();
                String response = readResponse(connection, status);

                JSONObject json = new JSONObject(response);
                boolean success = status >= 200 && status < 300;

                String error = success
                        ? null
                        : json.optString("error", "Request failed: " + status);

                deliver(callback, success, json, error);

            } catch (Exception e) {
                deliver(callback, false, null, e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private static String readResponse(
            HttpURLConnection connection,
            int status
    ) throws Exception {
        InputStream stream = status >= 200 && status < 300
                ? connection.getInputStream()
                : connection.getErrorStream();

        if (stream == null) {
            return "{}";
        }

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }

    // Возвращаем результат в главный UI-поток Android
    private static void deliver(
            AuthCallback callback,
            boolean success,
            JSONObject data,
            String error
    ) {
        new Handler(Looper.getMainLooper()).post(
                () -> callback.onResult(success, data, error));
    }
}
