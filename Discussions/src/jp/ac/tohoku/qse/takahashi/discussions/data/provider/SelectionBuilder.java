package jp.ac.tohoku.qse.takahashi.discussions.data.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Helper for building selection clauses for {@link SQLiteDatabase}. Each appended clause is combined using
 * {@code AND}. This class is <em>not</em> thread safe. */
public class SelectionBuilder {

	private static final boolean LOGV = false;
	private static final String TAG = SelectionBuilder.class.getSimpleName();
	private final Map<String, String> mProjectionMap = new HashMap<String, String>();
	private final StringBuilder mSelection = new StringBuilder();
	private final ArrayList<String> mSelectionArgs = new ArrayList<String>();
	private String mTable = null;

	/** Execute delete using the current internal state as {@code WHERE} clause. */
	/** @param db
	 *            {@link SQLiteDatabase} to operate with
	 * @return the number of rows affected if a whereClause is passed in, 0 otherwise. To remove all rows and
	 *         get a count set "1" as the whereClause. */
	public int delete(final SQLiteDatabase db) {

		assertTable();
		if (LOGV) {
			Log.v(TAG, "delete() " + this);
		}
		return db.delete(mTable, getSelection(), getSelectionArgs());
	}

	/** Return selection string for current internal state.
	 * 
	 * @return selection as string
	 * @see #getSelectionArgs() */
	public String getSelection() {

		return mSelection.toString();
	}

	/** Return selection arguments for current internal state.
	 * 
	 * @return an array of the String elements from this SelectionArgs.
	 * 
	 * @see #getSelection() */
	public String[] getSelectionArgs() {

		return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
	}

	/** Maps columns in kind of " toClause AS fromColumn "
	 * 
	 * @param fromColumn
	 *            desired column name
	 * @param toClause
	 *            database original column name
	 * @return this instance of SelectionBuuilder */
	public SelectionBuilder map(final String fromColumn, final String toClause) {

		mProjectionMap.put(fromColumn, toClause + " AS " + fromColumn);
		return this;
	}

	/** Puts to projection map next string " table.column ".
	 * 
	 * @param column
	 *            The column name
	 * @param table
	 *            The table name
	 * @return This selection builder with added map column to "table.column" */
	public SelectionBuilder mapToTable(final String column, final String table) {

		mProjectionMap.put(column, table + "." + column);
		return this;
	}

	/** Execute query using the current internal state as {@code WHERE} clause. */
	/** @param db
	 *            {@link SQLiteDatabase} to operate with
	 * @param columns
	 *            A list of which columns to return. Passing null will return all columns, which is
	 *            discouraged to prevent reading data from storage that isn't going to be used.
	 * @param orderBy
	 *            How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
	 *            Passing null will use the default sort order, which may be unordered.
	 * @return A {@link Cursor} object, which is positioned before the first entry. Note that Cursors are not
	 *         synchronized, see the documentation for more details. */
	public Cursor query(final SQLiteDatabase db, final String[] columns, final String orderBy) {

		return query(db, columns, null, null, orderBy, null);
	}

	/** Execute query using the current internal state as {@code WHERE} clause. */
	/** @param db
	 *            {@link SQLiteDatabase} to operate with
	 * @param columns
	 *            A list of which columns to return. Passing null will return all columns, which is
	 *            discouraged to prevent reading data from storage that isn't going to be used.
	 * @param groupBy
	 *            A filter declaring how to group rows, formatted as an SQL GROUP BY clause (excluding the
	 *            GROUP BY itself). Passing null will cause the rows to not be grouped.
	 * @param having
	 *            A filter declare which row groups to include in the cursor, if row grouping is being used,
	 *            formatted as an SQL HAVING clause (excluding the HAVING itself). Passing null will cause all
	 *            row groups to be included, and is required when row grouping is not being used.
	 * @param orderBy
	 *            How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
	 *            Passing null will use the default sort order, which may be unordered.
	 * @param limit
	 *            Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null
	 *            denotes no LIMIT clause.
	 * @return A {@link Cursor} object, which is positioned before the first entry. Note that Cursors are not
	 *         synchronized, see the documentation for more details. */
	public Cursor query(final SQLiteDatabase db, final String[] columns, final String groupBy,
			final String having, final String orderBy, final String limit) {

		assertTable();
		if (columns != null) {
			mapColumns(columns);
		}
		if (LOGV) {
			Log.v(TAG, "query(columns=" + Arrays.toString(columns) + ") " + this);
		}
		
		return db.query(mTable, columns, getSelection(), getSelectionArgs(), groupBy, having, orderBy, limit);
	}

	/** Reset any internal state, allowing this builder to be recycled. */
	/** @return this instance of SelectionBuilder */
	public SelectionBuilder reset() {

		mTable = null;
		mSelection.setLength(0);
		mSelectionArgs.clear();
		return this;
	}

	/** @param table
	 *            The table name to compile the query/update/delete/insert against.
	 * @return this instance of SelectionBuilder */
	public SelectionBuilder table(final String table) {

		mTable = table;
		return this;
	}

	@Override
	public String toString() {

		return "SelectionBuilder[table=" + mTable + ", selection=" + getSelection() + ", selectionArgs="
				+ Arrays.toString(getSelectionArgs()) + "]";
	}

	/** Execute update using the current internal state as {@code WHERE} clause. */
	/** @param db
	 *            {@link SQLiteDatabase} to operate with
	 * @param values
	 *            a map from column names to new column values. null is a valid value that will be translated
	 *            to NULL.
	 * @return the number of rows affected */
	public int update(final SQLiteDatabase db, final ContentValues values) {

		assertTable();
		if (LOGV) {
			Log.v(TAG, "update() " + this);
		}
		return db.update(mTable, values, getSelection(), getSelectionArgs());
	}

	/** Append the given selection clause to the internal state. Each clause is surrounded with parenthesis and
	 * combined using {@code AND}. */
	/** @param selection
	 *            A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the
	 *            WHERE itself). Passing null will return all rows for the given table.
	 * @param selectionArgs
	 *            You may include ?s in selection, which will be replaced by the values from selectionArgs, in
	 *            order that they appear in the selection. The values will be bound as Strings.
	 * @return this instance of SelectionBuilder */
	public SelectionBuilder where(final String selection, final String... selectionArgs) {

		if (TextUtils.isEmpty(selection)) {
			if ((selectionArgs != null) && (selectionArgs.length > 0)) {
				throw new IllegalArgumentException("Valid selection required when including arguments=");
			}
			// Shortcut when clause is empty
			return this;
		}
		if (mSelection.length() > 0) {
			mSelection.append(" AND ");
		}
		mSelection.append("(").append(selection).append(")");
		if (selectionArgs != null) {
			for (String arg : selectionArgs) {
				mSelectionArgs.add(arg);
			}
		}
		return this;
	}

	private void assertTable() {

		if (mTable == null) {
			throw new IllegalStateException("Table not specified");
		}
	}

	private void mapColumns(final String[] columns) {

		for (int i = 0; i < columns.length; i++) {
			final String target = mProjectionMap.get(columns[i]);
			if (target != null) {
				columns[i] = target;
			}
		}
	}
}
