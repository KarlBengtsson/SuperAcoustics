package com.example.myapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;


public class ReverbFragment extends DialogFragment implements TextView.OnEditorActionListener {
    private EditText one;
    private EditText two;
    private EditText three;
    private EditText four;
    private EditText five;
    private EditText six;


public interface ReverbDialogListener {
    void onFinishEditDialog(String a, String b, String c, String d, String e, String f);
}

    public ReverbFragment() {
    }

    public static ReverbFragment newInstance(String title) {
        ReverbFragment frag = new ReverbFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reverb, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        one = (EditText) view.findViewById(R.id.editText1);
        two = (EditText) view.findViewById(R.id.editText2);
        three = (EditText) view.findViewById(R.id.editText3);
        four = (EditText) view.findViewById(R.id.editText4);
        five = (EditText) view.findViewById(R.id.editText5);
        six = (EditText) view.findViewById(R.id.editText6);
        one.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        six.setOnEditorActionListener(this);
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text back to activity through the implemented listener
            ReverbDialogListener listener = (ReverbDialogListener) getActivity();
            listener.onFinishEditDialog(one.getText().toString(), two.getText().toString(),
                    three.getText().toString(), four.getText().toString(), five.getText().toString(),
                    six.getText().toString());
            // Close the dialog and return back to the parent activity
            dismiss();
            return true;
        }
        return false;
    }
}
