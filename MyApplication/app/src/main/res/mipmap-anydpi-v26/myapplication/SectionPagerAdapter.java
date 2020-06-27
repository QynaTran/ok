package com.example.myapplication;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.myapplication.Fragments.ChatsFragment;
import com.example.myapplication.Fragments.FriendsFragment;
import com.example.myapplication.Fragments.RequestsFragment;

class SectionPagerAdapter extends FragmentPagerAdapter {
    public SectionPagerAdapter(@androidx.annotation.NonNull FragmentManager fm) {
        super(fm);
    }

    public SectionPagerAdapter(@androidx.annotation.NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @androidx.annotation.NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                ChatsFragment chatsFragment=new ChatsFragment();
                return chatsFragment;
            case 1:
                FriendsFragment friendsFragment=new FriendsFragment();
                return friendsFragment;
            case 2:
                RequestsFragment requestsFragment=new RequestsFragment();
                return requestsFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3 ;
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "CHATS";
            case 1:
                return "FRIENDS";
            case 2:
                return "REQUESTS";
            default:
                return null;

        }
    }
}
