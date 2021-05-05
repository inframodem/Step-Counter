package com.example.stepcounter

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var sessionSteps = 0f
    private var previousSessionSteps = 0f
    private var running = false
    private var isOff = true
    private var currentSteps = 0

    private var MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 143

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadSteps()
        resetAndPause()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if(savedInstanceState != null){
            currentSteps = savedInstanceState.getInt("CURRENT_STEPS", 0)
            button2.text = ("$currentSteps")
            isOff = savedInstanceState.getBoolean("IS_OFF")
            running = savedInstanceState.getBoolean("RUNNING")
        }
    }



    override fun onResume(){
    super.onResume()
    running = true
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION)
    }
        var stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if(stepSensor == null){
            Toast.makeText(this,"Step Sensor Not Available", Toast.LENGTH_SHORT).show()
        }
        else{
            sensorManager?.registerListener(this,stepSensor, SensorManager.SENSOR_DELAY_UI)
        }

    }

    override fun onPause(){
        super.onPause()
        running = false
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(running && !isOff){
            sessionSteps = event!!.values[0]
            currentSteps = sessionSteps.toInt() - previousSessionSteps.toInt()
            button2.text = ("$currentSteps")
        }
        if(running && isOff){
            sessionSteps = event!!.values[0]
            previousSessionSteps += ((sessionSteps - currentSteps.toFloat()) - previousSessionSteps)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun resetAndPause(){
         button2.setOnClickListener{
            isOff = !isOff
             if(!isOff)
                Toast.makeText(this,"Resuming", Toast.LENGTH_SHORT).show()
             if(isOff)
                 Toast.makeText(this,"Pausing", Toast.LENGTH_SHORT).show()
         }

        button2.setOnLongClickListener{
             previousSessionSteps = sessionSteps
             button2.text = 0.toString()
             saveSteps()

             true
         }
    }

    private fun saveSteps() {
        val sharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("stepKey", previousSessionSteps)
        editor.apply()
    }
    private fun loadSteps() {
        val sharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE)
        val savedSteps = sharedPreferences.getFloat("stepKey", 0f)
        previousSessionSteps = savedSteps;
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putInt("CURRENT_STEPS", currentSteps)
        outState.putBoolean("IS_OFF", isOff)
        outState.putBoolean("RUNNING", running)
    }
}