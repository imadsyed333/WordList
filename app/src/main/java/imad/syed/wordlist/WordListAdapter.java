package imad.syed.wordlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.WordListViewHolder> {
    private final List<Word> mWordList;

    WordListAdapter(List<Word> wordList) {
        this.mWordList = wordList;
    }

    public static class WordListViewHolder extends RecyclerView.ViewHolder {
        public TextView nameItem;
        public TextView meaningItem;
        public WordListViewHolder(@NonNull View itemView) {
            super(itemView);
            nameItem = itemView.findViewById(R.id.nameView);
            meaningItem = itemView.findViewById(R.id.meaningView);
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
