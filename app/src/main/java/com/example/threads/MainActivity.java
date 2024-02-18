package com.example.threads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.example.threads.databinding.ActivityMainBinding;

import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    static String res = "";

    static int queue;

    static Object lock = new Object();

    static boolean[] end = {false, false, false};

    Handler handler;

    public int randInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                String str = (String) msg.obj;
                binding.result.setText(str);
            }
        };

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queue = 1;
                FirstThread firstThread = new FirstThread(binding.text1.getText().toString() + ' ', 1);
                FirstThread secondThread = new FirstThread(binding.text2.getText().toString() + ' ', 2);
                FirstThread thirdThread = new FirstThread(binding.text3.getText().toString() + ' ', 3);
                firstThread.start();
                secondThread.start();
                thirdThread.start();
            }
        });
    }

    class FirstThread extends Thread {
        private char[] textCh;
        private String text;
        private int q;

        public FirstThread(String text, int queue) {
            this.text = text;
            this.textCh = new char[text.length()];
            this.q = queue;
        }

        @Override
        public void run() {
            super.run();
            char[] chText = text.toCharArray();


            for (int i = 0; i < chText.length; ++i) {
                synchronized (lock) {
                    while (this.q != queue) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    char ch = chText[i];
                    textCh[i] = ch;
                    res += ch;

                    Message msg = new Message();
                    msg.obj = res;
                    handler.sendMessage(msg);

                    try {
//                        if (ch == '=' || ch == '-' || ch == ',' || ch == '.' || ch == '?') {
//                            Thread.sleep(1000);
//                        } else {
//                            Thread.sleep(200);
//                        }
                        Thread.sleep(randInt(100, 300));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if (i == chText.length - 1) {
                        end[this.q - 1] = true;
                        while (!(end[0] && end[1] && end[2])) {
                            while (this.q != queue) {
                                try {
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            queue = queue % 3 + 1;
                            lock.notifyAll();
                        }
                    } else if (ch == ' ') {
                        queue = queue % 3 + 1;
                        lock.notifyAll();
                    }
                }
            }
        }
    }
}