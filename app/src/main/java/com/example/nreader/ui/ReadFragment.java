package com.example.nreader.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.nreader.R;
import com.example.nreader.util.Common;
import com.example.nreader.util.MediaPlayerHelper;
import com.example.nreader.util.SharedPreferencesUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReadFragment extends Fragment {
    private static final int DEFAULT_NUMBER_LENGTH = 4;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("ReadFragment", "onCreateView called");
        View root = inflater.inflate(R.layout.fragment_read, container, false);
        initView(root);
        return root;
    }

    private void initView(View root) {
        //init RadioGroup
        ArrayList<String> names = SharedPreferencesUtil.getArray(SharedPreferencesUtil.KEY_NAMES);
        RadioGroup radioGroup = root.findViewById(R.id.rgp_names);
        if (names.isEmpty()) {
            TextView txt = root.findViewById(R.id.txt_choose_name);
            txt.setText(R.string.text_no_name);
        } else
            Common.initRadioGroup(root, names, radioGroup);
        initLengthEdit(root);
        initNumbersEdit(root, radioGroup);
    }

    private void initNumbersEdit(final View root, final RadioGroup radioGroup) {
        final EditText edtNumbers = root.findViewById(R.id.edt_numbers);
        final EditText edtLength = root.findViewById(R.id.edt_length);
        edtNumbers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int max = Integer.parseInt(edtLength.getText().toString());
                int len = s.length();
                if (len == max){
                    //play
                    Context context = root.getContext();
                    int index = radioGroup.getCheckedRadioButtonId();
                    File dir = new File(context.getFilesDir(), Integer.toString(index));
                    List<String> array = new ArrayList<>();
                    for (int i = 0; i < len; i++)
                        array.add(String.valueOf(s.charAt(i)));
                    MediaPlayerHelper.getInstance().play(dir, array);
                } else if (len > max) {
                    //left only new input
                    edtNumbers.removeTextChangedListener(this);
                    edtNumbers.setText(String.valueOf(s.charAt(len - 1)));
                    edtNumbers.addTextChangedListener(this);
                    edtNumbers.setSelection(1);
                }
            }
        });
    }

    private void initLengthEdit(View root) {
        final EditText edtLength = root.findViewById(R.id.edt_length);
        int length = SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEY_NUMBER_LENGTH, DEFAULT_NUMBER_LENGTH);
        edtLength.setText(Integer.toString(length));
        edtLength.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int length = Integer.parseInt(edtLength.getText().toString());
                    SharedPreferencesUtil.putInt(SharedPreferencesUtil.KEY_NUMBER_LENGTH, length);
                }
            }
        });
    }
}