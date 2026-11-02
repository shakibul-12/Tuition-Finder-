package com.shakibul.tuitionfinder.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {

    // যাকে জানাতে চান তার uid আর বার্তা দিলেই নোটিফিকেশন তৈরি হয়
    public static void send(String toUserId, String message) {
        if (toUserId == null) return;

        Map<String, Object> n = new HashMap<>();
        n.put("toUserId", toUserId);
        n.put("message", message);
        n.put("read", false);
        n.put("createdAt", System.currentTimeMillis());

        FirebaseFirestore.getInstance().collection("notifications").add(n);
    }
}