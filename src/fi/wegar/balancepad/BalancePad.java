package fi.wegar.balancepad;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class BalancePad extends ListActivity {
	
	private static final int ACTIVITY_SETTINGS=0;
    private static final int ACTIVITY_CREATE = ACTIVITY_SETTINGS+1;
    private static final int ACTIVITY_EDIT = ACTIVITY_SETTINGS+2;
    
    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;

	private BalanceDbAdapter mDbHelper;
	
	
	
    /** Called when the activity is first created. Sets up the view and opens the database connection */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance_entries_list);
        
        registerForContextMenu(getListView());
        
        mDbHelper = new BalanceDbAdapter(this);
        mDbHelper.open();
        
        populateList();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    }
    
    /**
     * Populates the list of balance entries from the database
     */
    private void populateList()
    {
    	Cursor entriesCursor = mDbHelper.fetchAllEntries();
    	startManagingCursor(entriesCursor);
    	
        setListAdapter(new BalanceListCursorAdapter(this, entriesCursor) );

    }
    
    /**
     * Creates the menu that is displayed when the user presses the menu button on the device
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	return true;
    }    

    /**
     * Called when the user selects one of the items in the options menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection
    	switch ( item.getItemId() ) {
	    	case R.id.menu_add:
	    		createEntry();
	    		return true;
	    	case R.id.menu_settings:
	    		openSettings();
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    }    

    /**
     * Creates a context menu for the list items
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        menu.add(0, EDIT_ID, 1, R.string.edit_entry);
        menu.add(0, DELETE_ID, 2, R.string.menu_delete);
    }

    /**
     * Called when the user selects one of the menu items from the list item context menu
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	
        switch(item.getItemId()) {
	        case INSERT_ID:
	        	createEntry();
	            return true;
            case DELETE_ID:
                mDbHelper.deleteEntry(info.id);
                populateList();
                return true;
            case EDIT_ID:
                Intent i = new Intent(this, BalanceEntryEdit.class);
                i.putExtra(BalanceDbAdapter.KEY_ROWID, info.id);
                startActivityForResult(i, ACTIVITY_EDIT);
            	return true;
        }
        return super.onContextItemSelected(item);
    }    
    
    /**
     * Starts a intent for adding a balance entry
     */
    private void createEntry() {
        Intent i = new Intent(this, BalanceEntryEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    /**
     * Starts a intent for modifying application preferences
     */
    private void openSettings() {
    	Intent i = new Intent(this, Preferences.class);
    	startActivityForResult(i, ACTIVITY_SETTINGS);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        populateList();
    }
    
    
    /**
     * Called when the user clicks one of the items in the list of entries. Opens the Edit view for that item.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, BalanceEntryEdit.class);
        i.putExtra(BalanceDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }
    
    
}