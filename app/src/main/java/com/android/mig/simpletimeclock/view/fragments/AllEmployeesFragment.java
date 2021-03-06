package com.android.mig.simpletimeclock.view.fragments;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.mig.simpletimeclock.R;
import com.android.mig.simpletimeclock.presenter.AllEmployeesPresenter;
import com.android.mig.simpletimeclock.presenter.AllEmployeesPresenterImpl;
import com.android.mig.simpletimeclock.view.AllEmployeesView;
import com.android.mig.simpletimeclock.view.activities.EmployeeDetailsActivity;
import com.android.mig.simpletimeclock.view.adapters.AllEmployeesAdapter;
import com.android.mig.simpletimeclock.view.adapters.EmployeeAdapter;

public class AllEmployeesFragment extends Fragment
        implements AllEmployeesView, AllEmployeesAdapter.OnListTapHandler{

    boolean actionMode = false;
    private AllEmployeesPresenter mAllEmployeesPresenter;
    private FloatingActionButton mFabSetActiveEmployee;
    private RecyclerView mAllEmployeesRecyclerView;
    private TextView mEmptyListMessageTextView;
    private View rootView;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallbacks = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = true;
            mActionMode = mode;
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.menu_all_employees_action_mode, menu);
            showFabDoneButton();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_item_delete){
                AllEmployeesAdapter allEmployeesAdapter = (AllEmployeesAdapter) mAllEmployeesRecyclerView.getAdapter();
                mAllEmployeesPresenter.onActionDeleteClicked(allEmployeesAdapter.getEmployeesIds());
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            resetScreen();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_all_employees , container, false);

        mFabSetActiveEmployee = rootView.findViewById(R.id.fab_set_active_employee);
        mFabSetActiveEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllEmployeesAdapter allEmployeesAdapter = (AllEmployeesAdapter) mAllEmployeesRecyclerView.getAdapter();
                mAllEmployeesPresenter.onActionDoneClicked(allEmployeesAdapter.getEmployeesIds());
            }
        });

        mEmptyListMessageTextView = rootView.findViewById(R.id.all_employees_list_text_view);
        mAllEmployeesRecyclerView = rootView.findViewById(R.id.all_employees_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mAllEmployeesRecyclerView.setLayoutManager(layoutManager);
        mAllEmployeesRecyclerView.hasFixedSize();
        AllEmployeesAdapter mAllEmployeeAdapter = new AllEmployeesAdapter(getActivity(), this);
        mAllEmployeesRecyclerView.setAdapter(mAllEmployeeAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAllEmployeesPresenter = new AllEmployeesPresenterImpl(this, getActivity());
        mAllEmployeesPresenter.onResume();
    }

    /** {@inheritDoc} */
    @Override
    public void showAllEmployees(Cursor employeesCursor) {
        if (employeesCursor.getCount() == 0){
            mEmptyListMessageTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyListMessageTextView.setVisibility(View.INVISIBLE);
        }
        AllEmployeesAdapter allEmployeesAdapter = (AllEmployeesAdapter) mAllEmployeesRecyclerView.getAdapter();
        allEmployeesAdapter.setAllEmployeesData(employeesCursor);
    }

    /** {@inheritDoc} */
    @Override
    public void showStatusUpdateMessage() {
        Snackbar snackbar = Snackbar.make(
                rootView,
                R.string.status_update_message,
                Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    /** {@inheritDoc} */
    @Override
    public void showSuccessDeleteMessage() {
        Snackbar snackbar = Snackbar.make(
                rootView,
                R.string.success_delete_message,
                Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    /** {@inheritDoc} */
    @Override
    public void showPartialDeleteMessage() {
        Snackbar snackbar = Snackbar.make(
                rootView,
                R.string.partial_delete_message,
                Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    /** {@inheritDoc} */
    @Override
    public void showFailedDeleteMessage() {
        Snackbar snackbar = Snackbar.make(
                rootView,
                R.string.failed_delete_message,
                Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    /** {@inheritDoc} */
    @Override
    public void showFabDoneButton() {
        mFabSetActiveEmployee.setVisibility(View.VISIBLE);
    }

    /** {@inheritDoc} */
    @Override
    public void hideFabDoneButton() {
        mFabSetActiveEmployee.setVisibility(View.GONE);
    }

    /** {@inheritDoc} */
    @Override
    public void resetScreen() {
        if (mActionMode != null){
            mActionMode.finish();
        }
        actionMode = false;
        mActionMode = null;
        AllEmployeesAdapter allEmployeesAdapter = (AllEmployeesAdapter) mAllEmployeesRecyclerView.getAdapter();
        allEmployeesAdapter.clearSelection();
        hideFabDoneButton();
        mAllEmployeesPresenter.onResume();
    }

    /**
     * Method to be called in parent activity to pass the values of
     * the new employee that was added.
     *
     * @param name name of employee
     * @param wage wage of employee
     */
    public void setNewEmployeeData(String name, double wage, @Nullable String photoUri){
        mAllEmployeesPresenter.onActionAddClicked(name, wage, photoUri);
    }

    /** {@inheritDoc} */
    @Override
    public void onItemLongTap() {
        if (!actionMode){
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.startSupportActionMode(mActionModeCallbacks);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onLastSelectionItemRemoved() {
        resetScreen();
    }

    @Override
    public void onClick(int empId, View photoImageView) {

        Bundle bundle = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptions
                    .makeSceneTransitionAnimation(getActivity(), photoImageView, photoImageView.getTransitionName())
                    .toBundle();
        }
        Intent intent = new Intent(getActivity(), EmployeeDetailsActivity.class);
        intent.putExtra(Intent.EXTRA_UID, empId);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent, bundle);
    }
}
