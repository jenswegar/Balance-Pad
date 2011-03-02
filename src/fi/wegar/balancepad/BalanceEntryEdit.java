package fi.wegar.balancepad;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

public class BalanceEntryEdit extends Activity {

	private BalanceDbAdapter mDbHelper;
	private EditText mDescriptionText;
	private EditText mDateText;
	private EditText mAmountText;
	private Spinner mAmountCurrency;
	
	private int mYear;
	private int mMonth;
	private int mDay;
	
	private Boolean doSaveState;
	
	private Long mRowId;
	
	static final int DATE_DIALOG_ID = 2;
	
	// the callback received when the user "sets" the date in the dialog
	private DatePickerDialog.OnDateSetListener mDateSetListener =
				new DatePickerDialog.OnDateSetListener() {

					public void onDateSet(DatePicker view, int year, 
								int monthOfYear, int dayOfMonth) {
								mYear = year;
								mMonth = monthOfYear;
								mDay = dayOfMonth;
								updateDateText();
					}
				};	
	
	/**
	 * creates the view and populates the fields with default values. 
	 * 
	 * First method to be called during life-cycle
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance_entry_edit);
        setTitle(R.string.edit_entry);
        
        mDbHelper = new BalanceDbAdapter(this);
        mDbHelper.open();

        mDescriptionText = (EditText) findViewById(R.id.description);
        mDateText = (EditText) findViewById(R.id.event_date);
        mAmountText = (EditText) findViewById(R.id.amount);
        mAmountCurrency = (Spinner) findViewById(R.id.currency);
        
        // populate the currency spinner with values
        Spinner spinner = (Spinner) findViewById(R.id.currency);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        		this, R.array.currencies_array, android.R.layout.simple_spinner_item);
        		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        		spinner.setAdapter(adapter);        

        Button addButton = (Button) findViewById(R.id.save);

        mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(BalanceDbAdapter.KEY_ROWID);
    	if (mRowId == null) {
    		Bundle extras = getIntent().getExtras();
    		mRowId = extras != null ? extras.getLong(BalanceDbAdapter.KEY_ROWID) : null;
    	}
    	
    	populateFields();
    	
        addButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
        
        
        Button cancelButton = (Button) findViewById(R.id.cancel);
        
        cancelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				doSaveState = false;
				
				setResult(RESULT_CANCELED);
				finish();
			}
		});
        
        // add a click listener to the date, make sure soft keyboard is not shown
        mDateText.setInputType(InputType.TYPE_NULL);
        mDateText.setOnClickListener( new View.OnClickListener() {
	        		public void onClick(View v) {
	        			showDialog(DATE_DIALOG_ID);
        		}
        	});        
        
    }
    
    private void updateDateText()
    {
    	mDateText.setText(new StringBuilder()
							.append(mYear).append("-")
							.append(mMonth + 1).append("-")							
							.append(mDay).append(" ") ); 	
    }
    
    /**
     * Adds default values to the fields, or if we're editing an old entry, the values from the database
     * 
     */
    private void populateFields()
    {
    	if(mRowId != null)
    	{
    		// We're editing an exising entry, so fetch the data and populate fields using it
    		Cursor entry = mDbHelper.fetchEntry(mRowId);
    		startManagingCursor(entry);
    		
    		long entryDate = entry.getLong( entry.getColumnIndexOrThrow(BalanceDbAdapter.KEY_EVENT_DATE) );
    		
    		Calendar c = Calendar.getInstance();
    		c.setTimeInMillis(entryDate);
    	
    		mYear = c.get(Calendar.YEAR);
    		mMonth = c.get(Calendar.MONTH);
    		mDay = c.get(Calendar.DATE);
    		
    		
    		mDescriptionText.setText( entry.getString( entry.getColumnIndexOrThrow( BalanceDbAdapter.KEY_DESCRIPTION) ) );
    		mAmountText.setText( entry.getString( entry.getColumnIndexOrThrow( BalanceDbAdapter.KEY_AMOUNT) ) );
    		
    		String currency = entry.getString( entry.getColumnIndexOrThrow( BalanceDbAdapter.KEY_CURRENCY) ); 
    		
    		setCurrencySelection(currency);

    	}
    	else
    	{
    		// this is a new entry, so use default values
    		Calendar c = Calendar.getInstance();
    		mYear = c.get(Calendar.YEAR);
    		mMonth = c.get(Calendar.MONTH);
    		mDay = c.get(Calendar.DATE);
    		
    		
    		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
    		
    		String code = settings.getString(Preferences.KEY_DEFAULT_CURRENCY_PREFERENCE, "EUR");
    			
    		setCurrencySelection(code);
    	}
    	
    	updateDateText();
    }
    
    /**
     * Sets the selection of the currency spinner to the value defined in code
     * @param code The Currency code that the spinner should be set to
     */
    private void setCurrencySelection(String code) {
		ArrayAdapter<String> spAdapter = (ArrayAdapter<String>) mAmountCurrency.getAdapter();
		int itemPos = spAdapter.getPosition( code );
		mAmountCurrency.setSelection(itemPos);;
    }
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    saveState();
	    outState.putSerializable(BalanceDbAdapter.KEY_ROWID, mRowId);
    }    
    
    
    /**
     * Second method to be called during life cycle
     */
    @Override
    protected void onStart()
    {
    	super.onStart();
    	doSaveState = true;
    }
    
    /**
     * Third method to be called. The activity can loop between this and onPause if the Activity ends up in the background
     */
    @Override
    protected void onResume() {
    	super.onResume();
    	// open the database connection
    	//mDbHelper.open();
    	populateFields();
    }
    
    
    /**
     * fourth method to be called. First to be called when exiting activity. 
     * The activity can loop between this and onResume if the Activity ends up in the background
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	saveState();
    	// close database connection
    	//mDbHelper.close();
    }
    
    private void saveState() {
    	
    	// save the state if the user hasn't clicked cancel explicitly
    	if(doSaveState)
    	{
	    	String description = mDescriptionText.getText().toString();
	    	
	    	double amount = 0;
	    	if(mAmountText.getText().toString().length() > 0)
	    		amount = Double.parseDouble( mAmountText.getText().toString() );
	    	
	    	String currency = (String) mAmountCurrency.getSelectedItem();

	    	DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd");
	    	Date eventDate = new Date();
	    	
	    	try
	    	{
	    		eventDate = dFormat.parse( mDateText.getText().toString() );
	    	}
	    	catch(ParseException e)
	    	{
	    		Log.e("BalanceEntryEdit", "Parsing eventDate failed", e);
	    	}
	    	
	    	
	    	if (mRowId == null) {
	    		long id = mDbHelper.createEntry(description, amount, currency, eventDate);
	    		if (id > 0) {
	    			mRowId = id;
	    		}
	    	} else {
	    		mDbHelper.updateEntry(mRowId, description, amount, currency, eventDate);
	    	}
    	}
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		// user pressed back so treat as cancel
    		doSaveState = false;
    	}
    	return super.onKeyUp(keyCode, event);
    }    
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
    	case DATE_DIALOG_ID:
    		return new DatePickerDialog(this,
    				mDateSetListener,
    				mYear, mMonth, mDay);
    	}
    	return null;
    }    
    
}
