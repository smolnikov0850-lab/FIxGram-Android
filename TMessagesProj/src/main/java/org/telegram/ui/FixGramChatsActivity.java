
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.FixGramApi;

public class FixGramChatsActivity extends Activity {

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(34,  Unused));
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        buildScreen();
    }

    private void buildScreen() {
        int blue = Color.rgb(41, 139, 220);
        int dark = Color.rgb(35, 45, 55);
        int grey = Color.rgb(125, 135, 145);
        int background = Color.rgb(255, 255, 255);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(background);

        // Верхняя панель
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(18), dp(12), dp(18), dp(12));
        toolbar.setBackgroundColor(blue);

        TextView title = text("FIxGram", 23, Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        toolbar.addView(title, new LinearLayout.LayoutParams(
                0, dp(42), 1
        ));

        TextView menu = text("⋮", 30, Color.WHITE);
        menu.setGravity(Gravity.CENTER);
        menu.setOnClickListener(v ->
                Toast.makeText(this, "Меню FIxGram скоро появится",
                        Toast.LENGTH_SHORT).show()
        );
        toolbar.addView(menu, new LinearLayout.LayoutParams(
                dp(40), dp(42)
        ));

        root.addView(toolbar);

        // Поиск
        LinearLayout searchArea = new LinearLayout(this);
        searchArea.setPadding(dp(16), dp(12), dp(16), dp(8));

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setHint("Поиск");
        search.setPadding(dp(16), 0, dp(16), 0);
        search.setBackground(shape(Color.rgb(242, 244, 246), 24));
        searchArea.addView(search, new LinearLayout.LayoutParams(
                -1, dp(44)
        ));

        root.addView(searchArea);

        // Область списка чатов
        FrameLayout content = new FrameLayout(this);
        content.setBackgroundColor(background);

        LinearLayout empty = new LinearLayout(this);
        empty.setOrientation(LinearLayout.VERTICAL);
        empty.setGravity(Gravity.CENTER);

        TextView icon = text("✉", 54, Color.rgb(175, 190, 202));
        icon.setGravity(Gravity.CENTER);
        empty.addView(icon);

        TextView emptyTitle = text("Пока нет чатов", 20, dark);
        emptyTitle.setTypeface(null, Typeface.BOLD);
        emptyTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(-1, -2);
        titleParams.topMargin = dp(12);
        empty.addView(emptyTitle, titleParams);

        TextView description = text(
                "Найди человека и начни общение в FIxGram",
                14, grey
        );
        description.setGravity(Gravity.CENTER);
        description.setPadding(dp(28), dp(8), dp(28), 0);
        empty.addView(description);

        Button findButton = new Button(this);
        findButton.setText("Найти пользователей");
        findButton.setOnClickListener(v ->
                Toast.makeText(this, "Поиск пользователей подключим следующим этапом",
                        Toast.LENGTH_SHORT).show()
        );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(-2, -2);
        buttonParams.topMargin = dp(18);
        empty.addView(findButton, buttonParams);

        FrameLayout.LayoutParams emptyParams =
                new FrameLayout.LayoutParams(-1, -1);
        content.addView(empty, emptyParams);

        root.addView(content, new LinearLayout.LayoutParams(
                -1, 0, 1
        ));

        // Нижняя панель навигации
        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER);
        bottom.setBackgroundColor(Color.WHITE);
        bottom.setElevation(dp(8));
        bottom.setPadding(dp(4), dp(8), dp(4), dp(8));

        addTab(bottom, "Чаты", true);
        addTab(bottom, "Контакты", false);
        addTab(bottom, "Профиль", false);

        root.addView(bottom, new LinearLayout.LayoutParams(
                -1, dp(62)
        ));

        // Кнопка создания чата
        FrameLayout page = new FrameLayout(this);
        page.addView(root);

        TextView plus = text("+", 30, Color.WHITE);
        plus.setGravity(Gravity.CENTER);
        plus.setBackground(shape(blue, 32));
        plus.setElevation(dp(6));
        plus.setOnClickListener(v ->
                Toast.makeText(this, "Создание чата подключим следующим этапом",
                        Toast.LENGTH_SHORT).show()
        );

        FrameLayout.LayoutParams plusParams =
                new FrameLayout.LayoutParams(dp(58), dp(58),
                        Gravity.RIGHT | Gravity.BOTTOM);
        plusParams.setMargins(0, 0, dp(20), dp(82));
        page.addView(plus, plusParams);

        setContentView(page);
    }

    private void addTab(LinearLayout parent, String label, boolean selected) {
        int color = selected
                ? Color.rgb(41, 139, 220)
                : Color.rgb(135, 145, 155);

        TextView tab = text(label, 13, color);
        tab.setGravity(Gravity.CENTER);
        tab.setOnClickListener(v -> {
            if (!selected) {
                Toast.makeText(this,
                        label + " скоро появится",
                        Toast.LENGTH_SHORT).show();
            }
        });

        parent.addView(tab, new LinearLayout.LayoutParams(
                0, dp(46), 1
        ));
    }
}
