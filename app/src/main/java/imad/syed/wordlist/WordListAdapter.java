package imad.syed.wordlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.WordListViewHolder> {
    private List<String> mWordList;

    WordListAdapter(List<String> wordList) {
        this.mWordList = wordList;
    }

    public static class WordListViewHolder extends RecyclerView.ViewHolder {
        public TextView wordItem;
        public WordListViewHolder(@NonNull View itemView) {
            super(itemView);
            wordItem = itemView.findViewById(R.id.word_row_item);
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
        holder.wordItem.setText(mWordList.get(position));
    }

    @Override
    public int getItemCount() {
        try {
            return mWordList.size();
        } catch (Exception e) {
            return 0;
        }
    }
}
