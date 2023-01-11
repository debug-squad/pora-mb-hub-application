package si.feri.timpra.mbhubapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.osmdroid.config.Configuration
import si.feri.timpra.mbhubapp.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var app: MyApplication

    private var initialized by Delegates.notNull<Boolean>()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance()
            .load(applicationContext, this.getPreferences(Context.MODE_PRIVATE))
        binding = ActivityMainBinding.inflate(layoutInflater)
        app = application as MyApplication
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications,
                R.id.navigation_simulate
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //
        // Permissions
        //

        initialized = false
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allAreGranted = true
            for (b in result.values) allAreGranted = allAreGranted && b
            if (!allAreGranted) {
                this.finish()
            } else {
                init()
            }
        }.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.HIGH_SAMPLING_RATE_SENSORS,
                Manifest.permission.CAMERA,
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        destroy()
    }

    //
    //
    //

    @RequiresApi(Build.VERSION_CODES.S)
    private fun init() {
        initialized = true

        //
        // Model view
        //

        val spanOffline = binding.spanOffline
        app.online.observe(this) {
            // Update status
            spanOffline.visibility = if (it) View.GONE else View.VISIBLE

            // Reconnect MQTT
            if (it) app.connect()
        }

        val spanMqttOffline = binding.spanMqttOffline
        app.mqttConnected.observe(this) {
            // Update status
            spanMqttOffline.visibility = if (it) View.GONE else View.VISIBLE
        }

        //
        //
        //

        // takePhoto()
        // recordAudio(5000)
        // recordAccelerometer(5000)
    }

    private fun destroy() {
        if (!initialized) return

        //
        //
        //

        app.disconnect()
    }

    @SuppressLint("ServiceCast")
    fun recordAccelerometer(time: Long) {
        Toast.makeText(this, "Started recording acceleration", Toast.LENGTH_SHORT).show()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val data = mutableListOf<FloatArray>()
        val start = System.currentTimeMillis()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                data.add(
                    floatArrayOf(
                        event!!.values[0],
                        event.values[1],
                        event.values[2],
                        (System.currentTimeMillis() - start) / 1000.0f
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        Handler(Looper.getMainLooper()).postDelayed({
            sensorManager.unregisterListener(listener)
            Toast.makeText(this, "Stopped recording acceleration", Toast.LENGTH_SHORT).show()

            //
            //
            //

            val json = Gson().toJson(data)
            getLocTimeTag {
                app.mqttClient.toBlocking()
                    .publish(
                        Mqtt5Publish.builder().topic("accel/${app.getId()}")
                            .payload(it + json.toByteArray())
                            .qos(MqttQos.AT_MOST_ONCE).build()
                    )
            }
        }, time)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun recordAudio(time: Long) {
        Toast.makeText(this, "Started recording", Toast.LENGTH_SHORT).show()
        val mRecorder = MediaRecorder(this)
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        val file = File(this.cacheDir, "audioCapture.mp3")
        mRecorder.setOutputFile(file)
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mRecorder.prepare()
        mRecorder.start()
        Handler(Looper.getMainLooper()).postDelayed({
            mRecorder.stop()
            mRecorder.release()
            Toast.makeText(this, "Stopped recording", Toast.LENGTH_SHORT).show()

            //
            //
            //

            val data = Files.readAllBytes(file.toPath())
            getLocTimeTag {
                app.mqttClient.toBlocking()
                    .publish(
                        Mqtt5Publish.builder()
                            .topic("audio/${app.getId()}")
                            .payload(it + data)
                            .qos(MqttQos.AT_MOST_ONCE).build()
                    )
            }
        }, time)
    }

    private var resultImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data!!.extras!!["data"] as Bitmap
                Toast.makeText(this, "Took photo", Toast.LENGTH_SHORT).show()

                //
                //
                //

                val out = ByteArrayOutputStream()
                data.compress(Bitmap.CompressFormat.JPEG, 90, out)

                getLocTimeTag {
                    app.mqttClient.toBlocking().publish(
                        Mqtt5Publish.builder().topic("picture/${app.getId()}")
                            .payload(it + out.toByteArray())
                            .qos(MqttQos.AT_MOST_ONCE).build()
                    )
                }

            }
        }

    @SuppressLint("QueryPermissionsNeeded")
    fun takePhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(this.packageManager) != null) {
            resultImage.launch(cameraIntent)
        } else {
            // No program found
            finish()
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocTimeTag(callback: (ByteArray) -> Unit) {
        val time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val json = Gson().toJson(
                        mapOf<String, Any>(
                            "timestamp" to time,
                            "latitude" to it.latitude,
                            "longitude" to it.longitude,
                        )
                    )
                    callback(json.toByteArray())
                }
            }
    }
}