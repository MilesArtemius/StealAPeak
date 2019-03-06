package com.ekdorn.stealapeak;

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

public class UserSearchFragment extends Fragment {
    EditText phone;
    TableRow onSearchedContainer;
    LinearLayout userBox;
    Button findButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        final View root = inflater.inflate(R.layout.inner_fragment_stealapeak, container, false);

        phone = (EditText) root.findViewById(R.id.phone);
        onSearchedContainer = (TableRow) root.findViewById(R.id.on_searched_container);
        userBox = (LinearLayout) root.findViewById(R.id.user_box);
        findButton = (Button) root.findViewById(R.id.find_button);

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String number = phone.getText().toString();
                findButton.setEnabled(false);
                phone.setEnabled(false);

                Console.getUserByPhone(number, new Console.OnLoaded() {
                    @Override
                    public void onGot(final User user, boolean successful) {
                        if (successful) {
                            swotch(false);

                            ImageView userAvater = (ImageView) root.findViewById(R.id.user_avatar);
                            TextView userName = (TextView) root.findViewById(R.id.user_name);
                            TextView userPhone = (TextView) root.findViewById(R.id.user_phone);

                            //userAvater
                            userName.setText(user.getName());
                            userPhone.setText(number);

                            final Button dialogButton = (Button) root.findViewById(R.id.dialog_button);
                            dialogButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Console.sendMessage(number, "test", "DATA");
                                    swotch(true);
                                }
                            });

                            final Button contactsButton = (Button) root.findViewById(R.id.contacts_button);
                            contactsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ContactsManager.get().addContact(user, number, UserSearchFragment.this.getActivity());
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
