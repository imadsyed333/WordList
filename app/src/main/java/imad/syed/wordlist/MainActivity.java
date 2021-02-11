package imad.syed.wordlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import android.text.InputType;
import android.util.Log;
import android.view.View;

import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Variable Declarations
    RecyclerView recyclerView;
    WordListAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    List<Word> WordList;
    List<Word> deletedList;
    List<Word> clearedList;
    Toast toast;
    ItemTouchHelper.SimpleCallback simpleCallback;
    ItemTouchHelper itemTouchHelper;
    FloatingActionButton fabAdd, fabUndo;
    RecyclerView.OnScrollListener onScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.wordListView);
        fabAdd = findViewById(R.id.addButton);
        fabUndo = findViewById(R.id.btnUndo);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        deletedList = new ArrayList<>();
        clearedList = new ArrayList<>();
        WordList = new ArrayList<>();
        fabUndo.hide();

        RetrieveList();

        //Code for the RecyclerView adapter
        adapter = new WordListAdapter(WordList);
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
                    WordListSort();
                    adapter.notifyDataSetChanged();
                    Save();
                    if (!fabUndo.isShown()) {
                        fabUndo.show();
                    }
                }
            }
        };
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        //Code for hiding fab when scrolling
        onScrollListener = new RecyclerView.OnScrollListener() {
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
        };
        recyclerView.addOnScrollListener(onScrollListener);
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
        try {
            SharedPreferences storage = getSharedPreferences("shared preferences", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = storage.getString("word list", String.valueOf(new ArrayList<Word>()));
            Type type = new TypeToken<ArrayList<Word>>() {}.getType();
            WordList = gson.fromJson(json, type);
            WordListSort();
            adapter.notifyDataSetChanged();
        } catch (NullPointerException ne) {
            Log.d("wordListError", "The WordList appears to be empty");
        }
    }
    // Method for adding a word
    public void AddWord (String name, String meaning, String type) {
        WordList.add(new Word(name, meaning, type));
        toast = Toast.makeText(getApplicationContext(), "Entry added. List Saved.", Toast.LENGTH_SHORT);
        Save();
        adapter.notifyDataSetChanged();
        WordListSort();
        toast.show();
    }
    //Method for undoing DeleteWord
    public void UndoAction (View view) {
        Word delWord = deletedList.get(deletedList.size() - 1);
        WordList.add(delWord);
        deletedList.remove(delWord);
        WordListSort();
        adapter.notifyDataSetChanged();
        Save();
        toast = Toast.makeText(getApplicationContext(), "Entry re-added. List Saved.", Toast.LENGTH_SHORT);
        toast.show();
        if (deletedList.size() == 0) {
            fabUndo.hide();
        }
    }
    //Method for opening Dialog
    public void openAddWordDialog (View view) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Entry");

        final EditText wordName = new EditText(this);
        wordName.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        wordName.setHint("Enter the name of the entry here");
        wordName.setHintTextColor(Color.GRAY);

        final EditText wordType = new EditText(this);
        wordType.setInputType(InputType.TYPE_CLASS_TEXT);
        wordType.setHint("Enter the type of your entry here");
        wordType.setHintTextColor(Color.GRAY);

        final EditText wordMeaning = new EditText(this);
        wordMeaning.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        wordMeaning.setHint("Enter any notes about the entry here");
        wordMeaning.setHintTextColor(Color.GRAY);

        layout.addView(wordName);
        layout.addView(wordType);
        layout.addView(wordMeaning);

        builder.setView(layout);

        builder.setPositiveButton("Add Entry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = wordName.getText().toString();
                String meaning = wordMeaning.getText().toString();
                String type = wordType.getText().toString().toLowerCase();
                if (name.isEmpty() || meaning.isEmpty()) {
                    toast = Toast.makeText(getApplicationContext(), "Entry must not be empty", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    AddWord(name, meaning, type);
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

    public void WordListSort() {
        Collections.sort(WordList, new Comparator<Word>() {
            @Override
            public int compare(Word word1, Word word2) {
                return word1.getName().compareToIgnoreCase(word2.getName());
            }
        });
    }
}