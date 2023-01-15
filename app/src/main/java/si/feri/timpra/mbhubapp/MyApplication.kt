package si.feri.timpra.mbhubapp

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.osmdroid.util.GeoPoint
import si.feri.timpra.mbhubapp.data.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

const val MY_SP_FILE_NAME = "myshared.data"
const val PREFERENCES_ID = "ID"
const val PREFERENCES_SIMULATION_POS = "SIMULATION_POS"
const val PREFERENCES_SETTINGS_ACCELEROMETER = "SETTINGS_ACCELEROMETER"
const val PREFERENCES_SETTINGS_SOUND = "SETTINGS_SOUND"

const val PREFERENCES_EVENT = "PREFERENCES_EVENT"

const val PREFERENCES_SETTINGS_SIM_SOUND = "SETTINGS_SIM_SOUND"
const val PREFERENCES_SETTINGS_SIM_SOUND_PATH = "SETTINGS_SIM_SOUND_PATH"
const val PREFERENCES_SETTINGS_SIM_IMG = "SETTINGS_SIM_IMG"
const val PREFERENCES_SETTINGS_SIM_IMG_PATH = "SETTINGS_SIM_IMG_PATH"
const val PREFERENCES_SETTINGS_SIM_ACC = "SETTINGS_SIM_ACC"
const val PREFERENCES_SETTINGS_SIM_ACC_PATH = "SETTINGS_SIM_ACC_PATH"

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

    private val _simulationPosition = MutableLiveData<GeoPoint>()
    val simulationPosition: LiveData<GeoPoint> = _simulationPosition

    private val _settingsAcc = MutableLiveData<CaptureSettings>()
    val settingsAcc: LiveData<CaptureSettings> = _settingsAcc

    private val _settingsSound = MutableLiveData<CaptureSettings>()
    val settingsSound: LiveData<CaptureSettings> = _settingsSound

    private val _simSoundSettings = MutableLiveData<SoundSettings>()
    val simSoundSettings: LiveData<SoundSettings> = _simSoundSettings
    private val _simAccSettings = MutableLiveData<AccSettings>()
    val simAccSettings: LiveData<AccSettings> = _simAccSettings
    private val _simImgSettings = MutableLiveData<ImgSettings>()
    val simImgSettings: LiveData<ImgSettings> = _simImgSettings

    private val _selectedEvent = MutableLiveData<Event?>()
    val selectedEvent: LiveData<Event?> = _selectedEvent

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
        // Set simulation position
        //

        if (!sharedPref.contains(PREFERENCES_SIMULATION_POS)) {
            _simulationPosition.value = GeoPoint(46.557314, 15.637771)
            with(sharedPref.edit()) {
                putString(PREFERENCES_SIMULATION_POS, Gson().toJson(simulationPosition.value))
                apply()
            }
        } else {
            _simulationPosition.value = Gson().fromJson(
                sharedPref.getString(PREFERENCES_SIMULATION_POS, null)!!, GeoPoint::class.java
            )
        }

        //
        // Set accelerometer
        //

        if (!sharedPref.contains(PREFERENCES_SETTINGS_ACCELEROMETER)) {
            _settingsAcc.value = CaptureSettings.DEFAULT_ACCELEROMETER
            with(sharedPref.edit()) {
                putString(PREFERENCES_SETTINGS_ACCELEROMETER, Gson().toJson(settingsAcc.value!!))
                apply()
            }
        } else {
            _settingsAcc.value = Gson().fromJson(
                sharedPref.getString(PREFERENCES_SETTINGS_ACCELEROMETER, null)!!,
                CaptureSettings::class.java
            )
        }

        //
        // Set sound
        //

        if (!sharedPref.contains(PREFERENCES_SETTINGS_SOUND)) {
            _settingsSound.value = CaptureSettings.DEFAULT_SOUND
            with(sharedPref.edit()) {
                putString(PREFERENCES_SETTINGS_SOUND, Gson().toJson(settingsSound.value!!))
                apply()
            }
        } else {
            _settingsSound.value = Gson().fromJson(
                sharedPref.getString(PREFERENCES_SETTINGS_SOUND, null)!!,
                CaptureSettings::class.java
            )
        }

        //
        // Set sim sound
        //

        if (!sharedPref.contains(PREFERENCES_SETTINGS_SIM_SOUND)) {
            _simSoundSettings.value = SoundSettings.DEFAULT
            with(sharedPref.edit()) {
                putString(PREFERENCES_SETTINGS_SIM_SOUND, Gson().toJson(simSoundSettings.value!!))
                apply()
            }
        } else {
            _simSoundSettings.value = Gson().fromJson(
                sharedPref.getString(PREFERENCES_SETTINGS_SIM_SOUND, null)!!,
                SoundSettings::class.java
            )
        }

        //
        // Set sim acc
        //

        if (!sharedPref.contains(PREFERENCES_SETTINGS_SIM_ACC)) {
            _simAccSettings.value = AccSettings.DEFAULT
            with(sharedPref.edit()) {
                putString(PREFERENCES_SETTINGS_SIM_ACC, Gson().toJson(simAccSettings.value!!))
                apply()
            }

        } else {
            _simAccSettings.value = Gson().fromJson(
                sharedPref.getString(PREFERENCES_SETTINGS_SIM_ACC, null)!!,
                AccSettings::class.java
            )
        }

        //
        // Set sim img
        //

        if (!sharedPref.contains(PREFERENCES_SETTINGS_SIM_IMG)) {
            _simImgSettings.value = ImgSettings.DEFAULT
            with(sharedPref.edit()) {
                putString(PREFERENCES_SETTINGS_SIM_IMG, Gson().toJson(simImgSettings.value!!))
                apply()
            }

        } else {
            _simImgSettings.value = Gson().fromJson(
                sharedPref.getString(PREFERENCES_SETTINGS_SIM_IMG, null)!!,
                ImgSettings::class.java
            )
        }

        //
        // Set event
        //

        if (!sharedPref.contains(PREFERENCES_EVENT)) {
            _selectedEvent.value = null
            with(sharedPref.edit()) {
                putString(PREFERENCES_EVENT, Gson().toJson(selectedEvent.value))
                apply()
            }
        } else {
            _selectedEvent.value = Gson().fromJson(
                sharedPref.getString(PREFERENCES_EVENT, null)!!, Event::class.java
            )
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
            .serverPort(MQTT_BROKER_PORT).sslWithDefaultConfig()
            .simpleAuth()
            .username(MQTT_USERNAME)
            .password(MQTT_PASSWORD.toByteArray())
            .applySimpleAuth().build()

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
        if (mqttConnected.value!!) return
        try {
            mqttClient.toBlocking().connect()
            _mqttConnected.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _mqttConnected.value = false
        }
    }

    fun disconnect() {
        if (!mqttConnected.value!!) return
        try {
            mqttClient.toBlocking().disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _mqttConnected.value = false
    }

    fun getId(): UUID = clientID

    fun updateSimulationPosition(geo: GeoPoint) {
        with(sharedPref.edit()) {
            putString(PREFERENCES_SIMULATION_POS, Gson().toJson(geo))
            apply()
        }
        _simulationPosition.value = geo
    }

    fun updateSettingsAccelerometer(settings: CaptureSettings) {
        with(sharedPref.edit()) {
            putString(PREFERENCES_SETTINGS_ACCELEROMETER, Gson().toJson(settings))
            apply()
        }
        _settingsAcc.postValue(settings)
    }

    fun updateSettingsSound(settings: CaptureSettings) {
        with(sharedPref.edit()) {
            putString(PREFERENCES_SETTINGS_SOUND, Gson().toJson(settings))
            apply()
        }
        _settingsSound.postValue(settings)
    }


    fun updateSimAccSettings(settings: AccSettings) {
        with(sharedPref.edit()) {
            putString(PREFERENCES_SETTINGS_SIM_ACC, Gson().toJson(settings))
            apply()
        }
        _simAccSettings.postValue(settings)
    }

    fun updateSimImgSettings(settings: ImgSettings) {
        with(sharedPref.edit()) {
            putString(PREFERENCES_SETTINGS_SIM_IMG, Gson().toJson(settings))
            apply()
        }
        _simImgSettings.postValue(settings)
    }

    fun updateSimSoundSettings(settings: SoundSettings) {
        with(sharedPref.edit()) {
            putString(PREFERENCES_SETTINGS_SIM_SOUND, Gson().toJson(settings))
            apply()
        }
        _simSoundSettings.postValue(settings)
    }

    fun updateEvent(event: Event?) {
        with(sharedPref.edit()) {
            putString(PREFERENCES_EVENT, Gson().toJson(event))
            apply()
        }
        _selectedEvent.postValue(event)
    }
    //
    //
    //

    private fun locTimeToTag(time: LocalDateTime, latitude: Double, longitude: Double): ByteArray =
        Gson().toJson(
            mapOf(
                "timestamp" to time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "latitude" to latitude,
                "longitude" to longitude,
                "eventId" to selectedEvent.value?.id,
            )
        ).toByteArray()

    fun sendSound(time: LocalDateTime, latitude: Double, longitude: Double, data: ByteArray) {
        if (mqttConnected.value != true) return;
        Toast.makeText(this, "Sending sound", Toast.LENGTH_SHORT).show()
        mqttClient.toBlocking()
            .publish(
                Mqtt5Publish.builder()
                    .topic("audio/$clientID")
                    .payload(
                        locTimeToTag(
                            time = time,
                            latitude = latitude,
                            longitude = longitude
                        ) + data
                    )
                    .qos(MqttQos.AT_MOST_ONCE).build()
            )
    }

    fun sendImg(time: LocalDateTime, latitude: Double, longitude: Double, data: ByteArray) {
        if (mqttConnected.value != true) return;
        Toast.makeText(this, "Sending image", Toast.LENGTH_SHORT).show()
        mqttClient.toBlocking().publish(
            Mqtt5Publish.builder().topic("picture/$clientID")
                .payload(
                    locTimeToTag(
                        time = time,
                        latitude = latitude,
                        longitude = longitude
                    ) + data
                )
                .qos(MqttQos.AT_MOST_ONCE).build()
        )
    }

    fun sendAcc(time: LocalDateTime, latitude: Double, longitude: Double, data: ByteArray) {
        if (mqttConnected.value != true) return;
        Toast.makeText(this, "Sending acceleration", Toast.LENGTH_SHORT).show()
        mqttClient.toBlocking()
            .publish(
                Mqtt5Publish.builder().topic("accel/$clientID")
                    .payload(
                        locTimeToTag(
                            time = time,
                            latitude = latitude,
                            longitude = longitude
                        ) + data
                    )
                    .qos(MqttQos.AT_MOST_ONCE).build()
            )
    }
}