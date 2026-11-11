
package org.telegram.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.telegram.messenger.FixGramApi;

public class FixGramLoginActivity extends Activity {

    private EditText usernameInput;
    private EditText passwordInput;
    private TextView statusText;
    private Button submitButton;
    private Button switchButton;

    private boolean registerMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScreen();
        checkSavedSession();
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private void createScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(32), dp(28), dp(32));

        TextView title = new TextView(this);
        title.setText("FIxGram");
        title.setTextSize(34);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("Твой мессенджер");
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, matchWrap());

        addSpace(root, 35);

        statusText = new TextView(this);
        statusText.setText("Войди или создай аккаунт");
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);
        root.addView(statusText, matchWrap());

        addSpace(root, 20);

        usernameInput = new EditText(this);
        usernameInput.setSingleLine(true);
        usernameInput.setHint("@username");
        usernameInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        root.addView(usernameInput, matchWidth());

        passwordInput = new EditText(this);
        passwordInput.setSingleLine(true);
        passwordInput.setHint("Пароль (минимум 6 символов)");
        passwordInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(passwordInput, matchWidth());

        addSpace(root, 20);

        submitButton = new Button(this);
        submitButton.setOnClickListener(v -> submit());
        root.addView(submitButton, matchWidth());

        switchButton = new Button(this);
        switchButton.setOnClickListener(v -> {
            registerMode = !registerMode;
            updateMode();
        });
        root.addView(switchButton, matchWidth());

        updateMode();
        setContentView(root);
    }

    private void updateMode() {
        submitButton.setText(
                registerMode ? "Создать аккаунт" : "Войти");

        switchButton.setText(
                registerMode
                        ? "Уже есть аккаунт? Войти"
                        : "Создать новый аккаунт");

        statusText.setText(
                registerMode
                        ? "Регистрация FIxGram"
                        : "Вход в FIxGram");
    }

    private void submit() {
        String username = usernameInput.getText()
                .toString().trim().replaceFirst("^@", "")
                .toLowerCase();

        String password = passwordInput.getText().toString();

        if (!username.matches("[a-z0-9_]{5,32}")) {
            showMessage("Username: 5–32 символа, латиница, цифры и _");
            return;
        }

        if (password.length() < 6) {
            showMessage("Пароль должен содержать минимум 6 символов");
            return;
        }

        final boolean creatingAccount = registerMode;

        submitButton.setEnabled(false);
        statusText.setText("Подключение к FIxGram...");

        FixGramApi.AuthCallback callback = (success, data, error) -> {
            submitButton.setEnabled(true);

            if (success) {
                JSONObject user = data.optJSONObject("user");
                String name = user != null
                        ? user.optString("username", username)
                        : username;

                statusText.setText("Вы вошли как @" + name);

                showMessage(creatingAccount
                        ? "Аккаунт создан!"
                        : "Вход выполнен!");
            } else {
                statusText.setText("Не удалось выполнить запрос");
                showMessage(error != null ? error : "Ошибка сервера");
            }
        };

        if (creatingAccount) {
            // Пока передаём username как отображаемое имя,
            // чтобы сохранить совместимость с текущим API.
            FixGramApi.register(
                    this, username, username, password, callback);
        } else {
            FixGramApi.login(
                    this, username, password, callback);
        }
    }

    private void checkSavedSession() {
        String token = FixGramApi.getSavedToken(this);

        if (token == null || token.isEmpty()) {
            return;
        }

        statusText.setText("Проверяем сессию...");

        FixGramApi.getMe(token, (success, data, error) -> {
            if (success) {
                JSONObject user = data.optJSONObject("user");
                if (user != null) {
                    statusText.setText(
                            "Вы вошли как @"
                                    + user.optString("username"));
                }
            } else {
                FixGramApi.clearToken(this);
                statusText.setText("Войди в аккаунт FIxGram");
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchWidth() {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(10);
        return params;
    }

    private void addSpace(LinearLayout layout, int height) {
        View space = new View(this);
        layout.addView(space,
                new LinearLayout.LayoutParams(1, dp(height)));
    }
}
