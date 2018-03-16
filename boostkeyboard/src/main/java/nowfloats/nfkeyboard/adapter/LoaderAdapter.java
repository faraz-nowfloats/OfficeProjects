package nowfloats.nfkeyboard.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nowfloats.nfkeyboard.R;
import nowfloats.nfkeyboard.interface_contracts.ItemClickListener;
import nowfloats.nfkeyboard.models.AllSuggestionModel;

/**
 * Created by Admin on 04-03-2018.
 */

class LoaderAdapter extends BaseAdapter<AllSuggestionModel> {

    LoaderAdapter(Context context, ItemClickListener listener) {
        super(context, listener);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_item_loader, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, AllSuggestionModel suggestion) {
    }
    class MyHolder extends RecyclerView.ViewHolder{

        public MyHolder(View itemView) {
            super(itemView);
            linLayoutParams.setMargins(metrics.widthPixels * 12 / 100, topSpace, 0, topSpace);
            itemView.setLayoutParams(linLayoutParams);
        }
    }
}