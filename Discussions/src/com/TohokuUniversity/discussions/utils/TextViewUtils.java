package com.TohokuUniversity.discussions.utils;

import android.widget.TextView;

public class TextViewUtils {

	public static String toString(final TextView editText) {

		return toString(editText, "");
	}

	public static String toString(final TextView editText, final String defaultValue) {

		if (editText == null) {
			return defaultValue;
		}
		CharSequence editable = editText.getText();
		if (editable == null) {
			return defaultValue;
		}
		return editable.toString();
	}
}
