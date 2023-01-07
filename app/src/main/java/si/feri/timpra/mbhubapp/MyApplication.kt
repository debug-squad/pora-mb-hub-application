package si.feri.timpra.mbhubapp

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import java.util.*

const val MY_SP_FILE_NAME = "myshared.data"
const val PREFERENCES_ID = "ID"
const val MQTT_BROKER_URL = "125202eb8708473eb5d18de1dacaa45a.s2.eu.hivemq.cloud"
const val MQTT_BROKER_PORT = 8883
const val MQTT_USERNAME = "mb-hub-client"
const val MQTT_PASSWORD = "hhKc28G39bYcec7Q"

class MyApplication : Application() {
    private lateinit var sharedPref: SharedPreferences

    private lateinit var clientID: UUID

    private lateinit var database: FirebaseDatabase
    private lateinit var eventsRef: DatabaseReference

    private lateinit var mqttClient: Mqtt5Client

    private val _online = MutableLiveData<Boolean>().apply { value = false }
    val online: LiveData<Boolean> = _online

    private val _mqttConnected = MutableLiveData<Boolean>().apply { value = false }
    val mqttConnected: LiveData<Boolean> = _mqttConnected

    override fun onCreate() {
        super.onCreate()

        //
        // Init shared preferences
        //

        sharedPref = getSharedPreferences(MY_SP_FILE_NAME, Context.MODE_PRIVATE)

        //
        // Set client id
        //

        if (!sharedPref.contains(PREFERENCES_ID)) {
            clientID = UUID.randomUUID()
            with(sharedPref.edit()) {
                putString(PREFERENCES_ID, clientID.toString())
                apply()
            }
        } else {
            clientID = UUID.fromString(sharedPref.getString(PREFERENCES_ID, null)!!)
        }

        //
        // Connect to db
        //

        database = Firebase.database
        eventsRef = database.getReference("events")

        //
        // Connect to MQTT
        //

        mqttClient = MqttClient.builder()
            .useMqttVersion5()
            .identifier(clientID.toString())
            .serverHost(MQTT_BROKER_URL)
            .serverPort(MQTT_BROKER_PORT)
            .sslWithDefaultConfig()
            .simpleAuth()
            .username(MQTT_USERNAME)
            .password(MQTT_PASSWORD.toByteArray())
            .applySimpleAuth()
            .build()

        //
        // Connectivity
        //

        getSystemService(ConnectivityManager::class.java)!!.registerDefaultNetworkCallback(object :
            NetworkCallback() {
            override fun onAvailable(network: Network) {
                _online.postValue(true)
            }

            override fun onLost(network: Network) {
                _online.postValue(false)
            }
        })
    }

    fun connect() {
        if (mqttConnected.value!!) return;
        try {
            mqttClient.toBlocking().connect()
            _mqttConnected.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _mqttConnected.value = false
        }
    }

    fun disconnect() {
        if (!mqttConnected.value!!) return;
        try {
            mqttClient.toBlocking().disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _mqttConnected.value = false
    }
}