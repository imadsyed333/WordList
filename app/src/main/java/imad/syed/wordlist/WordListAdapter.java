package imad.syed.wordlist;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.WordListViewHolder> {
    private List<Word> mWordList;
    List<Word> listCopy;

    WordListAdapter(List<Word> wordList) {
        this.mWordList = wordList;
        this.listCopy = wordList;
    }

    public static class WordListViewHolder extends RecyclerView.ViewHolder {
        public TextView nameItem;
        public TextView meaningItem;
        public TextView typeItem;
        public WordListViewHolder(@NonNull View itemView) {
            super(itemView);
            nameItem = itemView.findViewById(R.id.nameView);
            meaningItem = itemView.findViewById(R.id.meaningView);
            typeItem = itemView.findViewById(R.id.typeView);
        }
    }

    @NonNull
    @Override
    public WordListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
        return new WordListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordListViewHolder holder, int position) {
        holder.nameItem.setText(mWordList.get(position).getName());
        holder.meaningItem.setText(mWordList.get(position).getMeaning());
        holder.typeItem.setText(mWordList.get(position).getType());
    }

    @Override
    public int getItemCount() {
        try {
            return mWordList.size();
        } catch (Exception e) {
            return 0;
        }
    }
    public void filter(String text) {
        List<Word> filteredList = new ArrayList<>();
        if (!text.isEmpty()) {
            for (Word word : listCopy) {
                if (word.contains(text)) {
                    filteredList.add(word);
                }
            }
            mWordList = filteredList;
        } else {
            mWordList = listCopy;
        }
        notifyDataSetChanged();
    }
}
