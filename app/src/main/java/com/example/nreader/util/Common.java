package com.example.nreader.util;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.List;

public class Common {
    public static final String SUFFIX_WAV = ".wav";

    public static void initRadioGroup(View root, List<String> array, RadioGroup radioGroup) {
        radioGroup.removeAllViews();
        if (!array.isEmpty()) {
            int id = 0;
            for (String s : array) {
                RadioButton btn = new RadioButton(root.getContext());
                btn.setText(s);
                btn.setTextSize(30);
                btn.setId(id++);
                //btn.setGravity(Gravity.CENTER);
                radioGroup.addView(btn);
            }
            radioGroup.clearCheck();
            radioGroup.check(0);
        }
    }
}
