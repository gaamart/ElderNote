package com.applications.guilhermeaugusto.eldernote.Extended;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by guilhermeaugusto on 14/08/2014.
 */
public class ExtendedEditText extends EditText {

    public interface KeyPreImeListener { public void onKeyPreImeAccured(); }
    private KeyPreImeListener eventListener;

    public ExtendedEditText(Context context) {
        super(context);
    }

    public ExtendedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ExtendedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEventListener(KeyPreImeListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            dispatchKeyEvent(event);
            if (this.eventListener != null) {
                this.eventListener.onKeyPreImeAccured();
            }
            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
