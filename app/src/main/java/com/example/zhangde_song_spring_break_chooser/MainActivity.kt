package com.example.zhangde_song_spring_break_chooser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.RecognizerIntent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.zhangde_song_spring_break_chooser.databinding.ActivityMainBinding
import java.util.Locale
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), SensorEventListener, TextToSpeech.OnInitListener {

    lateinit var binding: ActivityMainBinding
    private lateinit var mSensorManager : SensorManager
    private var mAccelerometer : Sensor ?= null

    val shanghai = floatArrayOf(1.3624398F, 103.811966F)
    val beijing = floatArrayOf(25.102371F, 121.54854F)
    val luoyang = floatArrayOf(3.1579285F, 101.71011F)
    val china = arrayOf(shanghai,beijing,luoyang)

    val boston = floatArrayOf(42.355457F, (-71.0656455573191).toFloat())
    val la = floatArrayOf(1.3624398F, 103.811966F)
    val was = floatArrayOf(3.1579285F, 101.71011F)
    val usa = arrayOf(boston, la, was)

    val pairs = floatArrayOf(48.86233F, 2.3375907F)
    val tower = floatArrayOf(5.1020093F, 9.861543F)
    val church = floatArrayOf((-11.653199563210247).toFloat(), 43.263332F)
    val french = arrayOf(pairs,tower,church)

    val gate = floatArrayOf(40.41669F, (-3.693218681138702).toFloat())
    val king = floatArrayOf(19.416355F, (-99.2065305695433).toFloat())
    val art = floatArrayOf(3.5075827F, (-73.01713172001023).toFloat())
    val spanish = arrayOf(gate,king,art)

    private var country = "usa"

    //Text to Speech
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get reference of the service
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // focus in accelerometer
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        //Init Text to Speech
        tts = TextToSpeech(this, this)

        if (mAccelerometer == null) {
            // Handle the case where the accelerometer is not available on the device
            Log.d("Sensor - TYPE_ACCELEROMETER","Accelerometer not available on this device")
        }

        val extras = intent.extras
        if (extras != null) {
            country = extras.getString("country").toString()
            //The key argument here must match that used in the other activity
        }

        Log.d("Country",country)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result->
            if (result.resultCode == Activity.RESULT_OK){
                val results = result.data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                ) as ArrayList<String>

                binding.editText.setText(results[0])
            }
        }

        binding.mixIV.setOnClickListener {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, resources.configuration.locale.language)
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Something")
                result.launch(intent)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }



        binding.chineseButton.setOnClickListener {
            setLocale(this@MainActivity,"zh")
            finish()
            intent = getIntent()
            intent.putExtra("country", "china")
            startActivity(intent)
        }

        binding.englishButton.setOnClickListener {
            setLocale(this@MainActivity,"en")
            finish()
            intent = getIntent()
            intent.putExtra("country", "usa")
            startActivity(intent)
        }

        binding.spanishButton.setOnClickListener {
            setLocale(this@MainActivity,"es")
            finish()
            intent = getIntent()
            intent.putExtra("country", "spanish")
            startActivity(intent)
        }

        binding.frenchButton.setOnClickListener {
            setLocale(this@MainActivity,"fr")
            finish()
            intent = getIntent()
            intent.putExtra("country", "french")
            startActivity(intent)
        }

        }override fun onResume() {
        super.onResume()

        // Register the sensor listener when the activity is resumed
        mAccelerometer?.let {
            mSensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    //tts
    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
    override fun onPause() {
        super.onPause()

        // Unregister the sensor listener when the activity is paused
        mSensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        Log.d("Sensor - TYPE_ACCELEROMETER", "----------------------")
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val accelerationX = event.values[0]
                val accelerationY = event.values[1]
                val accelerationZ = event.values[2]
                //Log.d("Sensor - TYPE_ACCELEROMETER", "$accelerationX $accelerationY $accelerationZ")
                // Process accelerometer data
                if (sqrt((accelerationX * accelerationX + accelerationY * accelerationY + accelerationZ * accelerationZ).toDouble()) > 12)
                {

                    var city = getLang()

                    jumpToMapAtRandomLocation(city)
                }
            }
        }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
// Intent Jump to Map Activity
    private fun jumpToMapAtRandomLocation( city: FloatArray){
            var latitude = city[0]
            var longitude = city[1]
            val uri = java.lang.String.format(Locale.getDefault(), "geo:%f,%f", latitude, longitude)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            this@MainActivity.startActivity(intent)
        }

    private fun setLocale(context: Context, langCode:String){
        var local = Locale(langCode)
        Locale.setDefault(local)
        var resources = context.resources
        var config = resources.configuration
        config.setLocale(local)
        config.setLayoutDirection(local)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun getLang() : FloatArray{
        var c :  Array<FloatArray>
        var id : Int
        when(country){
            "china" -> {
                c = china
                speakOut("你好")
            }
            "spanish" -> {
                c = spanish
                speakOut("bonjour")
            }
            "french" -> {
                c = french
                speakOut("Hola")
            }
            else -> {
                c = usa
                speakOut("Hello")
            }
        }
        var city = c.random()
        return city
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Language data is missing or the language is not supported.
                // Handle this scenario accordingly.
            }
        } else {
            // Initialization failed.
            // Handle this scenario accordingly.
        }
    }


}