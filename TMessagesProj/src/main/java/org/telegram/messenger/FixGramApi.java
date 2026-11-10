
package org.telegram.messenger;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FixGramApi {

    private static final String BASE_URL =
            "https://fixgram-api.smolnikov0850.workers.dev";

    public interface Callback {
        void onResult(boolean success, String response);
    }

    public static void checkHealth(Callback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(BASE_URL + "/api/health");
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int status = connection.getResponseCode();

                InputStream stream = status >= 200 && status < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(stream)
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();

                JSONObject json = new JSONObject(result.toString());

                boolean success = status == 200
                        && "ok".equals(json.optString("status"));

                callback.onResult(success, result.toString());

            } catch (Exception e) {
                callback.onResult(false, e.getMessage());

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
