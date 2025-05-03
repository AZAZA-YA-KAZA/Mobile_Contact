package com.example.mobile_contact;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_contact.databinding.ActivityWallpaperBinding;

public class Wallpaper extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWallpaperBinding binding = ActivityWallpaperBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AlphaAnimation animIn = new AlphaAnimation(0f, 1f);
        animIn.setDuration(2000);
        animIn.setFillAfter(true);
        AlphaAnimation animOut = new AlphaAnimation(1f, 0f);
        animOut.setDuration(2000);
        animOut.setFillAfter(true);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.IM.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        binding.IM.startAnimation(animIn);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.IM.startAnimation(animOut);
            }
        }, 3000);
    }
}
