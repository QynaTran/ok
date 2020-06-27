package com.example.myapplication.Model;



import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchUIUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ImageActivity;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    Context context;
    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase, mMessageDatabase;
    private static final int RIGHT = 1;
    private static final int LEFT = 0;

    private static final int SECOND_MILLIS = 1000;     //1 giay
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;//1 phut
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;   //1 gio


    public MessageAdapter(Context context, List<Messages> mMessageList) {
        this.context=context;
        this.mMessageList = mMessageList;
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==RIGHT){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_message, parent, false);
            return new MessageViewHolder(v);
        }else{
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_message, parent, false);
            return new MessageViewHolder(v);
        }
    }
    

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        mAuth = FirebaseAuth.getInstance();
        final String current_user_id = mAuth.getCurrentUser().getUid();
        final Messages c = mMessageList.get(position);

        final String from_user = c.getFrom();
        final String message_type = c.getType();
        final String to_user = c.getTo();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String image= dataSnapshot.child("thump_image").getValue().toString();
                if(!from_user.equals(current_user_id))  {
                    Picasso.get().load(image).placeholder(R.drawable.icon_profile).into(holder.messageIcon);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.setIsRecyclable(false);
        //insert message
        holder.messageText.setText(c.getMessage());
        //insert time
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(c.getTime());
        String datetime = DateFormat.format("hh:mm aa", calendar).toString();
        holder.messageTime.setText(datetime);

        //click view seen
        if (position == mMessageList.size() - 1) {
            if (c.isSeen()) {
                holder.messageSeen.setText("Seen");
            }else {
                holder.messageSeen.setText("Delivered");
            }
        }else {
            holder.messageSeen.setVisibility(View.GONE);
        }


//        holder.messageText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (holder.messageSeen.getVisibility() == View.VISIBLE) {
//                    holder.messageSeen.setVisibility(View.GONE);
//                }else {
//                    holder.messageSeen.setVisibility(View.VISIBLE);
//                }
//            }
//        });

        //delete Message
        holder.messageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("Messages");

                        mMessageDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    ds.getRef().addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds1 : dataSnapshot.getChildren()) {
                                                long time = c.getTime();
                                                Query query=ds1.getRef().orderByChild("time").equalTo(time);
                                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds2 : dataSnapshot.getChildren()) {
                                                            ds2.getRef().child("message").setValue("This message was deleted...");
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ImageActivity.class);
                i.putExtra("src", c.getMessage());
                v.getContext().startActivity(i);
            }
        });

        if(message_type.equals("text")){
            holder.messageText.setText(c.getMessage());
            holder.cardView.setVisibility(View.GONE);

        }
        if(message_type.equals("image")){
            holder.messageText.setVisibility(View.GONE);
            Picasso picasso= Picasso.get();
            picasso.setIndicatorsEnabled(false);
            picasso.load(c.getMessage()).placeholder(R.drawable.icon_profile).into(holder.messageImage);
        }
        
        if(message_type.equals("ChatVideo")){
            holder.cardView.setVisibility(View.GONE);
            mMessageDatabase=FirebaseDatabase.getInstance().getReference().child("Messages");
            mMessageDatabase.child(from_user).child(to_user).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.hasChild("videoEnd") && dataSnapshot.hasChild("time")) {
                        long start = c.getTime();
                        long end = c.getVideoEnd();
                        long diff = end - start;

                        if (diff >= SECOND_MILLIS && diff <= 59 * SECOND_MILLIS) {
                            if (diff == SECOND_MILLIS) {
                                holder.messageText.setText("Video Chat" + "\n" + "1 sec");
                            }else{
                                holder.messageText.setText("Video Chat" + "\n" + diff / SECOND_MILLIS + " secs");
                            }
                        }
                        if (diff >= MINUTE_MILLIS && diff <= 59 * MINUTE_MILLIS) {
                            if (diff == MINUTE_MILLIS || (diff / MINUTE_MILLIS == 1 && diff % MINUTE_MILLIS != 0)) {
                                holder.messageText.setText("Video Chat" + "\n" + "1 min");
                            } else {
                                holder.messageText.setText("Video Chat" + "\n" + diff / MINUTE_MILLIS + " mins");
                            }

                        }
                        if (diff >= HOUR_MILLIS && diff <= 24 * HOUR_MILLIS && diff % HOUR_MILLIS == 0) {
                            if (diff == HOUR_MILLIS) {
                                holder.messageText.setText("Video Chat" + "\n" + "1 hr");
                            } else {
                                holder.messageText.setText("Video Chat" + "\n" + diff / HOUR_MILLIS + " hrs");
                            }
                        }
                        if (diff >= HOUR_MILLIS && diff <= 24 * HOUR_MILLIS && diff % HOUR_MILLIS != 0) {
                            if (diff == HOUR_MILLIS) {
                                if (diff % HOUR_MILLIS == MINUTE_MILLIS) {
                                    holder.messageText.setText("Video Chat" + "\n" + "1 hr" + "1 min");
                                } else {
                                    holder.messageText.setText("Video Chat" + "\n" + "1 hrs" + (diff % HOUR_MILLIS) / MINUTE_MILLIS + " mins");
                                }
                            } else {
                                if (diff % HOUR_MILLIS == MINUTE_MILLIS) {
                                    holder.messageText.setText("Video Chat" + "\n" + diff / HOUR_MILLIS + " hrs" + "1 min");
                                } else {
                                    holder.messageText.setText("Video Chat" + "\n" + diff / HOUR_MILLIS + " hrs" + (diff % HOUR_MILLIS) / MINUTE_MILLIS + " mins");
                                }
                            }

                        }
                    }
                    if (dataSnapshot.hasChild("message")) {
                        if (dataSnapshot.child("message").hasChild("End Call") || dataSnapshot.child("message").hasChild("Missed Call")) {
                            holder.messageText.setText(c.getMessage());
                            holder.messageImage.setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }



    @Override
    public int getItemViewType(int position) {
        mAuth= FirebaseAuth.getInstance();
        if(mMessageList.get(position).getFrom().equals(mAuth.getCurrentUser().getUid())){
            return RIGHT;
        }
        else {
            return LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText, messageSeen, messageTime;
        public CircleImageView messageIcon;
        public ImageView messageImage;
        public CardView cardView;
        public ConstraintLayout messageLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            
            messageText=(TextView) itemView.findViewById(R.id.message_text_layout);
            messageIcon=(CircleImageView)itemView.findViewById(R.id.message_icon_layout);
            messageImage=(ImageView)itemView.findViewById(R.id.message_image_layout);
            cardView = (CardView) itemView.findViewById(R.id.message_card);
            messageSeen = (TextView) itemView.findViewById(R.id.message_seen_layout);
            messageTime = (TextView) itemView.findViewById(R.id.message_time_layout);
            messageLayout = (ConstraintLayout) itemView.findViewById(R.id.message_layout);
        }
    }

    
}
