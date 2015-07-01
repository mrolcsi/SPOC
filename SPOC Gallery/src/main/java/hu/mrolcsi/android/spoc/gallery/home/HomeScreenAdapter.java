package hu.mrolcsi.android.spoc.gallery.home;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.lucasr.twowayview.widget.SpannableGridLayoutManager;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2015.07.01.
 * Time: 21:48
 */

public class HomeScreenAdapter extends RecyclerView.Adapter<HomeScreenAdapter.TextViewHolder> {

    private Random rnd = new Random();

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_1, viewGroup, false);
        return new TextViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TextViewHolder textViewHolder, int i) {
        textViewHolder.textView.setText("Random Item #" + rnd.nextInt(100));

        SpannableGridLayoutManager.LayoutParams lp = (SpannableGridLayoutManager.LayoutParams) textViewHolder.itemView.getLayoutParams();
        if (i % 5 == 0) {
            lp.colSpan = 2;
            lp.rowSpan = 2;
            textViewHolder.itemView.setBackgroundColor(Color.BLUE);
        } else {
            lp.colSpan = 1;
            lp.rowSpan = 1;
            textViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
        textViewHolder.itemView.setLayoutParams(lp);
    }

    @Override
    public int getItemCount() {
        return 25;
    }

    class TextViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public TextViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }
}
