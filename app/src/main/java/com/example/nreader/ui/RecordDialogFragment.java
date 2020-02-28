package com.example.nreader.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.nreader.R;
import com.example.nreader.util.AudioRecorder;
import com.example.nreader.util.Common;
import com.example.nreader.util.FileUtil;
import com.example.nreader.util.MediaPlayerHelper;
import com.example.nreader.util.SharedPreferencesUtil;

import java.io.File;
import java.util.ArrayList;

public class RecordDialogFragment extends DialogFragment {
    public static final String KEY_NAMES = "KEY_NAMES";
    public static final String KEY_INDEX = "KEY_INDEX";
    public static final String BTN_RECORD = "btn_record";
    public static final int DIALOG_REQUEST_CODE = 9527;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AudioRecorder.getInstance().init();
        //NO title: https://blog.csdn.net/trojx2/article/details/52768772
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View root = inflater.inflate(R.layout.dialogfragment_record, container, false);
        Bundle args = getArguments();
        ArrayList<String> names = args.getStringArrayList(KEY_NAMES);
        int index = args.getInt(KEY_INDEX);
        Log.d("RecordDialogFragment", names.toString() + index);
        initView(root, names, index);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AudioRecorder.getInstance().release();
    }

    protected void initView(View root, ArrayList<String> names, int index) {
        boolean isNew = index == names.size();
        if (!isNew) {
            EditText editText = root.findViewById(R.id.edt_name);
            editText.setText(names.get(index));
        }
        Context context = root.getContext();
        // create dir
        File dir = new File(Common.BASE_DIR, Integer.toString(index));
        if (!dir.exists())
            dir.mkdirs();
        initRecordButtons(root, dir, context.getPackageName());
        initSaveButton(root, names, index, isNew);
        initPlayButton(root, dir);
        initCancelButton(root, isNew, dir);
    }

    private void initCancelButton(View root, final boolean isNew, final File dir) {
        root.findViewById(R.id.btn_record_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNew)
                    FileUtil.deleteDir(dir);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
                dismiss();
            }
        });
    }

    private void initPlayButton(View root, final File dir) {
        root.findViewById(R.id.btn_record_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayerHelper.getInstance().play(dir);
            }
        });
    }

    private void initSaveButton(final View root, final ArrayList<String> names, final int index, final boolean isNew) {
        root.findViewById(R.id.btn_record_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = root.findViewById(R.id.edt_name);
                String name = editText.getText().toString();
                int i = names.indexOf(name);
                if (i >= 0 && i != index) {
                    editText.setText("");
                    editText.setHint(R.string.hint_duplicated_name);
                    return;
                }
                int resultCode = Activity.RESULT_OK;
                if (isNew)
                    names.add(name);
                else if (i == -1)
                    names.set(index, name);
                else
                    resultCode = Activity.RESULT_CANCELED;
                if (resultCode == Activity.RESULT_OK)
                    SharedPreferencesUtil.putArray(SharedPreferencesUtil.KEY_NAMES, names);
                getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
                dismiss();
            }
        });
    }

    private void initRecordButtons(View root, final File dir, String packageName) {
        Resources res = getResources();
        for (int i = 0; i < 10; i++) {
            //https://blog.csdn.net/weixin_39183543/article/details/79440997
            int id = res.getIdentifier(BTN_RECORD + i, "id", packageName);
            root.findViewById(id).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Button btn = (Button)v;
                    String num = btn.getText().toString();
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        //start record
                        AudioRecorder.getInstance().startRecord(dir, num);
                    }else if(event.getAction() == MotionEvent.ACTION_UP){
                        //stop record
                        AudioRecorder.getInstance().stopRecord();
                    }
                    return true;
                }
            });
        }
    }
}
