
package org.telegram.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
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
    private Button actionButton;
    private Button modeButton;

    private boolean registerMode = false;
    private boolean requestInProgress = false;

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private TextView makeText(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(41, 139, 220));
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        buildScreen();
        checkSavedSession();
    }

    private void buildScreen() {
        int blue = Color.rgb(41, 139, 220);
        int dark = Color.rgb(35, 45, 55);
        int grey = Color.rgb(125, 135, 145);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(28), dp(30), dp(28), dp(24));
        root.setBackgroundColor(Color.WHITE);

        TextView logo = makeText("FIxGram", 34, blue);
        logo.setTypeface(null, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);

        root.addView(logo, new LinearLayout.LayoutParams(
                -1, dp(65)
        ));

        TextView subtitle = makeText(
                "Твой мир общения", 16, grey
        );
        subtitle.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(-1, dp(35));
        subtitleParams.bottomMargin = dp(35);
        root.addView(subtitle, subtitleParams);

        usernameInput = new EditText(this);
        usernameInput.setSingleLine(true);
        usernameInput.setTextSize(16);
        usernameInput.setHint("Имя пользователя");
        usernameInput.setPadding(dp(16), 0, dp(16), 0);
        usernameInput.setBackground(rounded(
                Color.rgb(243, 245, 247), 12
        ));

        root.addView(usernameInput, new LinearLayout.LayoutParams(
                -1, dp(54)
        ));

        passwordInput = new EditText(this);
        passwordInput.setSingleLine(true);
        passwordInput.setTextSize(16);
        passwordInput.setHint("Пароль");
        passwordInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        passwordInput.setPadding(dp(16), 0, dp(16), 0);
        passwordInput.setBackground(rounded(
                Color.rgb(243, 245, 247), 12
        ));

        LinearLayout.LayoutParams passwordParams =
                new LinearLayout.LayoutParams(-1, dp(54));
        passwordParams.topMargin = dp(14);
        root.addView(passwordInput, passwordParams);

        actionButton = new Button(this);
        actionButton.setTextColor(Color.WHITE);
        actionButton.setTextSize(15);
        actionButton.setAllCaps(false);
        actionButton.setBackground(rounded(blue, 14));
        actionButton.setOnClickListener(v -> submit());

        LinearLayout.LayoutParams actionParams =
                new LinearLayout.LayoutParams(-1, dp(54));
        actionParams.topMargin = dp(24);
        root.addView(actionButton, actionParams);

        modeButton = new Button(this);
        modeButton.setAllCaps(false);
        modeButton.setTextColor(blue);
        modeButton.setBackgroundColor(Color.TRANSPARENT);
        modeButton.setOnClickListener(v -> {
            registerMode = !registerMode;
            updateMode();
        });

        root.addView(modeButton, new LinearLayout.LayoutParams(
                -1, dp(50)
        ));

        statusText = makeText("", 13, grey);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(dp(8), dp(10), dp(8), dp(10));

        root.addView(statusText, new LinearLayout.LayoutParams(
                -1, -2
        ));

        TextView footer = makeText(
                "FIxGram · Независимый мессенджер",
                12, grey
        );
        footer.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams footerParams =
                new LinearLayout.LayoutParams(-1, dp(40));
        footerParams.topMargin = dp(20);
        root.addView(footer, footerParams);

        setContentView(root);
        updateMode();
    }

    private void updateMode() {
        if (registerMode) {
            actionButton.setText("Создать аккаунт");
            modeButton.setText("Уже есть аккаунт? Войти");
        } else {
            actionButton.setText("Войти");
            modeButton.setText("Нет аккаунта? Зарегистрироваться");
        }

        statusText.setText("");
    }

    private void submit() {
        if (requestInProgress) {
            return;
        }

        String username = usernameInput.getText()
                .toString().trim().toLowerCase();

        String password = passwordInput.getText()
                .toString();

        if (!username.matches("[a-z0-9_]{5,32}")) {
            usernameInput.setError(
                    "Используй 5–32 символа: a-z, 0-9 или _"
            );
            return;
        }

        if (password.length() < 10) {
            passwordInput.setError(
                    "Пароль должен содержать минимум 10 символов"
            );
            return;
        }

        requestInProgress = true;
        actionButton.setEnabled(false);
        actionButton.setText("Подключение...");
        statusText.setText("Связываемся с сервером FIxGram...");

        FixGramApi.AuthCallback callback =
                (success, data, error) -> {
                    requestInProgress = false;
                    actionButton.setEnabled(true);
                    updateMode();

                    if (success) {
                        statusText.setText("Вход выполнен!");
                        Toast.makeText(
                                this,
                                registerMode
                                        ? "Аккаунт создан!"
                                        : "Добро пожаловать в FIxGram!",
                                Toast.LENGTH_SHORT
                        ).show();

                        openChats();
                    } else {
                        String message = error == null
                                ? "Не удалось выполнить запрос"
                                : error;

                        statusText.setText(message);
                        Toast.makeText(
                                this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                };

        if (registerMode) {
            FixGramApi.register(
                    this,
                    username,
                    username,
                    password,
                    callback
            );
        } else {
            FixGramApi.login(
                    this,
                    username,
                    password,
                    callback
            );
        }
    }

    private void checkSavedSession() {
        String token = FixGramApi.getSavedToken(this);

        if (token == null || token.isEmpty()) {
            return;
        }

        statusText.setText("Проверяем сохранённый вход...");

        FixGramApi.getMe(token, (success, data, error) -> {
            if (success) {
                openChats();
            } else {
                FixGramApi.clearToken(this);
                statusText.setText(
                        "Войди в свой аккаунт FIxGram"
                );
            }
        });
    }

    private void openChats() {
        startActivity(new android.content.Intent(
                FixGramLoginActivity.this,
                FixGramChatsActivity.class
        ));
        finish();
    }
}
