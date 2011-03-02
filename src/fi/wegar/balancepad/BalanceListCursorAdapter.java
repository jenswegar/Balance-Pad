package fi.wegar.balancepad;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class BalanceListCursorAdapter extends CursorAdapter {

	private LayoutInflater mInflater;
	
	BalanceListCursorAdapter(Context context, Cursor c) {
			super(context, c);
			
			mInflater = LayoutInflater.from(context);
			
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		// Format the date string
		TextView dateView = (TextView) view.findViewById(R.id.event_date);
		long millis = cursor.getLong(cursor.getColumnIndex(BalanceDbAdapter.KEY_EVENT_DATE));
		dateView.setText(DateFormat.getDateInstance().format(new Date(millis)));
		
		// create number formatter
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		nf.setCurrency( Currency.getInstance(cursor.getString( cursor.getColumnIndex( BalanceDbAdapter.KEY_CURRENCY ) ) ) );
		
		TextView amountView = (TextView) view.findViewById(R.id.amount);
		amountView.setText( nf.format( cursor.getDouble( cursor.getColumnIndex( BalanceDbAdapter.KEY_AMOUNT ) ) ) );
		
		TextView descriptionView = (TextView) view.findViewById(R.id.description);
		descriptionView.setText( cursor.getString( cursor.getColumnIndex( BalanceDbAdapter.KEY_DESCRIPTION ) ) );
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		 View view = mInflater.inflate(R.layout.entries_row, null);
		 bindView(view, context, cursor);
		 return view;
	}

}
