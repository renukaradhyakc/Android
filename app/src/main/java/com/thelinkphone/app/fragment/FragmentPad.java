package com.thelinkphone.app.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.thelinkphone.app.ActivityHome;
import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.LayoutKeypad;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.utils.ActionUtils;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.ReadContact;



public class FragmentPad extends BaseFragment implements LayoutKeypad.PadResult {
    private LayoutKeypad layoutKeypad;

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        getArrContact();
        if (this.layoutKeypad == null) {
            LayoutKeypad layoutKeypad = new LayoutKeypad(layoutInflater.getContext());
            this.layoutKeypad = layoutKeypad;
            layoutKeypad.setArrAllContact(this.arrAllContact);
            this.layoutKeypad.initView(MyShare.getTheme(layoutInflater.getContext()));
            this.layoutKeypad.setPadResult(this);
            if (getActivity() instanceof ActivityHome) {
                this.layoutKeypad.setNumber(((ActivityHome) getActivity()).getNumber());
            }
        }
        return this.layoutKeypad;
    }

    @Override 
    public void onResume() {
        super.onResume();
        LayoutKeypad layoutKeypad = this.layoutKeypad;
        if (layoutKeypad != null) {
            layoutKeypad.updateMode();
        }
    }

    public void checkNum() {
        LayoutKeypad layoutKeypad = this.layoutKeypad;
        if (layoutKeypad != null) {
            layoutKeypad.checkNum();
        }
    }

    @Override 
    public void onAddNewNumber(String str) {
        ActionUtils.onNewContact(this, str);
    }

    @Override 
    public void onShowInfo(ItemContact itemContact) {
        if (getActivity() instanceof ActivityHome) {
            FragmentInfo newInstance = FragmentInfo.newInstance(itemContact, R.string.contacts);
            newInstance.setContactResult(this.contactResult);
            ((ActivityHome) getActivity()).showFragment(newInstance, true);
        }
    }

    @Override 
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (getContext() == null || i2 != -1 || intent == null || intent.getData() == null) {
            return;
        }
        Cursor query = getContext().getContentResolver().query(intent.getData(), null, null, null, null);
        if (query != null && query.moveToFirst()) {
            int columnIndex = query.getColumnIndex("contact_id");
            if (columnIndex < 0) {
                columnIndex = query.getColumnIndex("_id");
            }
            ItemContact contact = ReadContact.getContact(getContext(), query.getString(columnIndex));
            if (contact != null) {
                if (getActivity() instanceof ActivityHome) {
                    ((ActivityHome) getActivity()).addNewContact(contact);
                }
                if (this.contactResult != null) {
                    this.contactResult.onContactChange();
                }
            }
        }
        if (query != null) {
            query.close();
        }
    }
}
