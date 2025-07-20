
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
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import android.app.AlertDialog
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.ImageView
import android.view.LayoutInflater
import android.widget.SeekBar
import android.net.Uri
import android.provider.MediaStore



class MainActivity : AppCompatActivity() {

    // Declare all the variables for our views and Firebase.
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var messageAdapter: ChatAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // Add these new functions inside the MainActivity class

    private fun loadWallpaper() {
        val prefs = getSharedPreferences("WallpaperPrefs", MODE_PRIVATE)
        val wallpaperUriString = prefs.getString("wallpaper_uri", null)
        val opacity = prefs.getFloat("wallpaper_opacity", 0.3f) // Default to 30% opacity

        if (wallpaperUriString != null) {
            wallpaperImageView.setImageURI(Uri.parse(wallpaperUriString))
        }
        wallpaperImageView.alpha = opacity
    }

    private fun showOpacitySliderDialog() {
        val prefs = getSharedPreferences("WallpaperPrefs", MODE_PRIVATE)
        val currentOpacity = prefs.getFloat("wallpaper_opacity", 0.3f)

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_opacity_slider, null)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.opacitySeekBar)
        // We use 0-100 for the seekbar and convert to 0.0-1.0 for alpha
        seekBar.progress = (currentOpacity * 100).toInt()

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                wallpaperImageView.alpha = progress / 100f
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Save the final value
                prefs.edit().putFloat("wallpaper_opacity", seekBar!!.progress / 100f).apply()
            }
        })

        AlertDialog.Builder(this)
            .setTitle("Adjust Wallpaper Opacity")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    // At the top of MainActivity class
    private lateinit var wallpaperImageView: ImageView
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                // Take persistent permission for this URI
                contentResolver.takePersistableUriPermission(
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                wallpaperImageView.setImageURI(imageUri)
                // Save the URI to SharedPreferences
                getSharedPreferences("WallpaperPrefs", MODE_PRIVATE).edit()
                    .putString("wallpaper_uri", imageUri.toString())
                    .apply()
            }
        }
    }

    // Inside MainActivity.kt

    // Add this new function inside the MainActivity class
    private fun showThemeChooserDialog() {
        val themes = arrayOf("Default Pink", "Blue", "Purple")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose a Theme")
        builder.setItems(themes) { dialog, which ->
            // 'which' will be 0 for Pink, 1 for Blue, 2 for Purple
            val sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("theme_id", which)
            editor.apply()

            // Recreate the activity to apply the new theme
            recreate()
        }
        builder.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // This function inflates (creates) the menu.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Update your existing onOptionsItemSelected function
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_change_theme -> {
                // Call the new dialog function when "Change Theme" is clicked
                showThemeChooserDialog()
                return true
            }
            R.id.action_logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return true
            }
            R.id.action_set_wallpaper -> {
                // Use ACTION_OPEN_DOCUMENT to get a persistable URI
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*" // We want to see all image types
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                pickImageLauncher.launch(intent)
                return true
            }
            R.id.action_wallpaper_opacity -> {
                showOpacitySliderDialog()
                return true
            }
            // Inside onOptionsItemSelected's when block
            R.id.action_clear_chat -> {
                AlertDialog.Builder(this)
                    .setTitle("Clear Chat")
                    .setMessage("Are you sure you want to delete all messages? This cannot be undone.")
                    .setPositiveButton("Yes, Clear") { _, _ ->
                        // Remove the entire "chats" node from Firebase
                        dbRef.child("chats").removeValue()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // In MainActivity.kt
    override fun onCreate(savedInstanceState: Bundle?) {
        // ADD THIS BLOCK TO APPLY THE THEME BEFORE THE ACTIVITY IS CREATED
        val sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE)
        val themeId = sharedPreferences.getInt("theme_id", 0) // Default to 0 (Pink)
        when (themeId) {
            0 -> setTheme(R.style.Theme_LoversConnect)
            1 -> setTheme(R.style.Theme_LoversConnect_Blue)
            2 -> setTheme(R.style.Theme_LoversConnect_Purple)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        wallpaperImageView = findViewById(R.id.wallpaperImageView)
        loadWallpaper()
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

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
                // NEW AND CORRECTED CODE
                val message = Message(text = messageText, senderId = senderId)
                // Add the new message to the "chats" branch in your Firebase database.
                dbRef.child("chats").push().setValue(message)
                // Clear the input box after sending.
                etMessage.setText("")
            }
        }
    }

    // Update the loadMessages function in MainActivity
    private fun loadMessages() {
        dbRef.child("chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (postSnapshot in snapshot.children) {
                    val message = postSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        message.id = postSnapshot.key // Get the key and set it
                        messageList.add(message)
                    }
                }
                messageAdapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(messageList.size - 1)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}