package com.arnav.loversconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase
class ChatAdapter(private val messageList: ArrayList<Message>) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    // This class holds the view for a single message item.
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(android.R.id.text1)
    }

    // Creates a new view holder when the RecyclerView needs one.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // We're using a simple built-in layout for each message row.
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return MessageViewHolder(view)
    }

    // Binds the data (the message text) to the view.
    // Inside ChatAdapter.kt -> onBindViewHolder
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentMessage = messageList[position]
        holder.messageText.text = currentMessage.text

        // Add a long-click listener to the item view
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Message")
                .setMessage("Do you want to delete this message for everyone?")
                .setPositiveButton("Delete") { _, _ ->
                    // Get reference to the chats node in Firebase
                    val dbRef = FirebaseDatabase.getInstance().getReference("chats")
                    // Use the message ID to delete the specific message
                    currentMessage.id?.let { messageId ->
                        dbRef.child(messageId).removeValue()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
            true // Return true to indicate the event was handled
        }
    }

    // Returns the total number of messages.
    override fun getItemCount(): Int {
        return messageList.size
    }
}