package imad.syed.wordlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;

import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Variable Declarations
    RecyclerView recyclerView;
    WordListAdapter adapter;
    LinearLayoutManager layoutManager;
    List<Word> WordList;
    List<Word> deletedList;
    List<Word> clearedList;
    List<String> oldWordList;
    Toast toast;
    ItemTouchHelper.SimpleCallback simpleCallback;
    ItemTouchHelper itemTouchHelper;
    FloatingActionButton fabAdd, fabUndo, fabTop;
    AppBarLayout appBarLayout;
    androidx.appcompat.widget.SearchView searchView;
    NestedScrollView scrollView;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        recyclerView = findViewById(R.id.wordListView);
        fabAdd = findViewById(R.id.addButton);
        fabUndo = findViewById(R.id.btnUndo);
        fabTop = findViewById(R.id.btnTop);
        scrollView = findViewById(R.id.scrollView);
        appBarLayout = findViewById(R.id.appbarLayout);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        searchView = findViewById(R.id.wordSearch);
        deletedList = new ArrayList<>();
        clearedList = new ArrayList<>();
        WordList = new ArrayList<>();
        oldWordList = new ArrayList<>();
        appBarLayout.setExpanded(true, true);
        fabUndo.hide();
        fabTop.hide();

        RetrieveList();

        if (WordList.isEmpty()) {
            noWords();
        }
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
                int i = viewHolder.getBindingAdapterPosition();
                if (direction == ItemTouchHelper.LEFT) {
                    deletedList.add(WordList.get(i));
                    WordList.remove(i);
                    WordListSort();
                    adapter.notifyItemRemoved(i);
                    Save();
                    toast = Toast.makeText(getApplicationContext(), "Entry deleted. List Saved.", Toast.LENGTH_SHORT);
                    toast.show();
                    if (!fabUndo.isShown()) {
                        fabUndo.show();
                    }
                } else if (direction == ItemTouchHelper.RIGHT) {
                    openEditWordDialog(i);
                    adapter.notifyItemChanged(i);
                }
            }
        };
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == 0) {
                    fabTop.hide();
                } else {
                    fabTop.show();
                }

                int dy = oldScrollY - scrollY;
                if (dy == 0) {
                    fabAdd.show();
                } else {
                    fabAdd.hide();
                }
            }
        });

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
            adapter.notifyDataSetChanged();
        } catch (NullPointerException ne) {
            Log.d("wordListError", "The WordList appears to be empty");
        }
    }

    public void sendWords(View view) {
        Gson gson = new Gson();
        File words = new File("words.txt");
        String json = gson.toJson(WordList);
        try {
            FileWriter fileWriter = new FileWriter("/storage/emulated/0/Download/words.txt");
            fileWriter.write(json);
            fileWriter.close();
            alert("Exported words to words.txt");
        } catch (IOException e) {
            e.printStackTrace();
            alert("Export failed");
        }
    }
    // Method for adding a word
    public void AddWord (String name, String meaning, String type) {
        Word newWord = new Word(name, meaning, type);
        WordList.add(newWord);
        alert("Entry added");
        WordListSort();
        Save();
        adapter.notifyDataSetChanged();
    }
    //Method for undoing DeleteWord
    public void UndoAction (View view) {
        Word delWord = deletedList.get(deletedList.size() - 1);
        WordList.add(delWord);
        deletedList.remove(delWord);
        WordListSort();
        adapter.notifyDataSetChanged();
        Save();
        alert("Entry re-added");
        if (deletedList.size() == 0) {
            fabUndo.hide();
        }
    }
    //Method for creating a Dialog when adding words
    public void openAddWordDialog (View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, 4);
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
                if (name.isEmpty()) {
                    alert("Name must not be empty");
                } else {
                    AddWord(name, meaning, type);
                }
                adapter.notifyDataSetChanged();
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this, 4);
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
                if (name.isEmpty()) {
                    alert("Name must not be empty");
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

    public void sendToTop (View view) {
        scrollView.scrollTo(0, 0);
        appBarLayout.setExpanded(true);
        fabTop.hide();
    }

    public void fetchDefinition(View view) {
        View dialogView = (View) view.getParent();
        EditText meaningField = dialogView.findViewById(R.id.txtWordMeaning);
        EditText nameField = dialogView.findViewById(R.id.txtWordName);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + nameField.getText().toString();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                displayDefinitions(meaningField, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error ", "request failed");
                alert("No official definition exists");
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    public void noWords() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, 4);
        builder.setTitle("No words?");
        builder.setPositiveButton("Add some", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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

    public void displayDefinitions(EditText meaningField, JSONArray jsonArray) {
        List<String> meanings = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject wordObject = jsonArray.getJSONObject(i);
                JSONArray meaningArray = wordObject.getJSONArray("meanings");
                for (int j = 0; j < meaningArray.length(); j++) {
                    JSONObject meaningObject = meaningArray.getJSONObject(j);
                    String meaning = meaningObject.getJSONArray("definitions").getJSONObject(0).get("definition").toString();
                    meanings.add(meaning);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ListView listView = new ListView(this);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.simple_row, meanings);
        Log.d("meanings:", String.valueOf(meanings));
        listView.setAdapter(adapter);
        builder.setView(listView);
        builder.setTitle("Choose a definition");
        AlertDialog dialog = builder.create();
        dialog.show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                meaningField.setText(meanings.get(i));
                dialog.cancel();
            }
        });
    }

    public void alert(String message) {
        toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}