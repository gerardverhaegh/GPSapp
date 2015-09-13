package com.example.gveapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileBrowser extends ListActivity {
    private String path;
    private String m_sPath = null;
    private String m_sFilter = null;
    private int m_position = -1;
    private boolean m_bGetFileNameOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Get data via the key
            String Path = extras.getString("Path");
            if (Path != null) {
                m_sPath = Path;
            }

            String Filter = extras.getString("Filter");
            if (Filter != null) {
                m_sFilter = Filter;
            }

            Boolean bGetFileNameOnly = extras.getBoolean("GetFileNameOnly");
            if (bGetFileNameOnly != null) {
                m_bGetFileNameOnly = bGetFileNameOnly;
            }
        }

        // Use the current directory as title
        if (m_sPath != null) {
            path = m_sPath;
        } else {
            path = "/";
        }

        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
        }

        setTitle(path);

        // Read all files sorted into the values-array
        List<File> values = new ArrayList();
        File dir = new File(path);

        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }

        File[] list = dir.listFiles();

        if (list != null) {
            for (File file : list) {
                if (!file.getAbsoluteFile().toString().startsWith(".")) {
                    if (m_sFilter == null) {
                        if (new File(file.toString()).isDirectory()) {
                            // values.add(file);
                        } else {
                            values.add(file);
                        }
                    } else {
                        if (new File(file.toString()).isDirectory()) {
                            // values.add(file);
                        } else {
                            if (file.toString().contains(m_sFilter)) {
                                values.add(file);
                            }
                        }
                    }
                }
            }
        }
        // Collections.sort(values);

        Collections.sort(values, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(
                        f1.lastModified());
            }
        });

        // remove paths
        List<String> values2 = new ArrayList();
        for (File f : values) {
            values2.add(f.getName());
        }

        // Put the data into the list
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_2, android.R.id.text1,
                values2);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        m_position = position;

        // ShowYesNoDialog();
        if (m_bGetFileNameOnly) {
            // return filename
            Intent _result = new Intent();
            _result.putExtra("FILENAME", (String) getListAdapter().getItem(position));

            setResult(Activity.RESULT_OK, _result);
            finish();
        }
        else
        {
            /** Instantiating PopupMenu class */
            PopupMenu popup = new PopupMenu(this.getApplicationContext(), v);

            /** Adding menu items to the pop up menu */
            popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());

            /** Defining menu item click listener for the pop up menu */
            popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    // Toast.makeText(getBaseContext(), "You selected the action : "
                    // + item.getTitle(), Toast.LENGTH_SHORT).show();
                    switch (item.getItemId()) {
                        case R.id.action_show:
                            Show(m_position);
                            break;
                        case R.id.action_email:
                            Email(m_position);
                            break;
                        case R.id.action_delete:
                            Delete(m_position);
                            break;
                    }

				/*
                Thread closeActivity = new Thread(new Runnable() {
					  @Override
					  public void run() {
					    try {
					      Thread.sleep(500);
					      // Do some stuff
					    } catch (Exception e) {
					      e.getLocalizedMessage();
					    }
					  }
					});
					*/

                    return true;
                }
            });

            /** Showing the pop up menu */
            popup.show();
        }
    }

    private void Show(int position) {
        String filename = (String) getListAdapter().getItem(position);
        if (path.endsWith(File.separator)) {
            filename = path + filename;
        } else {
            filename = path + File.separator + filename;
        }

        if (new File(filename).isDirectory()) {
            Intent intent = new Intent(this, FileBrowser.class);
            intent.putExtra("path", filename);
            startActivity(intent);
        } else {
            // Toast.makeText(this, filename + " is not a directory",
            // Toast.LENGTH_LONG).show();

            Intent _result = new Intent();
            _result.putExtra("FILENAME", filename);

            setResult(Activity.RESULT_OK, _result);
            finish();
        }
    }

    private void Email(int position) {
        Toast.makeText(this, "Not yet implemented", Toast.LENGTH_LONG).show();
/*		String filename = (String) getListAdapter().getItem(position);
		if (path.endsWith(File.separator)) {
			filename = path + filename;
		} else {
			filename = path + File.separator + filename;
		}

		if (new File(filename).isDirectory()) {
			Intent intent = new Intent(this, FileBrowser.class);
			intent.putExtra("path", filename);
			startActivity(intent);
		} else {

			Intent _result = new Intent();
			_result.putExtra("MESSAGE", filename);

			setResult(Activity.RESULT_OK, _result);
			finish();
		}*/
    }

    private void Delete(int position) {
        String filename = (String) getListAdapter().getItem(position);
        DeleteFileDialog(filename);
    }

    private void DeleteFileDialog(String filename) {
        final String fn = m_sPath + File.separator + filename;

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        File f = new File(fn);

                        if (f.exists()) {
                            boolean rc = f.delete();

                            if (f.exists()) {
                                f.delete();
                            } else {
							/*
							Toast.makeText(getBaseContext(), "File still exists, rc: " + rc,
									Toast.LENGTH_SHORT).show();
									*/
                            }
                        } else {
                            Toast.makeText(getBaseContext(), "File does not exist",
                                    Toast.LENGTH_SHORT).show();
                        }

                        finish();
                        startActivity(getIntent());

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // nothing
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete " + filename + "?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}
