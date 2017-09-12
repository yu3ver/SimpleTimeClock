package com.android.mig.simpletimeclock.view.activities;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.android.mig.simpletimeclock.R;
import com.android.mig.simpletimeclock.view.fragments.AddEmployeeDialogFragment;
import com.android.mig.simpletimeclock.view.fragments.AllEmployeesFragment;

public class AllEmployeesActivity extends AppCompatActivity implements AddEmployeeDialogFragment.NoticeDialogListener{

    private final static String DIALOG_TAG = AddEmployeeDialogFragment.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_employees);
        Toolbar mContextualToolbar = (Toolbar) findViewById(R.id.action_context_toolbar);
        setSupportActionBar(mContextualToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_all_employees_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_add:
                FragmentManager fragmentManager = getFragmentManager();
                DialogFragment dialogFragment = new AddEmployeeDialogFragment();
                dialogFragment.show(fragmentManager, DIALOG_TAG);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Required method that receives callbacks from the DialogFragment
     * to retrieve the name of wage of the new employee added.
     *
     * @param dialog the dialog window object
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Dialog dialogView = dialog.getDialog();
        EditText nameEditText = (EditText) dialogView.findViewById(R.id.name_edit_text);
        EditText wageEditText = (EditText) dialogView.findViewById(R.id.wage_edit_text);

        String name = String.valueOf(nameEditText.getText());
        double wage = Double.parseDouble(String.valueOf(wageEditText.getText()));

        AllEmployeesFragment fragment = (AllEmployeesFragment) getFragmentManager().findFragmentById(R.id.all_employees_fragment);
        fragment.setNewEmployeeData(name, wage);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // do nothing
    }
}