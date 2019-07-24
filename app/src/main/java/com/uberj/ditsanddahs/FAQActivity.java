package com.uberj.ditsanddahs;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FAQActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faq_activity);
        WebView webView = findViewById(R.id.faqView);
        webView.loadUrl("https://ditsanddahs.uberj.com/faq.html");
    }
}
