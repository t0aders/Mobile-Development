package exam.travelnotesmanager.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import exam.travelnotesmanager.database.NoteDatabaseHelper;
import exam.travelnotesmanager.database.NoteTable;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class NoteContentProvider extends ContentProvider {

	// database
	private NoteDatabaseHelper database;

	// Used for the UriMacher
	private static final int NOTES = 10;
	private static final int NOTE_ID = 20;

	private static final String AUTHORITY = "exam.travelnotesmanager.contentprovider";

	private static final String BASE_PATH = "notes";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/notes";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/note";

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, NOTES);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", NOTE_ID);
	}

	@Override
	public boolean onCreate() {
		database = new NoteDatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		checkColumns(projection);

		// Set the table
		queryBuilder.setTables(NoteTable.TABLE_NOTE);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case NOTES:
			break;
		case NOTE_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(NoteTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;

	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		long id = 0;
		switch (uriType) {
		case NOTES:
			id = sqlDB.insert(NoteTable.TABLE_NOTE, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case NOTES:
			rowsDeleted = sqlDB.delete(NoteTable.TABLE_NOTE, selection,
					selectionArgs);
			break;
		case NOTE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(NoteTable.TABLE_NOTE,
						NoteTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete(NoteTable.TABLE_NOTE,
						NoteTable.COLUMN_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case NOTES:
			rowsUpdated = sqlDB.update(NoteTable.TABLE_NOTE, values, selection,
					selectionArgs);
			break;
		case NOTE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(NoteTable.TABLE_NOTE, values,
						NoteTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(NoteTable.TABLE_NOTE, values,
						NoteTable.COLUMN_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		String[] available = { NoteTable.COLUMN_TITLE,
				NoteTable.COLUMN_ADDRESS, NoteTable.COLUMN_DESCRIPTION,
				NoteTable.COLUMN_VISITDATE, NoteTable.COLUMN_VISITAGAIN,
				NoteTable.COLUMN_ID };
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}
}
