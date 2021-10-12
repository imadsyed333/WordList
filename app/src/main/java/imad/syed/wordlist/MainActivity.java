package imad.syed.wordlist;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    // Variable Declarations
    RecyclerView recyclerView;
    WordListAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Word> WordList;
    ArrayList<Word> deletedList;
    ArrayList<Word> clearedList;
    ArrayList<String> oldWordList;
    Toast toast;
    ItemTouchHelper.SimpleCallback simpleCallback;
    ItemTouchHelper itemTouchHelper;
    FloatingActionButton fabAdd, fabUndo;
    RecyclerView.OnScrollListener onScrollListener;
    SearchView searchView;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String passcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.wordListView);
        fabAdd = findViewById(R.id.addButton);
        fabUndo = findViewById(R.id.btnUndo);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        searchView = findViewById(R.id.wordSearch);
        deletedList = new ArrayList<>();
        clearedList = new ArrayList<>();
        WordList = new ArrayList<>();
        oldWordList = new ArrayList<>();
        fabUndo.hide();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("WordListData");

        //Code for the RecyclerView adapter
        adapter = new WordListAdapter(WordList);
        recyclerView.setAdapter(adapter);

        RetrievePasscode();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                WordList.clear();
                DataSnapshot wordListSnapshot = snapshot.child(passcode);
                for (DataSnapshot wordSnapshot: wordListSnapshot.getChildren()) {
                    HashMap<String, String> wordMap = (HashMap<String, String>) wordSnapshot.getValue();
                    assert wordMap != null;
                    Word word = new Word(wordMap.get("name"), wordMap.get("meaning"), wordMap.get("type"));
                    WordList.add(word);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Code for "swipe left to delete" and "swipe right to edit"
        simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int i = viewHolder.getLayoutPosition();
                if (direction == ItemTouchHelper.LEFT) {
                    deletedList.add(WordList.get(i));
                    WordList.remove(i);
                    WordListSort();
                    adapter.notifyDataSetChanged();
                    Save();
                    toast = Toast.makeText(getApplicationContext(), "Entry deleted. List Saved.", Toast.LENGTH_SHORT);
                    toast.show();
                    if (!fabUndo.isShown()) {
                        fabUndo.show();
                    }
                } else if (direction == ItemTouchHelper.RIGHT) {
                    openEditWordDialog(i);
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

        //Code for search queries
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                adapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    // Method for saving the list
    public void Save() {
        myRef.child(passcode).setValue(WordList);
    }

    // Method for saving the passcode
    public void SavePasscode() {
        SharedPreferences storage = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = storage.edit();
        editor.putString("passcode", passcode);
        editor.apply();
    }
    public void TestPutWords() {
        oldWordList.add("Kowalski is a legendary penguin who aces pizza");
        SharedPreferences storage = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = storage.edit();
        Gson gson = new Gson();
        String json = gson.toJson(oldWordList);
        editor.putString("word list", json);
        editor.apply();
        toast = Toast.makeText(getApplicationContext(), "Old List Saved.", Toast.LENGTH_SHORT);
        toast.show();
    }
    // Method for retrieving passcode
    public void RetrievePasscode() {
        SharedPreferences storage = getSharedPreferences("shared preferences", MODE_PRIVATE);
        passcode = storage.getString("passcode", "");
        assert passcode != null;
        if (passcode.isEmpty()) {
            passcode = myRef.push().getKey();
            SavePasscode();
        }
    }
    // Method for retrieving old list
    public void RetrieveOldList(View view) {
        SharedPreferences storage = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = storage.getString("word list", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        oldWordList = gson.fromJson(json, type);
        System.out.println(oldWordList);

        if (oldWordList == null) {
            oldWordList = new ArrayList<>();
            System.out.println("I DIDN'T FIND ANYTHING");
        }
        else {
            convertWords();
        }
    }
    // Method for transferring words from oldWordList into WordList
    public void convertWords () {
        for (String entry: oldWordList) {
            String[] words = entry.split(" ");
            WordList.add(new Word(words[0], entry, ""));
        }
        WordListSort();
        adapter.notifyDataSetChanged();
        Save();
    }
    // Method for adding a word
    public void AddWord (String name, String meaning, String type) {
        WordList.add(new Word(name, meaning, type));
        toast = Toast.makeText(getApplicationContext(), "Entry added. List Saved.", Toast.LENGTH_SHORT);
        WordListSort();
        adapter.notifyDataSetChanged();
        toast.show();
        Save();
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
    //Method for creating a Dialog when adding words
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
        wordMeaning.setSingleLine(false);
        wordMeaning.setHint("Enter any notes about the entry here");
        wordMeaning.setHintTextColor(Color.GRAY);

        layout.addView(wordName);
        layout.addView(wordType);
        layout.addView(wordMeaning);

        builder.setView(layout);

        builder.setPositiveButton("Add Entry", (dialogInterface, i) -> {
            String name = wordName.getText().toString();
            String meaning = wordMeaning.getText().toString();
            String type = wordType.getText().toString().toLowerCase();
            if (name.isEmpty()) {
                toast = Toast.makeText(getApplicationContext(), "Name field must not be empty", Toast.LENGTH_LONG);
                toast.show();
                openAddWordDialog(view);
            }
            else if (meaning.isEmpty()) {
                toast = Toast.makeText(getApplicationContext(), "Meaning field must not be empty", Toast.LENGTH_LONG);
                toast.show();
                openAddWordDialog(view);
            }
            else {
                AddWord(name, meaning, type);
            }
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        builder.show();
    }
    //Method for creating Dialog when editing word
    public void openEditWordDialog (final int position) {
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
        wordMeaning.setSingleLine(false);
        wordMeaning.setHint("Enter any notes about the entry here");
        wordMeaning.setHintTextColor(Color.GRAY);

        final Word currentWord = WordList.get(position);
        wordName.setText(currentWord.getName());
        wordType.setText(currentWord.getType());
        wordMeaning.setText(currentWord.getMeaning());

        layout.addView(wordName);
        layout.addView(wordType);
        layout.addView(wordMeaning);

        builder.setView(layout);

        builder.setPositiveButton("Confirm Changes", (dialogInterface, i) -> {
            String name = wordName.getText().toString();
            String meaning = wordMeaning.getText().toString();
            String type = wordType.getText().toString().toLowerCase();
            if (name.isEmpty()) {
                toast = Toast.makeText(getApplicationContext(), "Name field must not be empty", Toast.LENGTH_LONG);
                toast.show();
                openEditWordDialog(position);
            }
            else if (meaning.isEmpty()) {
                toast = Toast.makeText(getApplicationContext(), "Meaning field must not be empty", Toast.LENGTH_LONG);
                toast.show();
                openEditWordDialog(position);
            }
            else {
                if (position >= 0) {
                    currentWord.setWord(name, type, meaning);
                }
                adapter.notifyDataSetChanged();
                Save();
            }
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.cancel();
            adapter.notifyDataSetChanged();
        });
        builder.show();
    }

    //Method that compares entries by name
    public void WordListSort() {
        Collections.sort(WordList, (word1, word2) -> word1.getName().compareToIgnoreCase(word2.getName()));
    }
}