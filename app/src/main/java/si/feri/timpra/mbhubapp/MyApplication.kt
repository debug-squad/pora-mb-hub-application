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
import com.google.gson.Gson
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import org.osmdroid.util.GeoPoint
import si.feri.timpra.mbhubapp.data.CaptureSettings
import java.util.*

const val MY_SP_FILE_NAME = "myshared.data"
const val PREFERENCES_ID = "ID"
const val PREFERENCES_SIMULATION_POS = "SIMULATION_POS"
const val PREFERENCES_SETTINGS_ACCELEROMETER = "SETTINGS_ACCELEROMETER"
const val PREFERENCES_SETTINGS_SOUND = "SETTINGS_SOUND"

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

    lateinit var mqttClient: Mqtt5Client

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


    private val _simSoundPath = MutableLiveData<String?>()
    val simSoundPath: LiveData<String?> = _simSoundPath
    private val _simSoundSettings = MutableLiveData<CaptureSettings>()
    val simSoundSettings: LiveData<CaptureSettings> = _simSoundSettings

    private val _simAccPath = MutableLiveData<String?>()
    val simAccPath: LiveData<String?> = _simAccPath
    private val _simAccSettings = MutableLiveData<CaptureSettings>()
    val simAccSettings: LiveData<CaptureSettings> = _simAccSettings

    private val _simImgPath = MutableLiveData<String?>()
    val simImgPath: LiveData<String?> = _simImgPath
    private val _simImgSettings = MutableLiveData<CaptureSettings>()
    val simImgSettings: LiveData<CaptureSettings> = _simImgSettings

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
            val point = GeoPoint(46.557314, 15.637771)
            with(sharedPref.edit()) {
                putString(PREFERENCES_SIMULATION_POS, Gson().toJson(point))
                apply()
            }
            _simulationPosition.value = point
        } else {
            _simulationPosition.value = Gson().fromJson(
                sharedPref.getString(PREFERENCES_SIMULATION_POS, null)!!, GeoPoint::class.java
            )
        }

        //
        // Set simulation accelerometer
        //

        if (!sharedPref.contains(PREFERENCES_SETTINGS_ACCELEROMETER)) {
            val settings = CaptureSettings.DEFAULT_ACCELEROMETER
            with(sharedPref.edit()) {
                putString(PREFERENCES_SETTINGS_ACCELEROMETER, Gson().toJson(settings))
                apply()
            }
            _settingsAcc.value = settings
        } else {
            _settingsAcc.value = Gson().fromJson(
                sharedPref.getString(PREFERENCES_SETTINGS_ACCELEROMETER, null)!!,
                CaptureSettings::class.java
            )
        }

        //
        // Set simulation sound
        //

        if (!sharedPref.contains(PREFERENCES_SETTINGS_SOUND)) {
            val settings = CaptureSettings.DEFAULT_SOUND
            with(sharedPref.edit()) {
                putString(PREFERENCES_SETTINGS_SOUND, Gson().toJson(settings))
                apply()
            }
            _settingsSound.value = settings
        } else {
            _settingsSound.value = Gson().fromJson(
                sharedPref.getString(PREFERENCES_SETTINGS_SOUND, null)!!,
                CaptureSettings::class.java
            )
        }

        //
        // Set sim sound
        //

        if (sharedPref.contains(PREFERENCES_SETTINGS_SIM_SOUND_PATH)) {
            _simSoundPath.value = sharedPref.getString(PREFERENCES_SETTINGS_SIM_SOUND_PATH, null)!!
        } else {
            _simSoundPath.value = null
        }
        if (!sharedPref.contains(PREFERENCES_SETTINGS_SIM_SOUND)) {
            val settings = CaptureSettings.DEFAULT_SOUND
            with(sharedPref.edit()) {
                putString(PREFERENCES_SETTINGS_SIM_SOUND, Gson().toJson(settings))
                apply()
            }
            _simSoundSettings.value = settings
        } else {
            _simSoundSettings.value = Gson().fromJson(
                sharedPref.getString(PREFERENCES_SETTINGS_SIM_SOUND, null)!!,
                CaptureSettings::class.java
            )
        }

        //
        // Set sim acc
        //

        if (sharedPref.contains(PREFERENCES_SETTINGS_SIM_ACC_PATH)) {
            _simAccPath.value = sharedPref.getString(PREFERENCES_SETTINGS_SIM_ACC_PATH, null)!!
        } else {
            _simAccPath.value = null
        }
        if (!sharedPref.contains(PREFERENCES_SETTINGS_SIM_ACC)) {
            val settings = CaptureSettings.DEFAULT_ACCELEROMETER
            with(sharedPref.edit()) {
                putString(PREFERENCES_SETTINGS_SIM_ACC, Gson().toJson(settings))
                apply()
            }
            _simAccSettings.value = settings
        } else {
            _simAccSettings.value = Gson().fromJson(
                sharedPref.getString(PREFERENCES_SETTINGS_SIM_ACC, null)!!,
                CaptureSettings::class.java
            )
        }

        //
        // Set sim img
        //

        if (sharedPref.contains(PREFERENCES_SETTINGS_SIM_IMG_PATH)) {
            _simImgPath.value = sharedPref.getString(PREFERENCES_SETTINGS_SIM_IMG_PATH, null)!!
        } else {
            _simImgPath.value = null
        }
        if (!sharedPref.contains(PREFERENCES_SETTINGS_SIM_IMG)) {
            val settings = CaptureSettings.DEFAULT_IMAGE
            with(sharedPref.edit()) {
                putString(PREFERENCES_SETTINGS_SIM_IMG, Gson().toJson(settings))
                apply()
            }
            _simImgSettings.value = settings
        } else {
            _simImgSettings.value = Gson().fromJson(
                sharedPref.getString(PREFERENCES_SETTINGS_SIM_IMG, null)!!,
                CaptureSettings::class.java
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
        _settingsAcc.value = settings
    }

    fun updateSettingsSound(settings: CaptureSettings) {
        with(sharedPref.edit()) {
            putString(PREFERENCES_SETTINGS_SOUND, Gson().toJson(settings))
            apply()
        }
        _settingsSound.value = settings
    }


    fun updateSimAccSettings(settings: CaptureSettings) {
        with(sharedPref.edit()) {
            putString(PREFERENCES_SETTINGS_SIM_ACC, Gson().toJson(settings))
            apply()
        }
        _simAccSettings.value = settings
    }

    fun updateSimAccPath(path: String?) {
        if (path == null) updateSimAccSettings(simAccSettings.value!!.setEnabled(false))

        with(sharedPref.edit()) {
            putString(PREFERENCES_SETTINGS_SIM_ACC_PATH, path)
            apply()
        }
        _simAccPath.value = path

    }

    fun updateSimImgSettings(settings: CaptureSettings) {
        with(sharedPref.edit()) {
            putString(PREFERENCES_SETTINGS_SIM_IMG, Gson().toJson(settings))
            apply()
        }
        _simImgSettings.value = settings
    }

    fun updateSimImgPath(path: String?) {
        if (path == null) updateSimImgSettings(simImgSettings.value!!.setEnabled(false))

        with(sharedPref.edit()) {
            putString(PREFERENCES_SETTINGS_SIM_IMG_PATH, path)
            apply()
        }
        _simImgPath.value = path
    }

    fun updateSimSoundSettings(settings: CaptureSettings) {
        with(sharedPref.edit()) {
            putString(PREFERENCES_SETTINGS_SIM_SOUND, Gson().toJson(settings))
            apply()
        }
        _simSoundSettings.value = settings
    }

    fun updateSimSoundPath(path: String?) {
        if (path == null) updateSimSoundSettings(simSoundSettings.value!!.setEnabled(false))

        with(sharedPref.edit()) {
            putString(PREFERENCES_SETTINGS_SIM_SOUND_PATH, path)
            apply()
        }
        _simSoundPath.value = path
    }
}