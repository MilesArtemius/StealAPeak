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
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        final View root = inflater.inflate(R.layout.inner_fragment_stealapeak, container, false);

        final EditText phone = (EditText) root.findViewById(R.id.phone);
        final TableRow onSearchedContainer = (TableRow) root.findViewById(R.id.on_searched_container);
        final LinearLayout userBox = (LinearLayout) root.findViewById(R.id.user_box);
        final Button findButton = (Button) root.findViewById(R.id.find_button);

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
                            findButton.setEnabled(true);
                            phone.setEnabled(true);

                            phone.setVisibility(View.GONE);
                            findButton.setVisibility(View.GONE);
                            onSearchedContainer.setVisibility(View.VISIBLE);
                            userBox.setVisibility(View.VISIBLE);

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

                                }
                            });

                            final Button contactsButton = (Button) root.findViewById(R.id.contacts_button);
                            contactsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ContactsManager.get().addContact(user, number, UserSearchFragment.this.getActivity());
                                }
                            });
                        } else {
                            findButton.setEnabled(true);
                            phone.setEnabled(true);

                            phone.setText("");
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
}
