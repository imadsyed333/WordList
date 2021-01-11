package imad.syed.wordlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.EditText;

import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Variable Declarations
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    List<String> WordList;
    List<String> deletedList;
    List<String> clearedList;
    Toast toast;
    ItemTouchHelper.SimpleCallback simpleCallback;
    ItemTouchHelper itemTouchHelper;
    FloatingActionButton fabAdd, fabUndo;
    Toolbar toolbar;
    boolean listChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.wordListView);
        fabAdd = findViewById(R.id.addButton);
        fabUndo = findViewById(R.id.btnUndo);
        toolbar = findViewById(R.id.toolbar);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        deletedList = new ArrayList<>();
        clearedList = new ArrayList<>();

        fabUndo.hide();
        listChanged = false;

        //Code for the RecyclerView adapter
        adapter = new RecyclerView.Adapter<CustomViewHolder>() {
            @Override
            public CustomViewHolder onCreateViewHolder(ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
                return new CustomViewHolder(view);
            }

            @Override
            public void onBindViewHolder(CustomViewHolder viewHolder, int i) {
                viewHolder.word_item.setText(WordList.get(i));
            }

            @Override
            public int getItemCount() {
                return WordList.size();
            }
        };
        recyclerView.setAdapter(adapter);

        //Code for "swipe left to delete"
        simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    int i = viewHolder.getAdapterPosition();
                    deletedList.add(WordList.get(i));
                    WordList.remove(i);
                    toast = Toast.makeText(getApplicationContext(), "Entry deleted. List Saved.", Toast.LENGTH_SHORT);
                    toast.show();
                    adapter.notifyDataSetChanged();
                    Save();
                    listChanged = true;
                    if (!fabUndo.isShown()) {
                        fabUndo.show();
                    }
                }
            }
        };
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        //Code for hiding fab when scrolling
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && fabAdd.isShown()) {
                    fabAdd.hide();
                    fabUndo.hide();
                } else {
                    fabAdd.show();
                    if (!deletedList.isEmpty()) {
                        fabUndo.show();
                    }
                }
            }
        });

        RetrieveList();
    }
    // Method for saving the list
    public void Save () {
        SharedPreferences storage = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = storage.edit();
        Gson gson = new Gson();
        String json = gson.toJson(WordList);
        editor.putString("word list", json);
        editor.apply();
    }
    // Method for retrieving the saved list
    public void RetrieveList (){
        SharedPreferences storage = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = storage.getString("word list", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        WordList = gson.fromJson(json, type);

        if (WordList == null) {
            WordList = new ArrayList<>();
        }
        adapter.notifyDataSetChanged();
    }
    // Method for adding a word
    public void AddWord (String input) {
        WordList.add(input);
        Collections.sort(WordList);
        toast = Toast.makeText(getApplicationContext(), "Entry added. List Saved.", Toast.LENGTH_SHORT);
        Save();
        adapter.notifyDataSetChanged();
        toast.show();
    }
    //Method for undoing DeleteWord
    public void UndoAction (View view) {
        String delWord = deletedList.get(deletedList.size() - 1);
        WordList.add(delWord);
        deletedList.remove(delWord);
        Collections.sort(WordList);
        adapter.notifyDataSetChanged();
        listChanged = true;
        Save();
        toast = Toast.makeText(getApplicationContext(), "Entry re-added. List Saved.", Toast.LENGTH_SHORT);
        toast.show();
        if (deletedList.size() == 0) {
            fabUndo.hide();
        }
    }
    //Method for opening Dialog
    public void openAddWordDialog (View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Entry");

        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(editText);

        builder.setPositiveButton("Add Entry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String input = editText.getText().toString();
                if (input.isEmpty()) {
                    toast = Toast.makeText(getApplicationContext(), "Entry must not be empty", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    AddWord(input);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }
}