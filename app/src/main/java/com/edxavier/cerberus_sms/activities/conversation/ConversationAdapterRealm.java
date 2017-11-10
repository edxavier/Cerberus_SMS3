package com.edxavier.cerberus_sms.activities.conversation;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.activities.conversation.contracts.ConversationPresenter;
import com.edxavier.cerberus_sms.activities.conversation.contracts.ConversationView;
import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesHistoryRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesPresenter;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesView;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.edxavier.cerberus_sms.helpers.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.edxavier.cerberus_sms.helpers.Constans.CLARO;
import static com.edxavier.cerberus_sms.helpers.Constans.CONVENCIONAL;
import static com.edxavier.cerberus_sms.helpers.Constans.COOTEL;
import static com.edxavier.cerberus_sms.helpers.Constans.DESCONOCIDO;
import static com.edxavier.cerberus_sms.helpers.Constans.INTERNACIONAL;
import static com.edxavier.cerberus_sms.helpers.Constans.MOVISTAR;

/**
 * Created by Eder Xavier Rojas on 12/10/2015.
 */
public class ConversationAdapterRealm extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements RealmChangeListener<RealmResults<MessagesHistoryRealm>> {

    private RealmResults<MessagesHistoryRealm> messages = null;
    private ConversationView view;
    private ConversationPresenter presenter;

    public ConversationAdapterRealm(RealmResults<MessagesHistoryRealm> messages, ConversationView view, ConversationPresenter presenter) {
        this.messages = messages;
        this.view = view;
        this.messages.addChangeListener(this);
        this.presenter = presenter;
    }

    @Override
    public void onChange(RealmResults<MessagesHistoryRealm> messages) {
        notifyDataSetChanged();
        view.scrollTo(0);
        if(messages.isEmpty()) {
            view.showEmptyMsg(true);
        }else {
            view.showEmptyMsg(false);
        }
    }

    public static class IncomingSmsHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        @BindView(R.id.txt_body)
        TextView txt_body;
        @BindView(R.id.txt_fecha)
        TextView txt_fecha;
        @BindView(R.id.avatar)
        AppCompatImageView avatar;

        public IncomingSmsHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public static class SentSmsHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        @BindView(R.id.txt_body)
        TextView txt_body;
        @BindView(R.id.txt_fecha)
        TextView txt_fecha;
        @BindView(R.id.txt_status)
        TextView txt_status;

        public SentSmsHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == Constans.MESSAGE_TYPE_INBOX){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_in_msg, parent, false);
            return new IncomingSmsHolder(v);
        }else{
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_out_msg, parent, false);
            return new SentSmsHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MessagesHistoryRealm msg = messages.get(position);
        if (msg.sms_type == Constans.MESSAGE_TYPE_INBOX) {
            IncomingSmsHolder holder_in = (IncomingSmsHolder) holder;
            holder_in.txt_body.setText(msg.sms_text);
            CharSequence d = DateUtils.getRelativeTimeSpanString(msg.sms_date.getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
            holder_in.txt_fecha.setText(d);
            String operador = DESCONOCIDO;
            AreaCodeRealm areaCode = Utils.getOperadoraV4(msg.sms_phone_number, holder.itemView.getContext());
            if(areaCode!=null) {
                operador = areaCode.area_operator;
            }
                switch (operador) {
                    case Constans.CLARO:
                        holder_in.avatar.setImageResource(R.drawable.ic_user_red);
                        break;
                    case Constans.MOVISTAR:
                        holder_in.avatar.setImageResource(R.drawable.ic_user_green);
                        break;
                    case Constans.COOTEL:
                        holder_in.avatar.setImageResource(R.drawable.ic_user_orange);
                        break;
                    case Constans.CONVENCIONAL:
                        holder_in.avatar.setImageResource(R.drawable.ic_user_blue);
                        break;
                    case Constans.INTERNACIONAL:
                        holder_in.avatar.setImageResource(R.drawable.ic_user_purple);
                        break;
                    case Constans.DESCONOCIDO:
                        holder_in.avatar.setImageResource(R.drawable.ic_user_unkown);
                        break;
                    default:
                        holder_in.avatar.setImageResource(R.drawable.ic_user_unkown);
                        break;
                }
        } else {
            SentSmsHolder holder_out = (SentSmsHolder) holder;
            holder_out.txt_body.setText(msg.sms_text);
            /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                holder_out.txt_body.setBackground(holder_out.itemView.getResources().getDrawable(R.drawable.out_going_msg));
            }else {
                holder_out.txt_body.setBackgroundDrawable(holder_out.itemView.getResources().getDrawable(R.drawable.out_going_msg));
            }*/
            CharSequence d = DateUtils.getRelativeTimeSpanString(msg.sms_date.getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
            holder_out.txt_fecha.setText(d);
            if(msg.sms_type == Constans.MESSAGE_TYPE_FAILED) {
                holder_out.txt_status.setText(holder_out.itemView.getResources().getString(R.string.send_fail));
                holder_out.txt_status.setTextColor(holder.itemView.getResources().getColor(R.color.md_red_700));
            }else if(msg.sms_type == Constans.MESSAGE_TYPE_QUEUED) {
                holder_out.txt_status.setText(holder_out.itemView.getResources().getString(R.string.sending));
                holder_out.txt_status.setTextColor(holder.itemView.getResources().getColor(R.color.md_blue_grey_300));
            }else {
                holder_out.txt_status.setText("");
                holder_out.txt_status.setTextColor(holder.itemView.getResources().getColor(R.color.md_black_1000_75));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (messages != null) {
            return messages.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
         super.getItemViewType(position);
        final MessagesHistoryRealm msg = messages.get(position);
        return msg.sms_type;
    }
}
