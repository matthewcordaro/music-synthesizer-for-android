package com.levien.synthesizer.android.ui;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.levien.synthesizer.R;

import android.widget.AbsListView.MultiChoiceModeListener;
 
public class TempActivity extends AppCompatActivity {
 
  // Declare Variables
  ListView list;
  ListViewAdapter listviewadapter;
  List<WorldPopulation> worldpopulationlist = new ArrayList<WorldPopulation>();
  String[] rank;
  String[] country;
  String[] population;
  String[] flag;
  final Context context = this;
 
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Get the view from listview_main.xml
    setContentView(R.layout.listview_main);
    Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
    setSupportActionBar(myToolbar);
    getSupportActionBar().setTitle("");
    // Generate sample data into string arrays
    rank = new String[] { "Track1", "Track2", "Track3", "Track4", "Track5", "Track6", "Track7", "Track8", "Track9", "Track10" };
 
    country = new String[] { "0", "1", "2",
        "3", "4", "5", "6", "7",
        "8", "9" };
 
    population = new String[] { "Piano", "Guitar",
        "Bass", "Violin", "Bass", "Drums",
        "Piano", "Strings", "Orchestra", "Marimba" };
 
    flag = new String[] { "On", "On", "Off",
            "On", "On", "Off", "On", "On", "Off", "Off" };
 
    for (int i = 0; i < rank.length; i++) {
      WorldPopulation worldpopulation = new WorldPopulation(flag[i],
          rank[i], country[i], population[i]);
      worldpopulationlist.add(worldpopulation);
    }
 
    // Locate the ListView in listview_main.xml
    list = (ListView) findViewById(R.id.listview);
 
    // Pass results to ListViewAdapter Class
    listviewadapter = new ListViewAdapter(this, R.layout.listview_item,
        worldpopulationlist);
 
    // Binds the Adapter to the ListView
    list.setAdapter(listviewadapter);
    
    list.setOnItemClickListener(new OnItemClickListener()
    { 
        public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
        { 
            Snackbar.make(arg1, "Item clicked"+ position, Snackbar.LENGTH_SHORT).show();
        }
    });
    

    final FloatingActionButton actionA = (FloatingActionButton) findViewById(R.id.action_a);
    final FloatingActionsMenu floatMenu = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
    actionA.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        floatMenu.collapse();
        final Dialog addDialog = new Dialog(context);
        addDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addDialog.setContentView(R.layout.add_track_dialog);
        addDialog.show();
        
        Button cancelButton = (Button)addDialog.findViewById(R.id.cancelButton);
        Button saveButton = (Button)addDialog.findViewById(R.id.saveButton);
        
        cancelButton.setOnClickListener(new View.OnClickListener() {          
        public void onClick(View v) {
            addDialog.dismiss();
        }
      });
        
      saveButton.setOnClickListener(new View.OnClickListener() {   
        public void onClick(View v) {
          String trackname = ((EditText)addDialog.findViewById(R.id.trackName)).getText().toString();                 
          String channel=((Spinner) addDialog.findViewById(R.id.channelSelector)).getSelectedItem().toString();
          String instrument=((Spinner) addDialog.findViewById(R.id.presetSpinner)).getSelectedItem().toString();
          String toggleval = ((ToggleButton) addDialog.findViewById(R.id.on_off)).isChecked() ? "On" : "Off";
          
          WorldPopulation worldpopulation = new WorldPopulation(toggleval,
                  trackname, channel, instrument);
              worldpopulationlist.add(worldpopulation);
          listviewadapter.notifyDataSetChanged();
          addDialog.dismiss(); 
        }
      });
    
      }
     });

    list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    // Capture ListView item click
    list.setMultiChoiceModeListener(new MultiChoiceModeListener() {
 
      public void onItemCheckedStateChanged(ActionMode mode,
          int position, long id, boolean checked) {
        // Capture total checked items
        final int checkedCount = list.getCheckedItemCount();
        // Set the CAB title according to total checked items
        mode.setTitle(checkedCount + " Selected");
        // Calls toggleSelection method from ListViewAdapter Class
        listviewadapter.toggleSelection(position);
      }
 
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
        case R.id.delete:
          // Calls getSelectedIds method from ListViewAdapter Class
          SparseBooleanArray selected = listviewadapter.getSelectedIds();
          // Captures all selected ids with a loop
          for (int i = (selected.size() - 1); i >= 0; i--) {
            if (selected.valueAt(i)) {
              WorldPopulation selecteditem = listviewadapter.getItem(selected.keyAt(i));
              // Remove selected items following the ids
              listviewadapter.remove(selecteditem);
            }
          }
          // Close CAB
          mode.finish();
          return true;
        default:
          return false;
        }
      }
 
      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
      }
 
      public void onDestroyActionMode(ActionMode mode) {
        // TODO Auto-generated method stub
        listviewadapter.removeSelection();
      }
 
      public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        // TODO Auto-generated method stub
        return false;
      }
    });
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.toolbar_options, menu);
      return true;
  }
}