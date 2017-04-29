package com.edxavier.cerberus_sms.fragments.callLog.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.CallLog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.CallsRealm;
import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogPresenter;
import com.edxavier.cerberus_sms.fragments.callLog.contracts.CallLogView;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.edxavier.cerberus_sms.helpers.TextViewHelper;
import com.edxavier.cerberus_sms.helpers.Utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 12/07/2016.
 */
public class AdapterCallsRealm extends RecyclerView.Adapter<AdapterCallsRealm.ViewHolder>
        implements RealmChangeListener<RealmResults<CallsRealm>> {
    private RealmResults<CallsRealm> calls;
    private Activity activity;
    private CallLogPresenter presenter;
    private CallLogView view;


    public AdapterCallsRealm( RealmResults<CallsRealm> list, Activity activity,
                              CallLogPresenter presenter, CallLogView view) {
        this.activity = activity;
        this.calls = list;
        this.presenter = presenter;
        this.view = view;
        this.calls.addChangeListener(this);
    }

    @Override
    public void onChange(RealmResults<CallsRealm> element) {
        notifyDataSetChanged();
        if(element.isEmpty())
            view.showEmptyMsg(true);
        else
            view.showEmptyMsg(false);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.lbl_caller_name)
        TextViewHelper lblCallerName;
        @BindView(R.id.lbl_caller_number)
        TextViewHelper lblCallerNumber;
        @BindView(R.id.lbl_call_datetime)
        TextViewHelper lblCallDatetime;
        @BindView(R.id.lbl_call_areaName)
        TextViewHelper lblCallAreaName;
        @BindView(R.id.lbl_call_operator)
        TextViewHelper lblCallOperator;
        @BindView(R.id.lbl_call_duration)
        TextViewHelper lblCallDuration;
        @BindView(R.id.call_type)
        ImageView callType;
        @BindView(R.id.sms_bl_cardviewRow)
        CardView smsBlCardviewRow;
        @BindView(R.id.avatar_operator)
        AppCompatImageView operator_avatar;
        @BindView(R.id.call_count)
        ImageView call_count;



        public ViewHolder(View itemView) {
            super(itemView);
                ButterKnife.bind(this, itemView);
            lblCallerNumber.setRobotoMedium();
            lblCallerName.setRobotoBold();
            lblCallDatetime.setRobotoItalic();
            lblCallAreaName.setRobotoItalic();
            lblCallOperator.setRobotoMedium();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_call_log2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        CallsRealm call = calls.get(position);
        SimpleDateFormat time_format = new SimpleDateFormat("dd-MMM-yy hh:mm a", Locale.getDefault());
        holder.lblCallDatetime.setText(time_format.format(call.call_date));
        String name;
        if(call.contact!=null)
            name = call.contact.contact_name;
        else
            name = (call.call_phone_number);
        holder.lblCallerName.setText(name);
        holder.call_count.setImageDrawable(Utils.getAvatar(String.valueOf(call.calls_count)));
        holder.lblCallerNumber.setText(call.call_phone_number);
        holder.lblCallDuration.setText(Utils.getDurationString(call.call_duration));
        if (call.call_direction == CallLog.Calls.OUTGOING_TYPE) {
            holder.lblCallerName.setTextColor(holder.itemView.getResources().getColor(R.color.md_blue_grey_700));
            holder.callType.setImageResource(R.drawable.ic_action_communication_call_made);
        } else if (call.call_direction == CallLog.Calls.INCOMING_TYPE) {
            holder.callType.setImageResource(R.drawable.ic_action_communication_call_received);
            holder.lblCallerName.setTextColor(holder.itemView.getResources().getColor(R.color.md_teal_700));
        } else if (call.call_direction == CallLog.Calls.MISSED_TYPE) {
            holder.callType.setImageResource(R.drawable.ic_action_communication_call_missed);
            holder.lblCallerName.setTextColor(holder.itemView.getResources().getColor(R.color.md_red_500));
        }

        holder.lblCallOperator.setRobotoBold();
        AreaCodeRealm areaCode = Utils.getOperadoraV4(call.call_phone_number, holder.itemView.getContext());
        if (areaCode != null && call.call_phone_number.length() > 4) {
            holder.lblCallOperator.setText(call.call_operator);
            holder.lblCallAreaName.setVisibility(View.VISIBLE);
            //holder.lblCallOperator.setVisibility(View.VISIBLE);
            holder.lblCallAreaName.setText(areaCode.country_name);

            switch (areaCode.area_operator) {
                case Constans.CLARO:
                    holder.lblCallOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_red_600));
                    holder.operator_avatar.setImageResource(R.drawable.ic_user_red);
                    break;
                case Constans.MOVISTAR:
                    holder.operator_avatar.setImageResource(R.drawable.ic_user_green);
                    holder.lblCallOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_green_600));
                    break;
                case Constans.COOTEL:
                    holder.operator_avatar.setImageResource(R.drawable.ic_user_orange);
                    holder.lblCallOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_amber_700));
                    break;
                case Constans.CONVENCIONAL:
                    holder.operator_avatar.setImageResource(R.drawable.ic_user_blue);
                    holder.lblCallOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_blue_700));
                    break;
                case Constans.INTERNACIONAL:
                    holder.operator_avatar.setImageResource(R.drawable.ic_user_purple);
                    holder.lblCallOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_purple_700));
                    break;
                default:
                    holder.operator_avatar.setImageResource(R.drawable.ic_user_unkown);
                    break;
            }

            if (areaCode.area_name.length() > 0)
                holder.lblCallAreaName.setText(areaCode.area_name + ", " + areaCode.country_name);
            else if (call.call_phone_number.replaceAll("\\s+", "").length() >= 8)
                holder.lblCallAreaName.setText(areaCode.country_name);
            else
                holder.lblCallAreaName.setText("");
        } else {
            holder.lblCallAreaName.setVisibility(View.GONE);
            holder.lblCallOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_grey_700));
            holder.lblCallOperator.setText(call.call_operator);
            holder.lblCallAreaName.setText("");
            holder.operator_avatar.setImageResource(R.drawable.ic_user_unkown);
        }

        holder.smsBlCardviewRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(v.getContext())
                        .title(name)
                        .typeface("Roboto-Medium.ttf", "Roboto-Regular.ttf")
                        .items(R.array.opciones_contacto)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, final View view, int which, CharSequence text) {

                                switch (which) {
                                    case 0:
                                        String uri = "tel:" + call.call_phone_number.replaceAll("\\s+", "");
                                        Intent intent = new Intent(Intent.ACTION_CALL);
                                        intent.setData(Uri.parse(uri));
                                        try {
                                            view.getContext().startActivity(intent);
                                        }catch (Exception ignored){}
                                        break;
                                    case 1:
                                        Intent intent2 = new Intent(Intent.ACTION_SENDTO,
                                                Uri.parse("smsto:" + call.call_phone_number.replaceAll("\\s+", "")));
                                        intent2.putExtra("sms_body", "");
                                        view.getContext().startActivity(intent2);
                                        break;

                                }
                            }
                        })
                        .show();
            }
        });


    }

    /**
     * Showing popup menu when tapping on 3 dots
     */

    @Override
    public int getItemCount() {
        if (calls != null) {
            return calls.size();
        } else {
            return 0;
        }
    }


}
