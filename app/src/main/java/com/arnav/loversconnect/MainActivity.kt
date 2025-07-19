
package com.arnav.loversconnect

// These are the import statements you were missing
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    // Declare all the variables for our views and Firebase.
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var messageAdapter: ChatAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth and Database reference.
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference

        // Find the views from our layout.
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        // Set up the message list and adapter.
        messageList = ArrayList()
        messageAdapter = ChatAdapter(messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        // Load messages from Firebase.
        loadMessages()

        // Set the action for the send button.
        btnSend.setOnClickListener {
            val messageText = etMessage.text.toString()
            val senderId = auth.currentUser?.uid // Get the current user's unique ID.

            // Make sure the message isn't empty and the user is logged in.
            if (messageText.isNotEmpty() && senderId != null) {
                val message = Message(messageText, senderId)
                // Add the new message to the "chats" branch in your Firebase database.
                dbRef.child("chats").push().setValue(message)
                // Clear the input box after sending.
                etMessage.setText("")
            }
        }
    }

    private fun loadMessages() {
        // This listener will run every time there's a change in the "chats" branch.
        dbRef.child("chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear() // Clear the old list.
                // Loop through all the messages in the database.
                for (postSnapshot in snapshot.children) {
                    val message = postSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        messageList.add(message) // Add the message to our list.
                    }
                }
                messageAdapter.notifyDataSetChanged() // Tell the adapter to refresh the screen.
                // Automatically scroll to the newest message.
                chatRecyclerView.scrollToPosition(messageList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                // This would run if there was a database error.
            }
        })
    }
}