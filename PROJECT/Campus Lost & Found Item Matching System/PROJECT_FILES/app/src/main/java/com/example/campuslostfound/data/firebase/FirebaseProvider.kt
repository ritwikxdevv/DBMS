package com.example.campuslostfound.data.firebase

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseProvider {

    val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()
}