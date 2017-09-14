package com.android.mig.simpletimeclock.view.fragments;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.mig.simpletimeclock.R;
import com.android.mig.simpletimeclock.presenter.AllEmployeesPresenter;
import com.android.mig.simpletimeclock.presenter.AllEmployeesPresenterImpl;
import com.android.mig.simpletimeclock.view.AllEmployeesView;
import com.android.mig.simpletimeclock.view.adapters.AllEmployeesAdapter;

public class AllEmployeesFragment extends Fragment
        implements AllEmployeesView, AllEmployeesAdapter.OnTapHandler{

    boolean actionMode = false;
    TextView mCounterTextView;
    AllEmployeesPresenter mAllEmployeesPresenter;
    FloatingActionButton mFabSetActiveEmployee;
    RecyclerView mAllEmployeesRecyclerView;
    View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_all_employees , container, false);

        mCounterTextView = rootView.findViewById(R.id.counter_text_view);
        mCounterTextView.setVisibility(View.GONE);

        mFabSetActiveEmployee = rootView.findViewById(R.id.fab_set_active_employee);
        mFabSetActiveEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllEmployeesAdapter allEmployeesAdapter = (AllEmployeesAdapter) mAllEmployeesRecyclerView.getAdapter();
                mAllEmployeesPresenter.onActionDoneClicked(allEmployeesAdapter.getEmployeesIds(), true);
            }
        });

        mAllEmployeesRecyclerView = rootView.findViewById(R.id.all_employees_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mAllEmployeesRecyclerView.setLayoutManager(layoutManager);
        mAllEmployeesRecyclerView.hasFixedSize();
        AllEmployeesAdapter mAllEmployeeAdapter = new AllEmployeesAdapter(this);
        mAllEmployeesRecyclerView.setAdapter(mAllEmployeeAdapter);

        mAllEmployeesPresenter = new AllEmployeesPresenterImpl(this, getActivity());
        mAllEmployeesPresenter.onResume();

        return rootView;
    }

    /** {@inheritDoc} */
    @Override
    public void showAllEmployees(Cursor employeesCursor) {
        AllEmployeesAdapter allEmployeesAdapter = (AllEmployeesAdapter) mAllEmployeesRecyclerView.getAdapter();
        allEmployeesAdapter.setAllEmployeesData(employeesCursor);
    }

    /** {@inheritDoc} */
    @Override
    public void showStatusUpdateMessage() {
        Snackbar mySnackbar = Snackbar.make(
                rootView,
                R.string.status_update_message,
                Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    /**
     * Method to be called in parent activity to pass the values of
     * the new employee that was added.
     *
     * @param name name of employee
     * @param wage wage of employee
     */
    public void setNewEmployeeData(String name, double wage){
        Log.d("passed" , name + " " + wage);
        mAllEmployeesPresenter.onActionAddClicked(name, wage);
    }

    @Override
    public void onTap(boolean actionMode) {
        this.actionMode = actionMode;
        if (this.actionMode){
            mFabSetActiveEmployee.setVisibility(View.VISIBLE);
        } else {
            mFabSetActiveEmployee.setVisibility(View.INVISIBLE);
        }
    }
}
