package com.ekdorn.stealapeak.parts;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ekdorn.stealapeak.R;
import com.ekdorn.stealapeak.database.Contact;
import com.ekdorn.stealapeak.managers.Console;
import com.ekdorn.stealapeak.managers.ContactsManager;
import com.ekdorn.stealapeak.managers.NotificationsManager;
import com.google.firebase.auth.FirebaseAuth;

public class UserSearchFragment extends Fragment {
    EditText phone;
    TableRow onSearchedContainer;
    LinearLayout userBox;
    Button findButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        final View root = inflater.inflate(R.layout.inner_fragment_usersearchfragment, container, false);

        phone = (EditText) root.findViewById(R.id.phone);
        onSearchedContainer = (TableRow) root.findViewById(R.id.on_searched_container);
        userBox = (LinearLayout) root.findViewById(R.id.user_box);
        findButton = (Button) root.findViewById(R.id.find_button);

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String number = phone.getText().toString();
                String myPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                if (!number.matches(LoginActivity.PHONE_MASK)) {
                    phone.setError("Doesn't looks like phone number...");
                    return;
                } else if (number.equals(myPhone)) {
                    phone.setError("This is your phone. Nice to meet you.");
                    return;
                }
                findButton.setEnabled(false);
                phone.setEnabled(false);

                Console.getUserByPhone(number, new Console.OnLoaded() {
                    @Override
                    public void onGot(final Contact contact, boolean successful) {
                        if (successful) {
                            swotch(false);

                            ImageView userAvater = (ImageView) root.findViewById(R.id.user_avatar);
                            TextView userName = (TextView) root.findViewById(R.id.user_name);
                            TextView userPhone = (TextView) root.findViewById(R.id.user_phone);

                            //userAvater
                            userName.setText(contact.getName());
                            userPhone.setText(number);

                            final Button dialogButton = (Button) root.findViewById(R.id.dialog_button);
                            dialogButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // twink to user;

                                    swotch(true);
                                }
                            });

                            final Button contactsButton = (Button) root.findViewById(R.id.contacts_button);
                            contactsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ContactsManager.get().addContact(contact, number, UserSearchFragment.this.getActivity());
                                    NotificationsManager.activeNotification(UserSearchFragment.this.getActivity(), number, null);

                                    Intent intentDialog = new Intent(UserSearchFragment.this.getActivity(), ContactViewer.class);
                                    intentDialog.putExtra(ContactViewer.PHONE, contact.getPhone());
                                    intentDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intentDialog);

                                    swotch(true);
                                }
                            });
                        } else {
                            swotch(true);

                            if (number.equals("")) {
                                phone.setError("No number on input :/");
                            //} else if... {

                            } else {
                                phone.setError("No user found!");
                            }
                        }
                    }
                });
            }
        });

        return root;
    }

    private void swotch(boolean enabled) {
        findButton.setEnabled(enabled);
        phone.setEnabled(enabled);

        int startVIsibiluty = enabled ? View.VISIBLE : View.GONE;
        int endVIsibiluty = enabled ? View.GONE : View.VISIBLE;
        phone.setVisibility(startVIsibiluty);
        findButton.setVisibility(startVIsibiluty);
        onSearchedContainer.setVisibility(endVIsibiluty);
        userBox.setVisibility(endVIsibiluty);

        phone.setText("");
    }
}
