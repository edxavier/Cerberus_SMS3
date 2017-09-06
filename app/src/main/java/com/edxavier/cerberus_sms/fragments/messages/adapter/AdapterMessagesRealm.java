package com.edxavier.cerberus_sms.fragments.messages.adapter;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.cerberus_sms.CreateMessageActivity;
import com.edxavier.cerberus_sms.DetailConversation;
import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.MessagesRealm;
import com.edxavier.cerberus_sms.fragments.messages.contracts.MessagesView;
import com.edxavier.cerberus_sms.helpers.TextViewHelper;
import com.edxavier.cerberus_sms.helpers.Utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.edxavier.cerberus_sms.helpers.Constans.CLARO;
import static com.edxavier.cerberus_sms.helpers.Constans.CONVENCIONAL;
import static com.edxavier.cerberus_sms.helpers.Constans.COOTEL;
import static com.edxavier.cerberus_sms.helpers.Constans.INTERNACIONAL;
import static com.edxavier.cerberus_sms.helpers.Constans.MOVISTAR;

/**
 * Created by Eder Xavier Rojas on 12/10/2015.
 */
public class AdapterMessagesRealm extends RecyclerView.Adapter<AdapterMessagesRealm.ViewHolder>
        implements RealmChangeListener<RealmResults<MessagesRealm>> {

    private RealmResults<MessagesRealm> messages = null;
    private MessagesView view;

    public AdapterMessagesRealm(RealmResults<MessagesRealm> messages, MessagesView view) {
        this.messages = messages;
        this.view = view;
        this.messages.addChangeListener(this);
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
        if(resume.contact!=null)
            name = (resume.contact.contact_name);
        else
            name = (resume.sms_phone_number);

        holder.senderName.setText(name);
        holder.conversationSmsCount.setText("("+String.valueOf(resume.sms_count)+")");
        holder.lblSmsSenderNumber.setText(resume.sms_phone_number);
        holder.msgBody.setText(resume.sms_text);
        SimpleDateFormat time_format = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        SimpleDateFormat date_format = new SimpleDateFormat("dd-MMM-yy", Locale.getDefault());

        if (resume.sms_date != null) {
            CharSequence d = DateUtils.getRelativeTimeSpanString(resume.sms_date.getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            //holder.lblSmsDate.setText(date_format.format(d));
            holder.lblSmsDate.setText(d);
            //holder.lblSmsTime.setText(time_format.format(resume.sms_date));
        }
        boolean isNumber = Utils.isPhoneNumber(resume.sms_phone_number);
        boolean isMarketNum = !resume.sms_phone_number.startsWith("+505") && resume.sms_phone_number.length() <= 4;
        boolean isMarketNum2 = resume.sms_phone_number.startsWith("+505") && resume.sms_phone_number.replaceAll("\\s+","").trim().length() <= 8;

        if(!(isMarketNum || isMarketNum2) && isNumber) {
            AreaCodeRealm areaCode = Utils.getOperadoraV4(resume.sms_phone_number, holder.itemView.getContext());
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
            }
        }else {
            holder.lblSmsOperator.setText("");
        }

        holder.smsBlCardviewRow.setOnClickListener(v -> {
            Intent myIntent = new Intent(v.getContext(), DetailConversation.class);
            v.getContext().startActivity(myIntent);
        });

        holder.smsBlCardviewRow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new MaterialDialog.Builder(v.getContext())
                        .title(name)
                        .typeface("Roboto-Medium.ttf", "Roboto-Regular.ttf")
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
