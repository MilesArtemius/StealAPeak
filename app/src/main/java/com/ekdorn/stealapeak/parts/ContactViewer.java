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

import com.ekdorn.stealapeak.managers.CryptoManager;
import com.ekdorn.stealapeak.services.MessagingService;
import com.ekdorn.stealapeak.R;
import com.ekdorn.stealapeak.database.AppDatabase;
import com.ekdorn.stealapeak.database.Message;
import com.ekdorn.stealapeak.database.MessageViewModel;
import com.ekdorn.stealapeak.managers.Console;
import com.ekdorn.stealapeak.managers.NotificationsManager;

import java.util.ArrayList;
import java.util.List;

public class ContactViewer extends AppCompatActivity {
    public static final String PHONE        = "phone";

    private String phone;
    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.inner_contactviewer);
        this.setFinishOnTouchOutside(true);

        phone = getIntent().getStringExtra(PHONE);
        initData();

        Toolbar toolbar = (Toolbar) findViewById(R.id.dialog_toolbar);
        Button sendButton = (Button) findViewById(R.id.send_button);
        final EditText sendText = (EditText) findViewById(R.id.send_text);

        findViewById(R.id.message_view).setVisibility(View.GONE);
        findViewById(R.id.recycler_view).setVisibility(View.VISIBLE);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(messagesAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.scrollToPosition(messageList.size() - 1);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = sendText.getText().toString();
                String key = AppDatabase.getDatabase(ContactViewer.this).contactDao().getContact(phone).getKey();
                String transfer = CryptoManager.encode(text, key);
                Console.sendMessage(phone, transfer, MessagingService.TYPE_FIELD_DATA, ContactViewer.this);

                sendText.setText("");
                recyclerView.scrollToPosition(messageList.size() - 1);
                Message message = new Message(phone, true, System.currentTimeMillis(), text);
                NotificationsManager.activeNotification(ContactViewer.this, phone, message);
            }
        });

        toolbar.setTitle(AppDatabase.getDatabase(this).contactDao().getContact(phone).getName());

        setSupportActionBar(toolbar);
    }

    private void initData() {
        MessageViewModel MVM = ViewModelProviders.of(this).get(MessageViewModel.class);
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
