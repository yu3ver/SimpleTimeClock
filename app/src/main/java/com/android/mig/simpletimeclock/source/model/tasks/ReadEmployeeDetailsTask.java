package com.android.mig.simpletimeclock.source.model.tasks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.android.mig.simpletimeclock.source.TimeClockContract;
import com.android.mig.simpletimeclock.source.TimeClockDbHelper;
import com.android.mig.simpletimeclock.source.model.EmployeeDetails;
import com.android.mig.simpletimeclock.source.model.EmployeeDetailsInteractor;
import com.android.mig.simpletimeclock.source.model.Timeclock;

import java.math.BigDecimal;
import java.util.ArrayList;


public class ReadEmployeeDetailsTask extends AsyncTask<Integer, Void, ReadEmployeeDetailsTask.ResultWrapper> {

    private final int PAID_STATUS = 1;
    private final int UNPAID_STATUS = 0;

    private final int ACTIVE_TIME_ID_INDEX = 0;
    private final int ACTIVE_CLOCKIN_INDEX = 1;
    private final int ACTIVE_WAGE_INDEX = 2;

    private final int BY_PAID_TIME_ID_INDEX = 0;
    private final int BY_PAID_CLOCKIN_INDEX = 1;
    private final int BY_PAID_CLOCKOUT_INDEX = 2;
    private final int BY_PAID_WAGE_INDEX = 3;

    private final int EMPLOYEES_NAME_INDEX = 0;
    private final int EMPLOYEES_WAGE_INDEX = 1;
    private final int EMPLOYEES_PHOTO_INDEX = 2;

    private final int BREAKS_START_INDEX = 0;
    private final int BREAKS_END_INDEX = 1;

    private final String ACTIVE_TIME_QUERY = "SELECT " +
            TimeClockContract.Timeclock.TIMECLOCK_ID + ", " +
            TimeClockContract.Timeclock.TIMECLOCK_CLOCK_IN + ", " +
            TimeClockContract.Timeclock.TIMECLOCK_WAGE + " FROM " +
            TimeClockContract.Timeclock.TABLE_TIMECLOCK + " WHERE " +
            TimeClockContract.Timeclock.TIMECLOCK_EMP_ID + " =? AND " +
            TimeClockContract.Timeclock.TIMECLOCK_CLOCK_OUT + " IS NULL";

    private final String BY_PAID_TIME_QUERY = "SELECT " +
            TimeClockContract.Timeclock.TIMECLOCK_ID + ", " +
            TimeClockContract.Timeclock.TIMECLOCK_CLOCK_IN + ", " +
            TimeClockContract.Timeclock.TIMECLOCK_CLOCK_OUT + ", " +
            TimeClockContract.Timeclock.TIMECLOCK_WAGE + " FROM " +
            TimeClockContract.Timeclock.TABLE_TIMECLOCK + " WHERE " +
            TimeClockContract.Timeclock.TIMECLOCK_EMP_ID + "=? AND " +
            TimeClockContract.Timeclock.TIMECLOCK_PAID + "=? AND " +
            TimeClockContract.Timeclock.TIMECLOCK_CLOCK_OUT + " IS NOT NULL ORDER BY " +
            TimeClockContract.Timeclock.TIMECLOCK_CLOCK_IN + " DESC";

    private final String EMPLOYEE_QUERY = "SELECT " +
            TimeClockContract.Employees.EMP_NAME + ", " +
            TimeClockContract.Employees.EMP_WAGE + ", " +
            TimeClockContract.Employees.EMP_PHOTO_PATH + " FROM " +
            TimeClockContract.Employees.TABLE_EMPLOYEES + " WHERE " +
            TimeClockContract.Employees.EMP_ID + " =?";

    private final String BREAKS_QUERY = "SELECT " +
            TimeClockContract.Breaks.TIMECLOCK_BREAK_START + ", " +
            TimeClockContract.Breaks.TIMECLOCK_BREAK_END + " FROM " +
            TimeClockContract.Breaks.TABLE_BREAKS + " WHERE " +
            TimeClockContract.Breaks.BREAK_TIMECLOCK_ID + "=?";

    private Context mContext;
    private EmployeeDetailsInteractor.OnFinishedTransactionListener mOnFinishedTransactionListener;

    public ReadEmployeeDetailsTask(Context context, EmployeeDetailsInteractor.OnFinishedTransactionListener onFinishedTransactionListener) {
        this.mContext = context;
        this.mOnFinishedTransactionListener = onFinishedTransactionListener;
    }

    @Override
    protected ResultWrapper doInBackground(Integer... params) {
        ArrayList<Timeclock> timeclockArrayList = new ArrayList<>();
        EmployeeDetails employeeDetails = null;
        boolean isWorking = false;
        TimeClockDbHelper mTimeClockDbHelper = new TimeClockDbHelper(mContext);
        final SQLiteDatabase db = mTimeClockDbHelper.getReadableDatabase();

        String empId = String.valueOf(params[0]);

        try {
            db.beginTransaction();

            Cursor currentCursor = db.rawQuery(ACTIVE_TIME_QUERY, new String[]{empId});
            long timeNow = (System.currentTimeMillis() / 1000);
            int currentTime = 0;
            double currentEarnings = 0;

            // calculates the earning of the current active time, if there is any
            if (currentCursor.moveToFirst()) {
                isWorking = true;

                int timeId = currentCursor.getInt(ACTIVE_TIME_ID_INDEX);
                long clockIn = currentCursor.getLong(ACTIVE_CLOCKIN_INDEX);
                double wage = currentCursor.getDouble(ACTIVE_WAGE_INDEX);

                Cursor breaksCursor = db.rawQuery(BREAKS_QUERY, new String[] {String.valueOf(timeId)});
                int currentBreakTime = calculateBreaks(breaksCursor, timeNow);
                currentTime = (int) (timeNow - clockIn - currentBreakTime);

                Log.d("READEMPLOYEEDETAILSTASK", "clock in = " + String.valueOf(currentCursor.getLong(ACTIVE_CLOCKIN_INDEX)));

                currentEarnings = calculateDecimalEarnings(currentTime, wage);

                // adds the first row of the work log list
                Timeclock timeclock = new Timeclock(timeId, clockIn, 0, currentTime, currentBreakTime, currentEarnings);
                timeclockArrayList.add(timeclock);
            }
            currentCursor.close();

            // calculate the earnings of unpaid times (active time not included)
            Cursor unpaidCursor = db.rawQuery(BY_PAID_TIME_QUERY, new String[]{empId, String.valueOf(UNPAID_STATUS)});
            long unpaidPreviousTime = 0;
            double totalUnpaidPreviousEarnings = 0;
            if (unpaidCursor.moveToFirst()) {
                int currentTimeWorked;
                do {
                    int timeId = unpaidCursor.getInt(BY_PAID_TIME_ID_INDEX);
                    long clockInTime = unpaidCursor.getLong(BY_PAID_CLOCKIN_INDEX);
                    long clockOutTime = unpaidCursor.getLong(BY_PAID_CLOCKOUT_INDEX);
                    double wage = unpaidCursor.getDouble(BY_PAID_WAGE_INDEX);
                    Cursor breaksCursor = db.rawQuery(BREAKS_QUERY, new String[]{String.valueOf(timeId)});
                    int unpaidBreakTime = calculateBreaks(breaksCursor, timeNow);

                    currentTimeWorked = (int) (clockOutTime - clockInTime - unpaidBreakTime);
                    double previousEarnings = calculateDecimalEarnings(currentTimeWorked, wage);

                    unpaidPreviousTime += currentTimeWorked;
                    totalUnpaidPreviousEarnings += previousEarnings;

                    // adds a new row to the work log list
                    Timeclock timeclock = new Timeclock(timeId, clockInTime, clockOutTime, currentTimeWorked, unpaidBreakTime, previousEarnings);
                    timeclockArrayList.add(timeclock);

                } while (unpaidCursor.moveToNext());
            }
            unpaidCursor.close();
            long totalUnpaidTime = currentTime + unpaidPreviousTime;
            double totalUnpaidEarnings = currentEarnings + totalUnpaidPreviousEarnings;

            // gets employee personal info
            Cursor employeeCursor = db.rawQuery(EMPLOYEE_QUERY, new String[]{empId});
            String empName = null;
            double empWage = 0;
            String empPhotoUri = null;
            if (employeeCursor.getCount() > 0) {
                employeeCursor.moveToFirst();
                empName = employeeCursor.getString(EMPLOYEES_NAME_INDEX);
                empWage = employeeCursor.getDouble(EMPLOYEES_WAGE_INDEX);
                empPhotoUri = employeeCursor.getString(EMPLOYEES_PHOTO_INDEX);
            }
            employeeCursor.close();

            employeeDetails = new EmployeeDetails(Integer.valueOf(empId), empName, empWage, empPhotoUri, totalUnpaidTime, totalUnpaidEarnings, isWorking);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.w("Exception: ", e);
        } finally {
            db.endTransaction();
        }
        ResultWrapper resultWrapper = new ResultWrapper(employeeDetails, timeclockArrayList);
        return resultWrapper;
    }

    @Override
    protected void onPostExecute(ResultWrapper resultWrapper) {
        if (resultWrapper.getEmployeeDetails() != null) {
            this.mOnFinishedTransactionListener.onReadSuccess(resultWrapper.getEmployeeDetails(), resultWrapper.getTimeclockArrayList());
        }
    }

    class ResultWrapper{

        private EmployeeDetails mEmployeeDetails;
        private ArrayList<Timeclock> mTimeclockArrayList;

        public ResultWrapper(EmployeeDetails employeeDetails, ArrayList<Timeclock> timeclockArrayList) {
            this.mEmployeeDetails = employeeDetails;
            this.mTimeclockArrayList = timeclockArrayList;
        }

        public EmployeeDetails getEmployeeDetails() {
            return mEmployeeDetails;
        }

        public ArrayList<Timeclock> getTimeclockArrayList() {
            return mTimeclockArrayList;
        }
    }

    private int calculateBreaks(Cursor breaksCursor, long timeNow){
        int breakSum = 0;
        if (breaksCursor.getCount() > 0){
            breaksCursor.moveToFirst();
            do {
                long breakStart = breaksCursor.getLong(BREAKS_START_INDEX);
                long breakEnd = breaksCursor.getLong(BREAKS_END_INDEX);
                if (breakEnd == 0) {
                    breakEnd = timeNow;
                }
                breakSum += (int) (breakEnd - breakStart);
            } while (breaksCursor.moveToNext());
        }
        breaksCursor.close();
        return breakSum;
    }

    private double calculateDecimalEarnings(int secondsWorked, double wage){
        double earned;
        int hours = secondsWorked / 3600;
        int minutes = (secondsWorked % 3600) / 60;
        double decimalMinutes = (double) minutes / 60;
        earned = (hours * wage) + (decimalMinutes * wage);
        BigDecimal earningsBigDecimal = new BigDecimal(String.valueOf(earned));                 // string on BigDecimal helps to preserve precision
        earningsBigDecimal = earningsBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        earned = earningsBigDecimal.doubleValue();
        return earned;
    }

}
