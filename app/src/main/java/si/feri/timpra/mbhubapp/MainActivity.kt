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
import org.osmdroid.config.Configuration
import si.feri.timpra.mbhubapp.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.time.LocalDateTime
import java.util.*
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

    private var timerSound: Timer? = null
    private var timerAcc: Timer? = null
    private var timerSimImg: Timer? = null
    private var timerSimAcc: Timer? = null
    private var timerSimSound: Timer? = null

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

        app.settingsSound.observe(this) {
            if (timerSound != null) timerSound!!.cancel()
            if (!it.enabled) {
                timerSound = null
                return@observe
            }
            timerSound = Timer()

            var flag = true
            timerSound!!.schedule(object : TimerTask() {
                override fun run() {
                    if (flag) {
                        flag = false
                        Looper.prepare()
                    }
                    recordAudio(it.duration)
                }
            }, 0, it.interval + it.duration)
        }

        //
        //
        //

        app.settingsAcc.observe(this) {
            if (timerAcc != null) timerAcc!!.cancel()
            if (!it.enabled) {
                timerAcc = null
                return@observe
            }
            timerAcc = Timer()
            var flag = true
            timerAcc!!.schedule(object : TimerTask() {
                override fun run() {
                    if (flag) {
                        flag = false
                        Looper.prepare()
                    }
                    recordAccelerometer(it.duration)
                }
            }, 0, it.interval + it.duration)
        }

        //
        //
        //

        app.simAccSettings.observe(this) {
            if (timerSimAcc != null) timerSimAcc!!.cancel()
            if (!it.enabled) {
                timerSimAcc = null
                return@observe
            }
            timerSimAcc = Timer()
            var flag = true
            timerSimAcc!!.schedule(object : TimerTask() {
                override fun run() {
                    if (flag) {
                        flag = false
                        Looper.prepare()
                    }

                    app.simulationPosition.value?.let { loc ->
                        app.simAccPath.value?.toPath()?.let { path ->
                            val data = try {
                                Files.readAllBytes(path)
                            } catch (e: IOException) {
                                e.printStackTrace()
                                Toast.makeText(
                                    applicationContext,
                                    "Failed to read acc file",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                app.updateSimAccPath(null)
                                return@run
                            }
                            app.sendAcc(
                                time = LocalDateTime.now(),
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                data = data
                            )
                        }
                    }
                }
            }, 0, it.interval + it.duration)
        }

        //
        //
        //

        app.simImgSettings.observe(this) {
            if (timerSimImg != null) timerSimImg!!.cancel()
            if (!it.enabled) {
                timerSimImg = null
                return@observe
            }
            timerSimImg = Timer()
            var flag = true
            timerSimImg!!.schedule(object : TimerTask() {
                override fun run() {
                    if (flag) {
                        flag = false
                        Looper.prepare()
                    }

                    app.simulationPosition.value?.let { loc ->
                        app.simImgPath.value?.toPath()?.let { path ->
                            val data = try {
                                Files.readAllBytes(path)
                            } catch (e: IOException) {
                                e.printStackTrace()
                                Toast.makeText(
                                    applicationContext,
                                    "Failed to read img file",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                app.updateSimImgPath(null)
                                return@run
                            }
                            app.sendImg(
                                time = LocalDateTime.now(),
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                data = data
                            )
                        }
                    }
                }
            }, 0, it.interval + it.duration)
        }

        //
        //
        //

        app.simSoundSettings.observe(this) {
            if (timerSimSound != null) timerSimSound!!.cancel()
            if (!it.enabled) {
                timerSimSound = null
                return@observe
            }
            timerSimSound = Timer()
            var flag = true
            timerSimSound!!.schedule(object : TimerTask() {
                override fun run() {
                    if (flag) {
                        flag = false
                        Looper.prepare()
                    }

                    app.simulationPosition.value?.let { loc ->
                        app.simSoundPath.value?.toPath()?.let { path ->
                            val data = try {
                                Files.readAllBytes(path)
                            } catch (e: IOException) {
                                e.printStackTrace()
                                Toast.makeText(
                                    applicationContext,
                                    "Failed to read sound file",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                app.updateSimSoundPath(null)
                                return@run
                            }
                            app.sendSound(
                                time = LocalDateTime.now(),
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                data = data
                            )
                        }
                    }
                }
            }, 0, it.interval + it.duration)
        }
    }

    private fun destroy() {
        if (!initialized) return

        //
        //
        //

        if (timerSound != null) timerSound!!.cancel()
        if (timerAcc != null) timerAcc!!.cancel()
        if (timerSimAcc != null) timerSimAcc!!.cancel()
        if (timerSimImg != null) timerSimImg!!.cancel()
        if (timerSimSound != null) timerSimSound!!.cancel()

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
            getLocTimeTag { time, loc ->
                app.sendSound(
                    time = time,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    data = json.toByteArray()
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
            getLocTimeTag { time, loc ->
                app.sendSound(
                    time = time,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    data = data
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

                getLocTimeTag { time, loc ->
                    app.sendImg(
                        time = time,
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        data = out.toByteArray()
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
    private fun getLocTimeTag(callback: (LocalDateTime, Location) -> Unit) {
        val time = LocalDateTime.now()
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    callback(time, location)
                }
            }
    }
}