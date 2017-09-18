package com.android.mig.simpletimeclock.source.model;

import android.content.ContentValues;
import android.content.Context;
import com.android.mig.simpletimeclock.source.TimeClockContract.Employees;
import com.android.mig.simpletimeclock.source.model.tasks.DeleteEmployeeTask;
import com.android.mig.simpletimeclock.source.model.tasks.InsertEmployeeTask;
import com.android.mig.simpletimeclock.source.model.tasks.InsertTimeTask;
import com.android.mig.simpletimeclock.source.model.tasks.ReadEmployeesTask;
import com.android.mig.simpletimeclock.source.model.tasks.UpdateStatusTask;

public class EmployeesInteractorImpl implements EmployeesInteractor{

    private static int ACTIVE_STATUS = 1;
    private static int INACTIVE_STATUS = 0;

    private Context mContext;

    public EmployeesInteractorImpl(Context context) {
        this.mContext = context;
    }

    /** {@inheritDoc} */
    @Override
    public void insertEmployee(String name, double wage, OnFinishedTransactionListener onFinishedTransactionListener) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Employees.EMP_NAME, name);
        contentValues.put(Employees.EMP_STATUS, INACTIVE_STATUS);
        contentValues.put(Employees.EMP_WAGE, wage);

        InsertEmployeeTask insertTask = new InsertEmployeeTask(mContext, onFinishedTransactionListener);
        insertTask.execute(contentValues);
    }

    /** {@inheritDoc} */
    @Override
    public void updateEmployeeStatus(Integer[] ids, boolean isActive, OnFinishedTransactionListener onFinishedTransactionListeners) {
        int mStatus = INACTIVE_STATUS;
        if (isActive){
            mStatus = ACTIVE_STATUS;
        }
        UpdateStatusTask updateStatusTask = new UpdateStatusTask(mContext, onFinishedTransactionListeners);
        updateStatusTask.execute(ids, mStatus);
        InsertTimeTask insertTimeTask = new InsertTimeTask(mContext, onFinishedTransactionListeners);
        insertTimeTask.execute(ids);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteEmployee(Integer[] ids, OnFinishedTransactionListener onFinishedTransactionListener) {
        DeleteEmployeeTask deleteTask = new DeleteEmployeeTask(mContext, onFinishedTransactionListener);
        deleteTask.execute(ids);
    }

    /** {@inheritDoc} */
    @Override
    public void readEmployees(OnFinishedTransactionListener onFinishedTransactionListeners) {
        ReadEmployeesTask readEmployeesTask = new ReadEmployeesTask(mContext, onFinishedTransactionListeners);
        readEmployeesTask.execute(null, null);
    }
}
