package imad.syed.wordlist;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class CustomViewHolder extends RecyclerView.ViewHolder {
    public TextView word_item;

    public CustomViewHolder(View view) {
        super(view);
        word_item = (TextView) view.findViewById(R.id.word_row_item);
    }
}
