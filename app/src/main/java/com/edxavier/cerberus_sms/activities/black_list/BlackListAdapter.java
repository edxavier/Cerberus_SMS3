package com.edxavier.cerberus_sms.activities.black_list;

import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.activities.black_list.contracts.BlackListView;
import com.edxavier.cerberus_sms.db.realm.BlackList;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.edxavier.cerberus_sms.helpers.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.edxavier.cerberus_sms.helpers.Constans.BLOCK_NONE;

/**
 * Created by Eder Xavier Rojas on 04/09/2017.
 */

public class BlackListAdapter extends RecyclerView.Adapter<BlackListAdapter.ViewHolder>
        implements RealmChangeListener<RealmResults<BlackList>> {

    private RealmResults<BlackList> blackList;
    private BlackListView view;

    public BlackListAdapter(RealmResults<BlackList> blackList, BlackListView view) {
        this.blackList = blackList;
        this.view = view;
        blackList.addChangeListener(this);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_number)
        TextView txt_number;
        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_blocked_calls)
        TextView txt_blocked_calls;
        @BindView(R.id.txt_blocked_sms)
        TextView txt_blocked_sms;
        @BindView(R.id.sms_bl_cardviewRow)
        CardView cardviewRow;

        @BindView(R.id.btn_block_calls)
        AppCompatImageButton btn_block_calls;

        @BindView(R.id.btn_block_sms)
        AppCompatImageButton btn_block_sms;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.black_list_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BlackList entry = blackList.get(position);
        ContactRealm contact = Utils.getContact(entry.phone_number);
        if (contact != null)
            holder.txt_name.setText(contact.contact_name);
        else
            holder.txt_name.setText(entry.phone_number);
        holder.txt_number.setText(entry.phone_number);
        holder.txt_blocked_calls.setText(String.valueOf(entry.block_calls_count));
        holder.txt_blocked_sms.setText(String.valueOf(entry.block_sms_count));
        if (entry.block_incoming_sms)
            holder.btn_block_sms.setImageResource(R.drawable.ic_padlock);
        else
            holder.btn_block_sms.setImageResource(R.drawable.ic_unlock);

        if (entry.block_incoming_call)
            holder.btn_block_calls.setImageResource(R.drawable.ic_padlock);
        else
            holder.btn_block_calls.setImageResource(R.drawable.ic_unlock);

        holder.cardviewRow.setOnClickListener(view1 -> {
            Realm realm = Realm.getDefaultInstance();
            int op = BLOCK_NONE;
            BlackList entry2 = realm.where(BlackList.class).equalTo("phone_number", entry.phone_number).findFirst();
            if(entry2!=null){
                if(entry2.block_incoming_call && entry2.block_incoming_sms)
                    op = Constans.BLOCK_BOTH;
                else if(entry2.block_incoming_call && !entry2.block_incoming_sms)
                    op = Constans.BLOCK_CALLS;
                else if(!entry2.block_incoming_call && entry2.block_incoming_sms)
                    op = Constans.BLOCK_MESSAGES;

            }
            realm.close();
            new MaterialDialog.Builder(holder.itemView.getContext())
                    .title(R.string.ac_block)
                    .items(R.array.block_options)
                    .itemsCallbackSingleChoice(op, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            Utils.sendToBlackList(which, entry.phone_number);
                            return true;
                        }
                    })
                    .positiveText(R.string.accept)
                    .negativeText(R.string.cancelar)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        if (blackList != null) {
            return blackList.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onChange(RealmResults<BlackList> blackLists) {
        notifyDataSetChanged();
        if (blackLists.isEmpty())
            view.showEmptyMsg(true);
        else
            view.showEmptyMsg(false);
    }


}
