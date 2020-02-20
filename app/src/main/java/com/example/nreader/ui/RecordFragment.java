package com.example.nreader.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nreader.R;
import com.example.nreader.util.Common;
import com.example.nreader.util.FileUtil;
import com.example.nreader.util.SharedPreferencesUtil;

import java.io.File;
import java.util.ArrayList;

public class RecordFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("RecordFragment", "onCreateView called");
        View root = inflater.inflate(R.layout.fragment_record, container, false);
        initView(root);
        return root;
    }

    private void initView(View root) {
        //init RadioGroup
        ArrayList<String> names = SharedPreferencesUtil.getArray(SharedPreferencesUtil.KEY_NAMES);
        RadioGroup radioGroup = root.findViewById(R.id.rgp_names);
        Common.initRadioGroup(root, names, radioGroup);
        initModifyButton(root, names, R.id.btn_record_new, null);
        initModifyButton(root, names, R.id.btn_record_modify, radioGroup);
        initDeleteButton(root, names, radioGroup);
    }

    private void initDeleteButton(final View root, final ArrayList<String> names, final RadioGroup radioGroup) {
        root.findViewById(R.id.btn_record_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int id = radioGroup.getCheckedRadioButtonId();
                if (id < 0)
                    return;
                //show Alertdialog
                final Context context = root.getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.text_confirm_delete);
                builder.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File parent = context.getFilesDir();
                        File dst = new File(parent, Integer.toString(id));
                        FileUtil.deleteDir(dst);
                        for (int i = id + 1; i < names.size(); i++){
                            File src = new File(parent, Integer.toString(i));
                            src.renameTo(dst);
                            dst = src;
                        }
                        names.remove(id);
                        SharedPreferencesUtil.putArray(SharedPreferencesUtil.KEY_NAMES, names);
                        Common.initRadioGroup(root, names, radioGroup);
                    }
                });
                builder.setNegativeButton(R.string.btn_cancel, null);
                builder.create().show();
            }
        });
    }

    private void initModifyButton(View root, final ArrayList<String> names, int id, final RadioGroup radioGroup) {
        root.findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = names.size();
                if (radioGroup != null) {
                    index = radioGroup.getCheckedRadioButtonId();
                    if (index < 0)
                        return;
                }
                showRecordDialogFragment(names, index);
            }
        });
    }

    //https://stackoverflow.com/questions/34526866/return-values-from-dialogfragment
    private void showRecordDialogFragment(ArrayList<String> names, int index) {
        RecordDialogFragment dialog = new RecordDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(RecordDialogFragment.KEY_NAMES, names);
        args.putInt(RecordDialogFragment.KEY_INDEX, index);
        dialog.setArguments(args);
        dialog.setTargetFragment(this, RecordDialogFragment.DIALOG_REQUEST_CODE);
        dialog.show(getFragmentManager(), "dialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RecordDialogFragment.DIALOG_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                View root = getView();
                ArrayList<String> names = SharedPreferencesUtil.getArray(SharedPreferencesUtil.KEY_NAMES);
                RadioGroup radioGroup = root.findViewById(R.id.rgp_names);
                Common.initRadioGroup(root, names, radioGroup);
            }
        }
    }
}