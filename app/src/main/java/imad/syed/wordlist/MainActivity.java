package imad.syed.wordlist;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    DatabaseReference userRef;
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
        userRef = database.getReference("users");

        RetrievePasscode();
        RetrieveList();

        //Code for the RecyclerView adapter
        adapter = new WordListAdapter(WordList);
        recyclerView.setAdapter(adapter);

        //Code for "swipe left to delete"
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
    public void Save () {
        Map<String, Object> WordListPackage = new HashMap<>();
        WordListPackage.put(passcode, WordList);
        myRef.updateChildren(WordListPackage);
    }
    // Method for saving the passcode
    public void SavePasscode () {
        SharedPreferences storage = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = storage.edit();
        editor.putString("passcode", passcode);
        editor.apply();
    }
    // Method for retrieving the saved list
    public void RetrieveList (){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                WordList.clear();
                for (DataSnapshot wordListSnapshot : snapshot.getChildren()) {
                    if (Objects.equals(wordListSnapshot.getKey(), passcode)) {
                        for (DataSnapshot wordSnapshot : wordListSnapshot.getChildren()) {
                            Map<String, String> wordMap = (Map<String, String>) wordSnapshot.getValue();
                            assert wordMap != null;
                            WordList.add(new Word(wordMap.get("name"), wordMap.get("meaning"), wordMap.get("type")));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("WordListData", "Data not retrieved");
            }
        });
    }
    // Method for retrieving passcode
    public void RetrievePasscode () {
        SharedPreferences storage = getSharedPreferences("shared preferences", MODE_PRIVATE);
        passcode = storage.getString("passcode", "");
        assert passcode != null;
        if (passcode.isEmpty()) {
            openPasswordDialog();
        }
    }
    // Method for adding a word
    public void AddWord (String name, String meaning, String type) {
        WordList.add(new Word(name, meaning, type));
        toast = Toast.makeText(getApplicationContext(), "Entry added. List Saved.", Toast.LENGTH_SHORT);
        Save();
        WordListSort();
        adapter.notifyDataSetChanged();
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
        final Word currentWord = WordList.get(position);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Entry");

        final EditText wordName = new EditText(this);
        wordName.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        wordName.setHint("Enter the name of the entry here");
        wordName.setText(currentWord.getName());
        wordName.setHintTextColor(Color.GRAY);

        final EditText wordType = new EditText(this);
        wordType.setInputType(InputType.TYPE_CLASS_TEXT);
        wordType.setHint("Enter the type of your entry here");
        wordType.setText(currentWord.getType());
        wordType.setHintTextColor(Color.GRAY);

        final EditText wordMeaning = new EditText(this);
        wordMeaning.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        wordMeaning.setHint("Enter any notes about the entry here");
        wordMeaning.setText(currentWord.getMeaning());
        wordMeaning.setHintTextColor(Color.GRAY);

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
                currentWord.setWord(name, type, meaning);
                adapter.notifyDataSetChanged();
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

    public void openPasswordDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Password");

        final EditText passWord = new EditText(this);
        passWord.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passWord.setHint("Type password here");
        passWord.setHintTextColor(Color.GRAY);

        layout.addView(passWord);

        builder.setView(layout);

        builder.setPositiveButton("Confirm Password", (dialogInterface, i) -> {
            String input = passWord.getText().toString();
            
            if (input.isEmpty()) {
                toast = Toast.makeText(getApplicationContext(), "Password must not be empty", Toast.LENGTH_SHORT);
                toast.show();
                openPasswordDialog();
            }
            else {
                passcode = input;
                SavePasscode();
            }
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        builder.show();
    }
}