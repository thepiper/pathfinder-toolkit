package com.lateensoft.pathfinder.toolkit.views;

import com.lateensoft.pathfinder.toolkit.R;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import roboguice.activity.RoboActivity;

/**
 * Base class for any activity which can edit a Parcelable, and return it as a result.
 * 
 * The base does not hold any reference to the Parcelable itself, but requires implementations
 * to provide a reference to one.
 * 
 * @author trevsiemens
 *
 */
public abstract class ParcelableEditorActivity extends RoboActivity {
    
    private static final String TAG = ParcelableEditorActivity.class.getSimpleName();
    
    /**
     * Returned as a result code if the item originally passed should be deleted
     */
    public static final int RESULT_DELETE = RESULT_FIRST_USER;
    
    /**
     * Key to be used when adding extra Parcelable to the activity's intent
     * Also used as key for extra Parcelable when returning data
     */
    public static final String INTENT_EXTRAS_KEY_EDITABLE_PARCELABLE = "parcelable_to_edit";

    public static final int DEFAULT_REQUEST_CODE = 14692;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        setParcelableToEdit(getParcelableFromIntent(getIntent()));
        
        setupContentView();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.base_editor_menu, menu);

        menu.findItem(R.id.mi_delete).setVisible(isParcelableDeletable());

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mi_done) {
            try {
                updateEditedParcelableValues();
                Log.v(TAG, "Add/Edit done");
                Intent resultData = new Intent();
                resultData.putExtra(INTENT_EXTRAS_KEY_EDITABLE_PARCELABLE, getEditedParcelable());
                setResult(RESULT_OK, resultData);
                finish();
            } catch (InvalidValueException e) {
                showErrorDialog(e.getMessage());
            }
        } else if (item.getItemId() == R.id.mi_cancel || 
                item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (item.getItemId() == R.id.mi_delete) {
            showDeleteConfirmationDialog();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
    
    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error_alert_title);
        builder.setMessage(message);
        builder.setNeutralButton(R.string.ok_button_text, null);
        builder.show();
    }
    
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_alert_title);
        builder.setMessage(R.string.delete_alert_message);
        OnClickListener ocl = new OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    setResult(RESULT_DELETE);
                    finish();
                }
            }
        };
        builder.setPositiveButton(R.string.cancel_button_text, ocl);
        builder.setNegativeButton(R.string.ok_button_text, ocl);
        builder.show();
    }
    
    protected void setupSpinner(Spinner spinner, int optionResourceId, int defaultSelection,
            OnTouchListener touchListener) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                optionResourceId, android.R.layout.simple_spinner_item);

        setupSpinner(spinner, adapter, defaultSelection, touchListener);
    }

    protected void setupSpinner(Spinner spinner, String[] options, int defaultSelection,
                                OnTouchListener touchListener) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options);

        setupSpinner(spinner, adapter, defaultSelection, touchListener);
    }

    protected void setupSpinner(Spinner spinner, ArrayAdapter adapter, int defaultSelection,
                                OnTouchListener touchListener) {
        adapter.setDropDownViewResource(R.layout.spinner_centered_wrapped);
        spinner.setGravity(Gravity.CENTER);
        spinner.setAdapter(adapter);
        spinner.setOnTouchListener(touchListener);
        spinner.setSelection(defaultSelection, false);
    }
    
    /**
     * Called by the ParcelableEditorActivity
     * Must perform all view setup, including calling setContentView
     */
    protected abstract void setupContentView();
    
    /**
     * Called by the ParcelableEditorActivity when the done button is pressed.
     * 
     * This should update the values of the parcelable object (retrieved with getEditedParcelable) 
     * from the current values shown by the UI
     * 
     * @throws InvalidValueException - This should occur if the update failed,
     *  and the ParcelableEditorActivity should not finish yet. The message given in the exception
     *  will be shown as an error dialog
     */
    protected abstract void updateEditedParcelableValues() throws InvalidValueException;
    
    /**
     * @return A direct reference to the parcelable object which is being edited. 
     * The object provided by setParcelableToEdit, or a copy, should be returned if called just before. 
     */
    protected abstract Parcelable getEditedParcelable();
    
    /**
     * Allow the ParcelableEditorActivity to set the value of
     * the Parcelable to be edited. This will only and always be called once onCreate.
     * @param p
     */
    protected abstract void setParcelableToEdit(Parcelable p);
    
    /**
     * @return True if when the activity can return RESULT_DELETE as a result code, and show a delete button. False otherwise.
     */
    protected abstract boolean isParcelableDeletable();

    /**
     * Convenience method for safely extracting the edited parcelable from a returned intent
     * @return parcelable contained in extras of the intent
     */
    public static <T extends Parcelable> T getParcelableFromIntent(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            return extras.getParcelable(INTENT_EXTRAS_KEY_EDITABLE_PARCELABLE);
        }
        return null;
    }
    
    
    public static class InvalidValueException extends Exception {

        private static final long serialVersionUID = 5193787377071717441L;
        
        public InvalidValueException() {
            super("Invalid value");
        }
        
        public InvalidValueException(String message) {
            super(message);
        }
    }
}
