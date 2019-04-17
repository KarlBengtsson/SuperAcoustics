package com.example.SuperAcoustics;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    private Button newButton, loadButton;
    private String path;
    private int fromCheck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);

        newButton = (Button) findViewById(R.id.newButton);
        loadButton = (Button) findViewById(R.id.loadButton);

        Button.OnClickListener newListener =
                new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Dismiss this dialog.
                        Intent intent;
                        intent = new Intent(WelcomeActivity.this,FileName.class);
                        startActivity(intent);
                    }
                };
        newButton.setOnClickListener(newListener);


        Button.OnClickListener loadListener =
                new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Dismiss this dialog.
                        Intent intent = null;
                        if (VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(Intent.createChooser(intent,"Choose Directory:"), 9999);
                        }
                    }
                };
        loadButton.setOnClickListener(loadListener);
    }

    public void getInfo (View view) {
        FragmentManager fm = getSupportFragmentManager();
        InfoFragment infoFragment = InfoFragment.newInstance("InfoFragment");
        infoFragment.show(fm, "fragment_info");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 9999:
                Uri uri = data.getData();
                Uri docUri = null;
                if (VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                            DocumentsContract.getTreeDocumentId(uri));
                    path = getPath(this, docUri);
                    Log.i("Test", "Result Path: " + path);
                    fromCheck = 1;
                    setPreferences();
                    Intent intent;
                    intent = new Intent(WelcomeActivity.this,ViewResult.class);
                    startActivity(intent);
                }
                break;
        }
    }

    private void setPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("loadpath", path);
        editor.putInt("fromCheck", fromCheck);
        editor.apply();
    }











    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id;
                    if (VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        id = DocumentsContract.getDocumentId(uri);
                        final Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                        return getDataColumn(context, contentUri, null, null);
                    }
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId;
                    if (VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        docId = DocumentsContract.getDocumentId(uri);
                        final String[] split = docId.split(":");
                        final String type = split[0];
                        Uri contentUri = null;
                        if ("image".equals(type)) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if ("video".equals(type)) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else if ("audio".equals(type)) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }

                        final String selection = "_id=?";
                        final String[] selectionArgs = new String[] {
                                split[1]
                        };

                        return getDataColumn(context, contentUri, selection, selectionArgs);
                    }



                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {

                // Return the remote address
                if (isGooglePhotosUri(uri))
                    return uri.getLastPathSegment();

                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}
