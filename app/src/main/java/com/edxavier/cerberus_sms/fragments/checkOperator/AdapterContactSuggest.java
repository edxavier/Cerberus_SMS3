package com.edxavier.cerberus_sms.fragments.checkOperator;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.edxavier.cerberus_sms.R;
import com.edxavier.cerberus_sms.db.realm.AreaCodeRealm;
import com.edxavier.cerberus_sms.db.realm.ContactRealm;
import com.edxavier.cerberus_sms.fragments.checkOperator.contracts.CheckOperatorView;
import com.edxavier.cerberus_sms.fragments.contacts.contracts.ContactsPresenter;
import com.edxavier.cerberus_sms.fragments.contacts.contracts.ContactsView;
import com.edxavier.cerberus_sms.helpers.Constans;
import com.edxavier.cerberus_sms.helpers.TextViewHelper;
import com.edxavier.cerberus_sms.helpers.Utils;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Eder Xavier Rojas on 12/07/2016.
 */
public class AdapterContactSuggest extends RecyclerView.Adapter<AdapterContactSuggest.ViewHolder> implements RealmChangeListener<RealmResults<ContactRealm>> {
    RealmResults<ContactRealm> contactRealms = null;
    Activity activity;
    CheckOperatorView operatorView;

    public AdapterContactSuggest(RealmResults<ContactRealm> list, Activity activity,  CheckOperatorView view) {
        this.activity = activity;
        this.contactRealms = list;
        this.operatorView = view;
        this.contactRealms.addChangeListener(this);
    }

    @Override
    public void onChange(RealmResults<ContactRealm> element) {
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.contact_avatar)
        AppCompatImageView contactAvatar;
        @BindView(R.id.lbl_contact_name)
        TextViewHelper lblContactName;
        @BindView(R.id.lbl_contact_number)
        TextViewHelper lblContactNumber;
        @BindView(R.id.lbl_contact_country)
        TextViewHelper lblContactCountry;
        @BindView(R.id.lbl_contact_operator)
        TextViewHelper lblContactOperator;
        @BindView(R.id.sms_bl_cardviewRow)
        CardView smsBlCardviewRow;
        @BindView(R.id.profile_image)
        CircleImageView profile_image;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_contacts_suggest, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ContactRealm contact = contactRealms.get(position);

        holder.lblContactName.setRobotoRegular();
        holder.lblContactOperator.setRobotoRegular();
        AreaCodeRealm areaCode = Utils.getOperadoraV4(contact.contact_phone_number, holder.itemView.getContext());
        if (areaCode != null) {
            holder.lblContactOperator.setText(contact.contact_operator);
            switch (areaCode.area_operator) {
                case Constans.CLARO:
                    holder.lblContactOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_red_600));
                    holder.contactAvatar.setImageResource(R.drawable.ic_user_red);
                    break;
                case Constans.MOVISTAR:
                    holder.contactAvatar.setImageResource(R.drawable.ic_user_green);
                    holder.lblContactOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_green_600));
                    break;
                case Constans.COOTEL:
                    holder.contactAvatar.setImageResource(R.drawable.ic_user_orange);
                    holder.lblContactOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_orange_700));
                    break;
                case Constans.CONVENCIONAL:
                    holder.contactAvatar.setImageResource(R.drawable.ic_user_blue);
                    holder.lblContactOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_blue_700));
                    break;
                case Constans.INTERNACIONAL:
                    holder.contactAvatar.setImageResource(R.drawable.ic_user_purple);
                    holder.lblContactOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_purple_700));
                    break;
                default:
                    holder.contactAvatar.setImageResource(R.drawable.ic_user_unkown);
                    break;
            }

            if (areaCode.area_name.length() > 0) {
                holder.lblContactCountry.setText(areaCode.area_name + ", " + areaCode.country_name);
                //holder.contactFlagImg.setImageDrawable(holder.itemView.getResources().getDrawable(areaCode.getFlag()));
            } else if (contact.contact_phone_number.replaceAll("\\s+", "").length() >= 8) {
                holder.lblContactCountry.setText(areaCode.country_name);
                //holder.contactFlagImg.setImageDrawable(holder.itemView.getResources().getDrawable(areaCode.getFlag()));
            } else {
                holder.lblContactCountry.setText("");
            }
        } else {
            holder.lblContactCountry.setText("");
            holder.lblContactOperator.setTextColor(holder.itemView.getResources().getColor(R.color.md_grey_700));
            holder.lblContactOperator.setText(contact.contact_operator);
            holder.contactAvatar.setImageResource(R.drawable.ic_user_unkown);
        }
        holder.lblContactName.setText(contact.contact_name);
        holder.lblContactNumber.setText(contact.contact_phone_number);

        holder.smsBlCardviewRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                //showPopupMenu(holder.lblContactName, contact, holder.smsBlCardviewRow);
                operatorView.setNumber(contact.contact_phone_number);
            }
        });
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(final View view, final ContactRealm contact, final View card) {
        // inflate menu
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.contact_menu, popup.getMenu());
        // Force icons to show
        Object menuHelper;
        Class[] argTypes;
        card.setAlpha((float) 0.6);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_call_contact:
                        String uri = "tel:" + contact.contact_phone_number.replaceAll("\\s+", "");
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse(uri));
                        if (ActivityCompat.checkSelfPermission(view.getContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            activity.startActivity(intent);
                            card.setAlpha((float) 1);
                            return true;
                        }
                        return true;
                    case R.id.action_write_sms:
                        Intent intent2 = new Intent(Intent.ACTION_SENDTO,
                                Uri.parse("smsto:" + contact.contact_phone_number.replaceAll("\\s+", "")));
                        intent2.putExtra("sms_body", "");
                        view.getContext().startActivity(intent2);
                        card.setAlpha((float) 1);
                        return true;
                    case R.id.action_share_contact:
                        try
                        {
                            AreaCodeRealm res = Utils.getOperadoraV4(contact.contact_phone_number, view.getContext());
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("text/plain");
                            i.putExtra(Intent.EXTRA_SUBJECT, "Contacto");
                            String sAux = contact.contact_name+" \n";
                            sAux = sAux + contact.contact_phone_number+"\n";
                            if(res!=null)
                                sAux = sAux + res.area_operator + "\n";
                            i.putExtra(Intent.EXTRA_TEXT, sAux);
                            view.getContext().startActivity(Intent.createChooser(i,
                                    view.getContext().getResources().getString(R.string.share_using)));
                        }
                        catch(Exception ignored) {}
                        return true;
                }
                return false;
            }
        });

        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                card.setAlpha((float) 1);
            }
        });

        try {
            Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
            fMenuHelper.setAccessible(true);
            menuHelper = fMenuHelper.get(popup);
            argTypes = new Class[]{boolean.class};
            menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
            popup.show();
        } catch (Exception e) {
            popup.show();
        }

    }

    @Override
    public int getItemCount() {
        try {
            if (contactRealms != null) {
                return contactRealms.size();
            } else {
                return 0;
            }
        }catch (Exception ignored){
            return 0;
        }
    }

}
