package imad.syed.wordlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.widget.EditText;

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
    List<String> oldWordList;
    Toast toast;
    ItemTouchHelper.SimpleCallback simpleCallback;
    ItemTouchHelper itemTouchHelper;
    FloatingActionButton fabAdd, fabUndo;
    RecyclerView.OnScrollListener onScrollListener;
    SearchView searchView;

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

//        TestPutWords();
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
                int i = viewHolder.getAdapterPosition();
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
        SharedPreferences storage = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = storage.edit();
        Gson gson = new Gson();
        String json = gson.toJson(WordList);
        editor.putString("wordlist", json);
        editor.apply();
    }
    // Method for retrieving the saved list
    public void RetrieveList (){
        try {
            SharedPreferences storage = getSharedPreferences("shared preferences", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = storage.getString("wordlist", String.valueOf(new ArrayList<Word>()));
            Type type = new TypeToken<ArrayList<Word>>() {}.getType();
            WordList = gson.fromJson(json, type);
            WordListSort();
            adapter.notifyDataSetChanged();
        } catch (NullPointerException ne) {
            Log.d("wordListError", "The WordList appears to be empty");
        }
    }

//    public void TestPutWords() {
//        List<String> oldWords = new ArrayList<>();
//        oldWords.add("Kowalski is a legendary penguin that aces pizza");
//        SharedPreferences storage = getSharedPreferences("shared preferences", MODE_PRIVATE);
//        SharedPreferences.Editor editor = storage.edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(oldWords);
//        editor.putString("word list", json);
//        editor.apply();
//    }

    public void RetrieveOldList() {
        try {
            SharedPreferences storage = getSharedPreferences("shared preferences", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = storage.getString("word list", String.valueOf(new ArrayList<String>()));
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            oldWordList = gson.fromJson(json, type);
            if (!oldWordList.isEmpty()) {
                convertWords();
            }
        } catch (NullPointerException ne) {
            Log.d("oldListError", "The old WordList appears to be empty");
        }
    }

    public void convertWords() {
        for (String entry : oldWordList) {
            String[] words = entry.split(" ");
            AddWord(words[0], entry, "");
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setTitle("Add Entry");

        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.word_dialog, null);

        final EditText wordName = dialogView.findViewById(R.id.txtWordName);
        final EditText wordMeaning = dialogView.findViewById(R.id.txtWordMeaning);
        final EditText wordType = dialogView.findViewById(R.id.txtWordType);

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
        builder.setView(dialogView);
        builder.show();
    }
    //Method for creating Dialog when editing word
    public void openEditWordDialog (final int position) {
        final Word currentWord = WordList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setTitle("Add Entry");

        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.word_dialog, null);

        final EditText wordName = dialogView.findViewById(R.id.txtWordName);
        final EditText wordMeaning = dialogView.findViewById(R.id.txtWordMeaning);
        final EditText wordType = dialogView.findViewById(R.id.txtWordType);

        wordName.setText(currentWord.getName());
        wordMeaning.setText(currentWord.getMeaning());
        wordType.setText(currentWord.getType());

        builder.setPositiveButton("Confirm Changes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = wordName.getText().toString();
                String meaning = wordMeaning.getText().toString();
                String type = wordType.getText().toString().toLowerCase();
                if (name.isEmpty() || meaning.isEmpty()) {
                    toast = Toast.makeText(getApplicationContext(), "Entry must not be empty", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    currentWord.editWord(name, meaning, type);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                adapter.notifyDataSetChanged();
            }
        });
        builder.setView(dialogView);
        builder.show();
    }

    public void retrieveOldListDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setTitle(R.string.oldListMessage);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RetrieveOldList();
                Toast toast = Toast.makeText(getApplicationContext(), "Old Words Retrieved.", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    //Method that compares entries by name
    public void WordListSort() {
        Collections.sort(WordList, new Comparator<Word>() {
            @Override
            public int compare(Word word1, Word word2) {
                return word1.getName().compareToIgnoreCase(word2.getName());
            }
        });
    }
}