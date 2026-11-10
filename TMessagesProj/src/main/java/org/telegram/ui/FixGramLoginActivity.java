
package org.telegram.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.telegram.messenger.FixGramApi;

public class FixGramLoginActivity extends Activity {

    private EditText usernameInput;
    private EditText displayNameInput;
    private EditText passwordInput;

    private TextView statusText;
    private Button submitButton;

    private boolean registerMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createScreen();
        checkSavedSession();
    }

    private void createScreen() {
        ScrollView scrollView = new ScrollView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(28), dp(40), dp(28), dp(28));
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        scrollView.addView(layout);

        TextView title = new TextView(this);
        title.setText("FIxGram");
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);
        layout.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("Твой независимый мессенджер");
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);
        addSpace(layout, 8);
        layout.addView(subtitle, matchWrap());

        addSpace(layout, 32);

        statusText = new TextView(this);
        statusText.setText("Войди или создай аккаунт FIxGram");
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);
        layout.addView(statusText, matchWrap());

        addSpace(layout, 24);

        usernameInput = new EditText(this);
        usernameInput.setSingleLine(true);
        usernameInput.setHint("Имя пользователя");
        usernameInput.setInputType( android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        layout.addView(usernameInput, matchWidth());

        displayNameInput = new EditText(this);
        displayNameInput.setSingleLine(true);
        displayNameInput.setHint("Отображаемое имя");
        layout.addView(displayNameInput, matchWidth());

        passwordInput = new EditText(this);
        passwordInput.setSingleLine(true);
        passwordInput.setHint("Пароль");
        passwordInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordInput, matchWidth());

        addSpace(layout, 20);

        submitButton = new Button(this);
        submitButton.setText("Войти");
        submitButton.setOnClickListener(v -> submit());
        layout.addView(submitButton, matchWidth());

        Button switchButton = new Button(this);
        switchButton.setText("Создать новый аккаунт");
        switchButton.setOnClickListener(v -> {
            registerMode = !registerMode;
            updateMode();
        });
        layout.addView(switchButton, matchWidth());

        Button logoutButton = new Button(this);
        logoutButton.setText("Выйти из аккаунта");
        logoutButton.setOnClickListener(v -> {
            FixGramApi.clearToken(this);
            statusText.setText("Локальная сессия удалена");
            Toast.makeText(this, "Вы вышли из FIxGram",
                    Toast.LENGTH_SHORT).show();
        });
        addSpace(layout, 12);
        layout.addView(logoutButton, matchWidth());

        updateMode();
        setContentView(scrollView);
    }

    private void updateMode() {
        displayNameInput.setVisibility(
                registerMode ? View.VISIBLE : View.GONE);

        submitButton.setText(
                registerMode ? "Создать аккаунт" : "Войти");

        passwordInput.setHint(
                registerMode ? "Пароль (минимум 10 символов)" : "Пароль");

        statusText.setText(
                registerMode
                        ? "Регистрация нового аккаунта FIxGram"
                        : "Вход в аккаунт FIxGram");
    }

    private void submit() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String displayName = displayNameInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Заполни имя пользователя и пароль");
            return;
        }

        if (registerMode && displayName.isEmpty()) {
            showMessage("Введи отображаемое имя");
            return;
        }

        submitButton.setEnabled(false);
        statusText.setText("Подключение к FIxGram...");

        FixGramApi.AuthCallback callback = (success, data, error) -> {
            submitButton.setEnabled(true);

            if (success) {
                JSONObject user = data.optJSONObject("user");

                String name = user != null
                        ? user.optString("display_name", username)
                        : username;

                statusText.setText("Вы вошли как " + name);

                showMessage(
                        registerMode
                                ? "Аккаунт FIxGram создан!"
                                : "Вход выполнен!");

            } else {
                statusText.setText("Не удалось выполнить запрос");
                showMessage(error != null ? error : "Неизвестная ошибка");
            }
        };

        if (registerMode) {
            FixGramApi.register(
                    this, username, displayName, password, callback);
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

        statusText.setText("Проверяем сохранённую сессию...");

        FixGramApi.getMe(token, (success, data, error) -> {
            if (success) {
                JSONObject user = data.optJSONObject("user");

                if (user != null) {
                    statusText.setText(
                            "Вы вошли как "
                                    + user.optString("display_name"));
                }
            } else {
                FixGramApi.clearToken(this);
                statusText.setText("Сессия истекла. Войди снова.");
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchWidth() {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(8);
        return params;
    }

    private void addSpace(LinearLayout layout, int height) {
        View space = new View(this);
        layout.addView(space, new LinearLayout.LayoutParams(
                1, dp(height)));
    }
}
