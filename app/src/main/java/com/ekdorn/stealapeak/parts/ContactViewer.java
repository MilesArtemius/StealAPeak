package com.ekdorn.stealapeak.parts;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.ekdorn.stealapeak.services.MessagingService;
import com.ekdorn.stealapeak.R;
import com.ekdorn.stealapeak.database.AppDatabase;
import com.ekdorn.stealapeak.database.Contact;
import com.ekdorn.stealapeak.database.Message;
import com.ekdorn.stealapeak.database.MessageViewModel;
import com.ekdorn.stealapeak.managers.Console;
import com.ekdorn.stealapeak.managers.NotificationsManager;
import com.ekdorn.stealapeak.managers.PrefManager;

import java.util.ArrayList;
import java.util.List;

public class ContactViewer extends AppCompatActivity {
    public static final String PHONE        = "phone";
    public static final String DATA_KEY     = "data";

    private Button sendButton;
    private EditText sendText;
    private Toolbar toolbar;

    private Contact dialogist;
    private String phone;
    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter messagesAdapter;
    private MessageViewModel MVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.inner_contactviewer);
        this.setFinishOnTouchOutside(true);

        phone = getIntent().getStringExtra(PHONE);
        initData();

        toolbar = (Toolbar) findViewById(R.id.dialog_toolbar);
        sendButton = (Button) findViewById(R.id.send_button);
        sendText = (EditText) findViewById(R.id.send_text);

        if (AppDatabase.getDatabase(this).contactDao().isContact(phone)) {
            dialogist = AppDatabase.getDatabase(this).contactDao().getContact(phone);
            loadAsDialog();
        } else if (getIntent().hasExtra(DATA_KEY)) {
            Console.getUserByPhone(phone, new Console.OnLoaded() {
                @Override
                public void onGot(Contact contact, boolean successful) {
                    if (successful) {
                        loadAsMessage(contact);
                    }
                }
            });
        } else {
            loadAnonymously(phone);
        }

        setSupportActionBar(toolbar);
    }

    private void initData() {
        MVM = ViewModelProviders.of(this).get(MessageViewModel.class);
        MVM.getMessagesList(phone).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                messageList.clear();
                messageList.addAll(messages);
                RecyclerView recyclerView = findViewById(R.id.recycler_view);
                if (messagesAdapter == null) {
                    messagesAdapter = new MessageAdapter(messageList);
                    recyclerView.setAdapter(messagesAdapter);
                } else {
                    messagesAdapter.notifyDataSetChanged();
                }
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });
    }

    private void loadAsMessage(Contact contact) {
        findViewById(R.id.message_view).setVisibility(View.VISIBLE);
        findViewById(R.id.recycler_view).setVisibility(View.GONE);

        TextView textView = findViewById(R.id.text_view);
        TextView timeView = findViewById(R.id.time_view);

        Message current = (Message) getIntent().getSerializableExtra(DATA_KEY);
        textView.setText(current.getText());
        timeView.setText(String.valueOf(current.getTime()));

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message(phone, true, System.currentTimeMillis(), sendText.getText().toString());
                Console.sendMessage(message , MessagingService.TYPE_FIELD_DATA, ContactViewer.this);
                sendText.setText("");
                finish();
            }
        });

        toolbar.setTitle(contact.getName());
    }

    private void loadAsDialog() {
        findViewById(R.id.message_view).setVisibility(View.GONE);
        findViewById(R.id.recycler_view).setVisibility(View.VISIBLE);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(messagesAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.scrollToPosition(messageList.size() - 1);

        PrefManager.get(this).nullNotifications(phone);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message(phone, true, System.currentTimeMillis(), sendText.getText().toString());
                Console.sendMessage(message, MessagingService.TYPE_FIELD_DATA, ContactViewer.this);
                sendText.setText("");
                recyclerView.scrollToPosition(messageList.size() - 1);
                NotificationsManager.activeNotification(ContactViewer.this, phone, message);
            }
        });

        toolbar.setTitle(dialogist.getName());
    }

    private void loadAnonymously(String phone) {
        findViewById(R.id.message_view).setVisibility(View.GONE);
        findViewById(R.id.recycler_view).setVisibility(View.GONE);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message(ContactViewer.this.phone, true, System.currentTimeMillis(), sendText.getText().toString());
                Console.sendMessage(message, MessagingService.TYPE_FIELD_DATA, ContactViewer.this);
                sendText.setText("");
                finish();
            }
        });

        toolbar.setTitle(phone);
    }



    private class MessageAdapter extends RecyclerView.Adapter<MessageHolder> {
        private List<Message> messages;

        public MessageAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
            View view = layoutInflater.inflate(R.layout.inner_itemmessage, parent, false);
            return new MessageHolder(view);
        }

        @Override
        public void onBindViewHolder(MessageHolder holder, int position) {
            Message message = this.messages.get(position);
            holder.bindMessage(message);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }

    private class MessageHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener*/ {
        private LinearLayout messageView;
        private TextView textView;
        private TextView timeView;
        private Space mySpace;
        private Space otherSpace;

        private MessageHolder(View itemView) {
            super(itemView);
            this.messageView = (LinearLayout) itemView.findViewById(R.id.message_view);
            this.textView = (TextView) itemView.findViewById(R.id.text_view);
            this.timeView = (TextView) itemView.findViewById(R.id.time_view);
            this.mySpace = (Space) itemView.findViewById(R.id.my_space);
            this.otherSpace = (Space) itemView.findViewById(R.id.other_space);
        }

        private void bindMessage(Message message) {
            messagePosToggle(message.getMyMessage());

            this.textView.setText(message.getText());
            this.timeView.setText(String.valueOf(message.getTime()));
        }

        private void messagePosToggle(boolean myMessage) {
            int gravity = myMessage ? Gravity.END : Gravity.START;
            this.messageView.setGravity(gravity);
            this.textView.setGravity(gravity);
            this.timeView.setGravity(gravity);

            this.mySpace.setVisibility(myMessage ? View.VISIBLE : View.GONE);
            this.otherSpace.setVisibility(myMessage ? View.GONE : View.VISIBLE);
        }
    }
}
