package com.example.nreader.ui;

import android.os.Bundle;
import android.os.Environment;
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
        Log.d("StoragePublicDirectory", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString());
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
        initReadButton(root, radioGroup);
    }

    private void initReadButton(final View root, final RadioGroup radioGroup) {
        root.findViewById(R.id.btn_read_number).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //play
                int index = radioGroup.getCheckedRadioButtonId();
                File dir = new File(Common.BASE_DIR, Integer.toString(index));
                if (!dir.exists())
                    return;
                EditText edtNumbers = root.findViewById(R.id.edt_numbers);
                char[] chars = edtNumbers.getText().toString().toCharArray();
                List<String> array = new ArrayList<>();
                for (char c : chars)
                    array.add(String.valueOf(c));
                MediaPlayerHelper.getInstance().play(dir, array);
            }
        });
    }
}