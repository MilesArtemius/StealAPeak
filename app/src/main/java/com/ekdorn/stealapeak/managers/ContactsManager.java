package com.ekdorn.stealapeak.managers;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.Toast;

import com.ekdorn.stealapeak.R;
import com.ekdorn.stealapeak.StealAPeak;
import com.ekdorn.stealapeak.database.Contact;
import com.ekdorn.stealapeak.database.ContactViewModel;
import com.ekdorn.stealapeak.services.MessagingService;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

public class ContactsManager implements NavigationView.OnNavigationItemSelectedListener {
    private static ContactsManager manager;
    private Menu contactsList;
    private WeakReference<StealAPeak> contextHolder;
    private ContactViewModel CVM;
    private OnSelected selected;

    private ContactsManager(@Nullable Menu menu, @Nullable OnSelected selected, WeakReference<StealAPeak> holder) {
        if (menu != null) this.contactsList = menu;
        if (selected != null) this.selected = selected;
        if (holder != null) {
            this.contextHolder = holder;
            initData(holder.get());
        }
    }

    public static ContactsManager create(Menu menu, OnSelected selected, WeakReference<StealAPeak> holder) {
        manager = new ContactsManager(menu, selected, holder);
        return manager;
    }

    public static ContactsManager get() {
        if (manager != null) {
            return manager;
        } else {
            manager = new ContactsManager(null, null, null);
            return manager;
        }
    }

    private void initData(StealAPeak sap) {
        CVM = ViewModelProviders.of(sap).get(ContactViewModel.class);
        CVM.getContactsList().observe(sap, new Observer<List<Contact>>() {
            @Override
            public void onChanged(@Nullable List<Contact> contacts) {
                if ((ContactsManager.this.contactsList != null) && (contacts != null))
                    ContactsManager.this.contactsList.removeGroup(R.id.main_group);
                for (int i = 0; i < contacts.size(); i++) {
                    addItem(contacts.get(i).getPhone());
                }
            }
        });
    }

    private void addItem(final String phone) {
        if (this.contactsList != null) {
            MenuItem item = this.contactsList.add(R.id.main_group, Menu.NONE, Menu.FIRST, phone);
            item.setTitleCondensed(phone);
            item.setActionView(new CheckBox(contextHolder.get()));
            item.getActionView().setClickable(false);
            if (CVM.getContact(phone).isActive()) {
                ((CheckBox) item.getActionView()).setChecked(true);
            }
        } else {
            Log.e("TAG", "called outside app" );
        }
    }



    public void addContact(Contact contact, String phone, Context context) {
        if (!CVM.isContact(phone)) {
            CVM.setContact(contact);
            Console.sendMessage(phone, MessagingService.SERVICE_CONTACT_CR, MessagingService.TYPE_FIELD_SERVICE, context);
            selected.added();
        } else {
            Toast.makeText(contextHolder.get(), "Already in contacts!", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeContact(String phone) {
        if (CVM.getContact(phone).isActive()) {
            NotificationsManager.dismissDialogNotification(contextHolder.get(), phone);
        }
        CVM.deleteContact(phone);
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getGroupId() == R.id.main_group) {
            String phone = item.getTitleCondensed().toString();

            if (((Switch) this.contactsList.findItem(R.id.app_bar_switch).getActionView().findViewById(R.id.switcher)).isChecked()) {
                removeContact(phone);
            } else {
                ((CheckBox) item.getActionView()).toggle();

                if (((CheckBox) item.getActionView()).isChecked()) {
                    NotificationsManager.activeNotification(contextHolder.get(), phone, null);
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
