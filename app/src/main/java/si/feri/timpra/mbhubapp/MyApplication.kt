package si.feri.timpra.mbhubapp

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

const val MY_SP_FILE_NAME = "myshared.data"
const val PREFERENCES_ID = "ID"

class MyApplication : Application() {
    private lateinit var sharedPref: SharedPreferences

    private lateinit var clientID: UUID

    private lateinit var database: FirebaseDatabase
    private lateinit var eventsRef: DatabaseReference

    override fun onCreate() {
        super.onCreate()

        // Init shared preferences
        sharedPref = getSharedPreferences(MY_SP_FILE_NAME, Context.MODE_PRIVATE)

        // Set client id
        if (!sharedPref.contains(PREFERENCES_ID)) {
            clientID = UUID.randomUUID()
            with(sharedPref.edit()) {
                putString(PREFERENCES_ID, clientID.toString())
                apply()
            }
        } else {
            clientID = UUID.fromString(sharedPref.getString(PREFERENCES_ID, null)!!)
        }

        // Connect to db
        database = Firebase.database
        eventsRef = database.getReference("events")
    }
}