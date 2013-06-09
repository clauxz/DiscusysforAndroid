package com.TohokuUniversity.discussions.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class CommentEditText extends EditText {

	public CommentEditText(final Context context) {

		super(context);
	}

	public CommentEditText(final Context context, final AttributeSet attrs) {

		super(context, attrs);
	}

	public CommentEditText(final Context context, final AttributeSet attrs, final int defStyle) {

		super(context, attrs, defStyle);
	}

	@Override
	public boolean onKeyPreIme(final int keyCode, final KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			clearFocus();
		}
		return super.onKeyPreIme(keyCode, event);
	}
}
