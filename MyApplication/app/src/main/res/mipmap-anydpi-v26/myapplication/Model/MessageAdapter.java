package com.example.myapplication.Model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private static final int RIGHT = 1;
    private static final int LEFT = 0;


    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }
    

    @androidx.annotation.NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
        if(viewType==RIGHT){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_message, parent, false);
            return new MessageViewHolder(v);
        }else{
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_message, parent, false);
            return new MessageViewHolder(v);
        }

    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull final MessageViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        final String current_user_id = mAuth.getCurrentUser().getUid();
        Messages c = mMessageList.get(position);

        final String from_user = c.getFrom();
        String message_type = c.getType();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                String image= dataSnapshot.child("thump_image").getValue().toString();
                if(!from_user.equals(current_user_id))  {
                    Picasso.get().load(image).placeholder(R.drawable.icon_profile).into(holder.messageIcon);
                }
            }
            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {

            }
        });
        holder.setIsRecyclable(false);
        if(message_type.equals("text")){
            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);
        }else {
            holder.messageText.setVisibility(View.INVISIBLE);
            Picasso picasso=Picasso.get();
            picasso.setIndicatorsEnabled(false);
            picasso.load(c.getMessage()).placeholder(R.drawable.icon_profile).into(holder.messageImage);
        }
        holder.messageText.setText(c.getMessage());




    }

    @Override
    public int getItemViewType(int position) {
        mAuth=FirebaseAuth.getInstance();
        if(mMessageList.get(position).getFrom().equals(mAuth.getCurrentUser().getUid())){
            return RIGHT;
        }
        else
            return LEFT;


    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public CircleImageView messageIcon;
        public ImageView messageImage;

        public MessageViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);
            
            messageText=(TextView) itemView.findViewById(R.id.message_text_layout);
            messageIcon=(CircleImageView)itemView.findViewById(R.id.message_icon_layout);
            messageImage=(ImageView)itemView.findViewById(R.id.message_image_layout);
        }
    }

    
}
