package com.thelinkphone.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thelinkphone.app.adapter.AdapterBlock;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.utils.MyShare;

import java.util.ArrayList;

/**
 * Activity for managing blocked phone numbers
 *
 * Features:
 * - Add numbers manually by typing
 * - Select contacts from phonebook
 * - View and manage existing blocked numbers
 * - Numbers are integrated with MyCallScreeningService for automatic call blocking
 */
public class CallBlockActivity extends AppCompatActivity implements AdapterBlock.OnItemBlockClick {

    private static final String TAG = "CallBlockActivity";
    private static final int PICK_CONTACT_REQUEST = 1001;

    private RecyclerView recyclerViewBlocked;
    private EditText etPhoneNumber;
    private Button btnAddNumber, btnSelectContact;
    private View tvEmptyMessage;
    private TextView tvBlockedCount;
    private ImageView btnBack;
    private AdapterBlock adapterBlock;
    private ArrayList<ItemContact> blockedNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_block);

        // Apply theme
        applyTheme();

        initViews();
        loadBlockedNumbers();
        setupClickListeners();
        setupRecyclerView();
        updateUI();
    }

    private void applyTheme() {
        // Theme is handled by the layout XML and app theme
        // No need for manual theme application here
    }

    private void initViews() {
        try {
            recyclerViewBlocked = findViewById(R.id.recyclerViewBlocked);
            etPhoneNumber = findViewById(R.id.etPhoneNumber);
            btnAddNumber = findViewById(R.id.btnAddNumber);
            btnSelectContact = findViewById(R.id.btnSelectContact);
            tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
            tvBlockedCount = findViewById(R.id.tvBlockedCount);
            btnBack = findViewById(R.id.btnBack);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadBlockedNumbers() {
        blockedNumbers = MyShare.getArrBlock(this);
        if (blockedNumbers == null) {
            blockedNumbers = new ArrayList<>();
        }
        Log.d(TAG, "Loaded " + blockedNumbers.size() + " blocked numbers");
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddNumber.setOnClickListener(v -> addNumberToBlockList());

        btnSelectContact.setOnClickListener(v -> openContactPicker());

        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnAddNumber.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        boolean theme = MyShare.getTheme(this);
        adapterBlock = new AdapterBlock(blockedNumbers, theme, this);
        recyclerViewBlocked.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBlocked.setAdapter(adapterBlock);
    }

    private void addNumberToBlockList() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clean and validate phone number format
        phoneNumber = phoneNumber.replaceAll("[^0-9+]", "");
        if (phoneNumber.length() < 3) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if number is already blocked
        for (ItemContact contact : blockedNumbers) {
            String contactNumber = getPhoneNumber(contact);
            if (!contactNumber.isEmpty() && PhoneNumberUtils.compare(contactNumber, phoneNumber)) {
                Toast.makeText(this, "Number is already blocked", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Add new blocked number
        ItemContact newBlockedContact = new ItemContact(phoneNumber);
        newBlockedContact.setName("Manual Block");

        blockedNumbers.add(newBlockedContact);
        Log.d(TAG, "Added blocked number: " + phoneNumber + ", Total count: " + blockedNumbers.size());
        adapterBlock.refreshFilter(); // Refresh the filter list to sync display
        updateUI();
        saveBlockedNumbers(); // Save immediately after adding

        etPhoneNumber.setText("");
        Toast.makeText(this, "Number blocked successfully", Toast.LENGTH_SHORT).show();
    }

    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                addContactToBlockList(contactUri);
            }
        }
    }

    private void addContactToBlockList(Uri contactUri) {
        String[] projection = {
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                if (phoneNumber != null) {
                    phoneNumber = phoneNumber.replaceAll("[^0-9+]", ""); // Clean phone number

                    // Check if number is already blocked
                    for (ItemContact contact : blockedNumbers) {
                        String contactNumber = getPhoneNumber(contact);
                        if (!contactNumber.isEmpty() && PhoneNumberUtils.compare(contactNumber, phoneNumber)) {
                            Toast.makeText(this, "Contact is already blocked", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // Add contact to block list
                    ItemContact newBlockedContact = new ItemContact(phoneNumber);
                    newBlockedContact.setName(name != null ? name : "Unknown");

                    blockedNumbers.add(newBlockedContact);
                    Log.d(TAG, "Added contact to block list: " + phoneNumber + ", Total count: " + blockedNumbers.size());
                    adapterBlock.refreshFilter(); // Refresh the filter list to sync display
                    updateUI();
                    saveBlockedNumbers(); // Save immediately after adding

                    Toast.makeText(this, "Contact added to block list", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error adding contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBlockedNumbers() {
        try {
            if (blockedNumbers != null) {
                MyShare.putBlockNumber(this, blockedNumbers);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving blocked numbers", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(ItemContact itemContact) {
        // Handle unblocking number - called when user taps delete icon
        String numberToUnblock = getPhoneNumber(itemContact);

        // Remove from main list
        boolean removed = blockedNumbers.remove(itemContact);
        if (removed) {
            Log.d(TAG, "Removed blocked number: " + numberToUnblock + ", Remaining count: " + blockedNumbers.size());
            adapterBlock.refreshFilter(); // Refresh the filter list to sync display
            updateUI();
            saveBlockedNumbers(); // Save immediately after removing
            Toast.makeText(this,
                "Unblocked: " + (numberToUnblock.isEmpty() ? "Unknown" : numberToUnblock),
                Toast.LENGTH_SHORT).show();
        } else {
            Log.w(TAG, "Failed to remove blocked number: " + numberToUnblock);
        }
    }

    private void updateUI() {
        Log.d(TAG, "Updating UI - blocked numbers count: " + blockedNumbers.size());
        if (blockedNumbers.isEmpty()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerViewBlocked.setVisibility(View.GONE);
            tvBlockedCount.setText("No blocked numbers");
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            recyclerViewBlocked.setVisibility(View.VISIBLE);
            tvBlockedCount.setText(blockedNumbers.size() + " blocked number" + (blockedNumbers.size() == 1 ? "" : "s"));
        }

        // Log adapter state for debugging
        if (adapterBlock != null) {
            Log.d(TAG, "Adapter item count: " + adapterBlock.getItemCount());
        }
    }

    /**
     * Helper method to safely get phone number from ItemContact
     */
    private String getPhoneNumber(ItemContact contact) {
        if (contact != null && !contact.getArrPhone().isEmpty()) {
            return contact.getArrPhone().get(0).getNumber();
        }
        return "";
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save blocked numbers when leaving the activity
        saveBlockedNumbers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Final save to ensure data persistence
        saveBlockedNumbers();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        // Save data before going back
        saveBlockedNumbers();
        super.onBackPressed();
    }
}
