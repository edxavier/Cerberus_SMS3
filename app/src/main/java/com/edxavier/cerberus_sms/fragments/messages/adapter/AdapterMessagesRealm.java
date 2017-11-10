package com.edxavier.cerberus_sms.fragments.messages.adapter;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.cerberus_sms.activities.conversation.DetailConversation;
import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.BlackList;
import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesPresenter;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesView;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.edxavier.cerberus_sms.helpers.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.edxavier.cerberus_sms.helpers.Constans.BLOCK_NONE;
import static com.edxavier.cerberus_sms.helpers.Constans.CLARO;
import static com.edxavier.cerberus_sms.helpers.Constans.CONVENCIONAL;
import static com.edxavier.cerberus_sms.helpers.Constans.COOTEL;
import static com.edxavier.cerberus_sms.helpers.Constans.DESCONOCIDO;
import static com.edxavier.cerberus_sms.helpers.Constans.INTERNACIONAL;
import static com.edxavier.cerberus_sms.helpers.Constans.MOVISTAR;

/**
 * Created by Eder Xavier Rojas on 12/10/2015.
 */
public class AdapterMessagesRealm extends RecyclerView.Adapter<AdapterMessagesRealm.ViewHolder>
        implements RealmChangeListener<RealmResults<MessagesRealm>> {

    private RealmResults<MessagesRealm> messages = null;
    private MessagesView view;
    private MessagesPresenter presenter;
    private AreaCodeRealm areaCode;

    public AdapterMessagesRealm(RealmResults<MessagesRealm> messages, MessagesView view, MessagesPresenter presenter) {
        this.messages = messages;
        this.view = view;
        this.messages.addChangeListener(this);
        this.presenter = presenter;
    }

    @Override
    public void onChange(RealmResults<MessagesRealm> element) {
        notifyDataSetChanged();
        if(element.isEmpty()) {
            view.showEmptyMsg(true);
        }else {
            view.showEmptyMsg(false);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        @BindView(R.id.sender_name)
        TextView senderName;
        @BindView(R.id.conversation_sms_count)
        TextView conversationSmsCount;
        //@BindView(R.id.new_messages)
        //TextViewHelper newMessages;
        //@BindView(R.id.card_new_messages)
        //CardView cardNewMessages;
        @BindView(R.id.lbl_sms_sender_number)
        TextView lblSmsSenderNumber;
        @BindView(R.id.msg_body)
        TextView msgBody;
        //@BindView(R.id.lbl_contact_country)
        //TextViewHelper lblContactCountry;
        @BindView(R.id.lbl_sms_date)
        TextView lblSmsDate;
        //@BindView(R.id.lbl_sms_time)
        //TextViewHelper lblSmsTime;
        @BindView(R.id.lbl_sms_operator)
        TextView lblSmsOperator;
        //@BindView(R.id.img_send_failures)
        //ImageView imgSendFailures;
        @BindView(R.id.avatar_operator)
        AppCompatImageView avatar_operator;
        @BindView(R.id.send_fail)
        AppCompatImageView send_fail;
        @BindView(R.id.sms_bl_cardviewRow)
        CardView smsBlCardviewRow;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_inbox_v2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final MessagesRealm resume = messages.get(position);
        String name;
        int unreads = presenter.getUnreadedMsgs(resume.sms_phone_number);
        int fails_msg = presenter.getFailedMsgs(resume.sms_phone_number);

        if(resume.contact!=null)
            name = (resume.contact.contact_name);
        else
            name = (resume.sms_phone_number);

        if(fails_msg>0)
            holder.send_fail.setVisibility(View.VISIBLE);
        else
            holder.send_fail.setVisibility(View.GONE);
        if(unreads>0){
            holder.senderName.setTypeface(null, Typeface.BOLD);
            holder.msgBody.setTypeface(null, Typeface.BOLD);
            holder.conversationSmsCount.setTypeface(null, Typeface.BOLD);
            holder.lblSmsSenderNumber.setTypeface(null, Typeface.BOLD_ITALIC);
            holder.lblSmsDate.setTypeface(null, Typeface.BOLD_ITALIC);
            holder.lblSmsOperator.setTypeface(null, Typeface.BOLD_ITALIC);
        }else {
            holder.senderName.setTypeface(null, Typeface.NORMAL);
            holder.msgBody.setTypeface(null, Typeface.NORMAL);
            holder.conversationSmsCount.setTypeface(null, Typeface.NORMAL);
            holder.lblSmsSenderNumber.setTypeface(null, Typeface.ITALIC);
            holder.lblSmsDate.setTypeface(null, Typeface.ITALIC);
            holder.lblSmsOperator.setTypeface(null, Typeface.ITALIC);
        }

        if(unreads>0)
            holder.conversationSmsCount.setText("("+String.valueOf(unreads) +"/"+ String.valueOf(resume.entries.size())+")");
        else
            holder.conversationSmsCount.setText("("+String.valueOf(resume.entries.size())+")");

        holder.senderName.setText(name);
        holder.lblSmsSenderNumber.setText(resume.sms_phone_number);
        holder.msgBody.setText(resume.entries.last().sms_text);

        if (resume.entries.last().sms_date != null) {
            CharSequence d = DateUtils.getRelativeTimeSpanString(resume.entries.last().sms_date.getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            //holder.lblSmsDate.setText(date_format.format(d));
            holder.lblSmsDate.setText(d);
            //holder.lblSmsTime.setText(time_format.format(resume.sms_date));
        }
        boolean isNumber = Utils.isPhoneNumber(resume.sms_phone_number);
        boolean isMarketNum = !resume.sms_phone_number.startsWith("+505") && resume.sms_phone_number.length() <= 4;
        boolean isMarketNum2 = resume.sms_phone_number.startsWith("+505") && resume.sms_phone_number.replaceAll("\\s+","").trim().length() <= 8;

        if(!(isMarketNum || isMarketNum2) && isNumber) {
            areaCode = Utils.getOperadoraV4(resume.sms_phone_number, holder.itemView.getContext());
            if (areaCode != null) {
               holder.lblSmsOperator.setText(areaCode.area_operator);
                switch (areaCode.area_operator) {
                    case CLARO:
                        holder.lblSmsOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_red_700));
                        holder.avatar_operator.setImageResource(R.drawable.ic_user_red);
                        break;
                    case MOVISTAR:
                        holder.lblSmsOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_green_700));
                        holder.avatar_operator.setImageResource(R.drawable.ic_user_green);
                        break;
                    case COOTEL:
                        holder.lblSmsOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_amber_700));
                        holder.avatar_operator.setImageResource(R.drawable.ic_user_orange);
                        break;
                    case CONVENCIONAL:
                        holder.lblSmsOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_blue_700));
                        holder.lblSmsOperator.setText(areaCode.area_name);
                        holder.avatar_operator.setImageResource(R.drawable.ic_user_blue);
                        break;
                    case INTERNACIONAL:
                        holder.lblSmsOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_purple_700));
                        holder.lblSmsOperator.setText(areaCode.country_name+"-"+areaCode.area_name);
                        holder.avatar_operator.setImageResource(R.drawable.ic_user_purple);
                        break;
                    default:
                        holder.lblSmsOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_grey_700));
                        holder.avatar_operator.setImageResource(R.drawable.ic_user_unkown);
                        break;
                }
                //else
                //holder.txtOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_blue_grey_400));
            } else {
                holder.lblSmsOperator.setText("");
                holder.avatar_operator.setImageResource(R.drawable.ic_user_unkown);
            }
        }else {
            holder.lblSmsOperator.setText("");
            holder.avatar_operator.setImageResource(R.drawable.ic_user_unkown);
        }

        holder.smsBlCardviewRow.setOnClickListener(v -> {
            Intent myIntent = new Intent(v.getContext(), DetailConversation.class);
            String operador = DESCONOCIDO;
            AreaCodeRealm op = Utils.getOperadoraV4(resume.sms_phone_number, v.getContext());
            if(op!=null)
                operador = op.area_operator;

            myIntent.putExtra("nombre", name);
            myIntent.putExtra("numero", resume.sms_phone_number);
            myIntent.putExtra("operador", operador);
            v.getContext().startActivity(myIntent);
        });

        holder.smsBlCardviewRow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new MaterialDialog.Builder(v.getContext())
                        .title(name)
                        .items(R.array.opciones_sms)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, final View view, int which, CharSequence text) {

                                switch (which) {
                                    case 0:
                                        String uri = "tel:" + resume.sms_phone_number.replaceAll("\\s+", "");
                                        Intent intent = new Intent(Intent.ACTION_CALL);
                                        intent.setData(Uri.parse(uri));
                                        try {
                                            view.getContext().startActivity(intent);
                                        }catch (Exception ignored){}
                                        break;
                                    case 1:
                                        Intent intent2 = new Intent(Intent.ACTION_SENDTO,
                                                Uri.parse("smsto:" + resume.sms_phone_number.replaceAll("\\s+", "")));
                                        intent2.putExtra("sms_body", "");
                                        view.getContext().startActivity(intent2);
                                        break;
                                    case 2:
                                        Realm realm = Realm.getDefaultInstance();
                                        int op = BLOCK_NONE;
                                        BlackList entry = realm.where(BlackList.class).equalTo("phone_number", resume.sms_phone_number).findFirst();
                                        if(entry!=null){
                                            if(entry.block_incoming_call && entry.block_incoming_sms)
                                                op = Constans.BLOCK_BOTH;
                                            else if(entry.block_incoming_call && !entry.block_incoming_sms)
                                                op = Constans.BLOCK_CALLS;
                                            else if(!entry.block_incoming_call && entry.block_incoming_sms)
                                                op = Constans.BLOCK_MESSAGES;

                                        }
                                        realm.close();
                                        new MaterialDialog.Builder(view.getContext())
                                                .title(R.string.ac_block)
                                                .items(R.array.block_options)
                                                .itemsCallbackSingleChoice(op, new MaterialDialog.ListCallbackSingleChoice() {
                                                    @Override
                                                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                        presenter.sendToBlackList(which, resume.sms_phone_number);
                                                        return true;
                                                    }
                                                })
                                                .positiveText(R.string.accept)
                                                .negativeText(R.string.cancelar)
                                                .show();
                                        break;
                                    case 3:
                                        if(Utils.isDefaultSmsApp(view.getContext())) {
                                            new MaterialDialog.Builder(view.getContext())
                                                    .title(R.string.notice)
                                                    .content(R.string.delete_sms_content)
                                                    .positiveText(R.string.accept)
                                                    .negativeText(R.string.cancelar)
                                                    .onPositive((dialog1, which1) -> {
                                                        presenter.clearContactReacords(resume.sms_phone_number);
                                                    })
                                                    .show();
                                        }else {
                                            new MaterialDialog.Builder(view.getContext())
                                                    .title(R.string.notice)
                                                    .content(R.string.sms_warn1)
                                                    .positiveText(R.string.accept)
                                                    .show();
                                        }
                                        break;
                                }
                            }
                        })
                        .show();
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        if (messages != null) {
            return messages.size();
        } else {
            return 0;
        }
    }

}
