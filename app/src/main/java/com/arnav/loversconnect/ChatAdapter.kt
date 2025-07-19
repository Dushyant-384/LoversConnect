package com.arnav.loversconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentMessage = messageList[position]
        holder.messageText.text = currentMessage.text
    }

    // Returns the total number of messages.
    override fun getItemCount(): Int {
        return messageList.size
    }
}