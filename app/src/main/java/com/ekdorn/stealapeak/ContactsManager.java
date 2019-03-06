package com.ekdorn.stealapeak;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.Toast;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class ContactsManager implements NavigationView.OnNavigationItemSelectedListener {
    private static ContactsManager manager;
    private Menu contactsList;
    private WeakReference<StealAPeak> contextHolder;
    private OnSelected selected;

    private ContactsManager(@Nullable Menu menu, @Nullable OnSelected selected, WeakReference<StealAPeak> holder) {
        if (menu != null) this.contactsList = menu;
        if (selected != null) this.selected = selected;
        if (holder != null) this.contextHolder = holder;
    }

    public static ContactsManager create(Menu menu, OnSelected selected, WeakReference<StealAPeak> holder) {
        if (manager != null) {
            return manager;
        } else {
            manager = new ContactsManager(menu, selected, holder);
            return manager;
        }
    }

    public static ContactsManager get() {
        if (manager != null) {
            return manager;
        } else {
            manager = new ContactsManager(null, null, null);
            return manager;
        }
    }

    public void addItem(final String phone) {
        if (this.contactsList != null) {
            MenuItem item = this.contactsList.add(R.id.main_group, Menu.NONE, Menu.FIRST, phone);
            item.setTitleCondensed(phone);
            item.setActionView(new CheckBox(contextHolder.get()));
            item.getActionView().setClickable(false);
            if (PrefManager.get(contextHolder.get()).getUser(phone).isNotificationOpened()) {
                ((CheckBox) item.getActionView()).setChecked(true);
            }
        } else {
            Log.e("TAG", "called outside app" );
        }
    }

    private void removeItem(String phone) {
        if (this.contactsList != null) {
            int itemN = findByPhone(phone);
            if (itemN != -1) {
                MenuItem item = this.contactsList.getItem(itemN);
                if (((CheckBox) item.getActionView()).isChecked()) {
                    NotificationsManager.dismissDialogNotification(contextHolder.get(), phone);
                }
                this.contactsList.removeItem(item.getItemId());
            }
        } else {
            Log.e("TAG", "called outside app" );
        }
    }



    public void addContact(User user, String phone, Context context) {
        if (!PrefManager.get(context).isUser(phone)) {
            PrefManager.get(context).setUser(phone, user);
            addItem(phone);
            selected.added();
        } else {
            Toast.makeText(contextHolder.get(), "Already in contacts!", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeContact(String phone, Context context) {
        PrefManager.get(context).deleteUser(phone);
        removeItem(phone);
    }



    private int findByPhone(String phone) {
        for (int i = 0; i < this.contactsList.size(); i++) {
            MenuItem item = this.contactsList.getItem(i);
            if (item.getTitleCondensed().equals(phone)) {
                return i;
            }
        }
        return -1;
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getGroupId() == R.id.main_group) {
            String phone = item.getTitleCondensed().toString();

            if (((Switch) this.contactsList.findItem(R.id.app_bar_switch).getActionView().findViewById(R.id.switcher)).isChecked()) {
                removeContact(phone, contextHolder.get());
            } else {
                ((CheckBox) item.getActionView()).toggle();

                if (((CheckBox) item.getActionView()).isChecked()) {
                    NotificationsManager.addToDialogNotification(contextHolder.get(), phone, null);
                    this.selected.selected();
                } else {
                    NotificationsManager.dismissDialogNotification(contextHolder.get(), phone);
                }
            }
        }
        return true;
    }

    public interface OnSelected {
        void selected();
        void added();
    }
}
