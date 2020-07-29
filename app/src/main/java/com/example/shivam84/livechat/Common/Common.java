package com.example.shivam84.livechat.Common;

import com.example.shivam84.livechat.Holder.QBUserHolder;
import com.quickblox.users.model.QBUser;

import java.util.List;

/**
 * Created by shivam84 on 6/24/2017.
 */
public class Common {
    //add global variable to common class
    public static final String DIALOG_EXTRA="dialog";

    public static final String UPDATE_DIALOG_EXTRA="chatdialogs";
    public static final String UPDATE_MODE="mode";
    public static final String UPDATE_ADD_MODE="add";
    public static final String UPDATE_REMOVE_MODE="remove";



    //write method of create name of chat dialog from userList
    public static String createChatDialogName(List<Integer> qbUsers){



        //create Holder package and QBUserHolder class for cache
        List<QBUser> qbUsers1= QBUserHolder.getInstance().getUserByIds(qbUsers);
        StringBuilder name=new StringBuilder();

        //dialog name of all user name in the list,if length of namw over 30 then we put "..." at end of name
        for(QBUser user:qbUsers1)
            name.append(user.getFullName()).append("");
        if(name.length()>30)
            name=name.replace(30,name.length()-1,"...");

        return name.toString();



    }
    //now goto  listUserActivity in groupchat method and set the name


    public static boolean isNullOrEmpty(String content){
        return (content !=null && !content.trim().isEmpty() ? false :true);
    }
}
