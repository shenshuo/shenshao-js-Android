package com.lyk.jsbridge;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shenshao on 16-7-19.
 * 用于返回读取到联系人的集合
 */
public class ContactInfoService {
    private Context context;

    public ContactInfoService(Context context) {
        this.context = context;
    }

    public List<ContactBean> getContactList() {

        List<ContactBean> mContactBeanList = new ArrayList<>();
        ContactBean mContactBean = null;
        ContentResolver mContentResolver = context.getContentResolver();
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        Uri dataUri = Uri.parse("content://com.android.contacts/data");

        Cursor cursor = mContentResolver.query(uri, null, null, null, null);
        while (cursor.moveToNext()) {
            mContactBean = new ContactBean();
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            String title = cursor.getString(cursor.getColumnIndex("display_name"));//获取联系人姓名
            String firstHeadLetter="";
            if(Build.VERSION.SDK_INT>=19){

                firstHeadLetter  = cursor.getString(cursor.getColumnIndex("phonebook_label"));//这个字段保存了每个联系人首字的拼音的首字母
            }else{
               firstHeadLetter = cursor.getString(cursor.getColumnIndex("sort_key"));
                if(firstHeadLetter.trim().substring(0,1).matches("[a-z]")){
                    firstHeadLetter=firstHeadLetter.toUpperCase();
                    firstHeadLetter= firstHeadLetter.trim().substring(0,1);
                }else{
                    firstHeadLetter= firstHeadLetter.trim().substring(0,1);
                }
            }
            mContactBean.setTitle(title);
            mContactBean.setFirstHeadLetter(firstHeadLetter);

            Cursor dataCursor = mContentResolver.query(dataUri, null, "raw_contact_id= ?", new String[]{id}, null);
            while (dataCursor.moveToNext()) {
                String type = dataCursor.getString(dataCursor.getColumnIndex("mimetype"));
                if (type.equals("vnd.android.cursor.item/phone_v2")) {//如果得到的mimeType类型为手机号码类型才去接收

                    String phoneNum = dataCursor.getString(dataCursor.getColumnIndex("data1"));//获取手机号码
                    mContactBean.setPhoneNum(phoneNum);
                }
            }
            dataCursor.close();
            if (mContactBean.getTitle() != null && mContactBean.getPhoneNum() != null) {
                mContactBeanList.add(mContactBean);
            }

        }
        cursor.close();
        return mContactBeanList;
    }
    public static String changeArrayDateToJson(List<ContactBean> list) {  //把一个集合转换成json格式的字符串
        JSONObject object;
        JSONArray jsonArray;//JSONObject对象，处理一个一个集合或者数组
        String jsonString;  //保存带集合的json字符串
        JSONObject object2;
        jsonArray = null;
        object = null;
        jsonArray = null;
        object = null;
        jsonArray = new JSONArray();
        object = new JSONObject();


        for (int i = 0; i < list.size(); i++) {  //遍历上面初始化的集合数据，把数据加入JSONObject里面
            object2 = new JSONObject();//一个user对象，使用一个JSONObject对象来装
            try {
                object2.put("name", list.get(i).getTitle());  //从集合取出数据，放入JSONObject里面 JSONObject对象和map差不多用法,以键和值形式存储数据
                object2.put("phone", list.get(i).getPhoneNum());
                jsonArray.put(object2); //把JSONObject对象装入jsonArray数组里面
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



//        try {
//            object.put("userDate", jsonArray); //再把JSONArray数据加入JSONObject对象里面(数组也是对象)
//            //object.put("time", "2013-11-14"); //这里还可以加入数据，这样json型字符串，就既有集合，又有普通数据
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        jsonString=null;
//        jsonString = object.toString(); //把JSONObject转换成json格式的字符串
        return jsonArray.toString();

    }
    public List<ContactBean> printContacts() {
        //生成ContentResolver对象
        List<ContactBean> mContactBeanList=new ArrayList<>();
        ContactBean mContactBean=null;
        ContentResolver contentResolver = context.getContentResolver();

        // 获得所有的联系人
        /*Cursor cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
         */
        //这段代码和上面代码是等价的，使用两种方式获得联系人的Uri
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.contacts/contacts"),null,null,null,null);

        // 循环遍历
        if (cursor.moveToFirst()) {

            int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            int displayNameColumn = cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

            do {
                // 获得联系人的ID
                String contactId = cursor.getString(idColumn);
                // 获得联系人姓名
                String displayName = cursor.getString(displayNameColumn);


                //使用Toast技术显示获得的联系人信息

                String firstHeadLetter="";
                if(Build.VERSION.SDK_INT>=19){

                    firstHeadLetter  = cursor.getString(cursor.getColumnIndex("phonebook_label"));//这个字段保存了每个联系人首字的拼音的首字母
                }else{
                    firstHeadLetter = cursor.getString(cursor.getColumnIndex("sort_key"));
                    if(firstHeadLetter.trim().substring(0,1).matches("[a-z]")){
                        firstHeadLetter=firstHeadLetter.toUpperCase();
                        firstHeadLetter= firstHeadLetter.trim().substring(0,1);
                    }else{
                        firstHeadLetter= firstHeadLetter.trim().substring(0,1);
                    }
                }
                // 查看联系人有多少个号码，如果没有号码，返回0
                int phoneCount = cursor
                        .getInt(cursor
                                .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if (phoneCount > 0) {
                    // 获得联系人的电话号码列表
                    Cursor phoneCursor = context.getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                    + "=" + contactId, null, null);
                    if(phoneCursor.moveToFirst())
                    {
                        do
                        {
                            //遍历所有的联系人下面所有的电话号码
                            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            //使用Toast技术显示获得的号码
                            mContactBean=new ContactBean();
                            mContactBean.setTitle(displayName);
                            mContactBean.setFirstHeadLetter(firstHeadLetter);
                            mContactBean.setPhoneNum(phoneNumber);
                            if (mContactBean.getTitle()!=null&&mContactBean.getPhoneNum()!=null){
                                mContactBeanList.add(mContactBean);
                            }
                        }while(phoneCursor.moveToNext());
                    }
                }


            } while (cursor.moveToNext());

        }
        cursor.close();
        return mContactBeanList;
    }
}
