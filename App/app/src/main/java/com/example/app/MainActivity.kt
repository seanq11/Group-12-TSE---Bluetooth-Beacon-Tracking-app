// adapted from https://github.com/Estimote/Android-Indoor-SDK/tree/master/example/indoorapp
package com.example.app

import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.estimote.indoorsdk.EstimoteCloudCredentials
import com.estimote.indoorsdk.IndoorLocationManagerBuilder
import com.estimote.indoorsdk_module.algorithm.OnPositionUpdateListener
import com.estimote.indoorsdk_module.algorithm.ScanningIndoorLocationManager
import com.estimote.indoorsdk_module.cloud.CloudCallback
import com.estimote.indoorsdk_module.cloud.EstimoteCloudException
import com.estimote.indoorsdk_module.cloud.IndoorCloudManagerFactory
import com.estimote.indoorsdk_module.cloud.Location
import com.estimote.indoorsdk_module.cloud.LocationPosition
import com.estimote.indoorsdk_module.view.IndoorLocationView
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory

import com.example.myapplication.R

// IndoorApplication ///////////////////////////////////////////////////////////////////////////////
// application data for estimote is stored here for later use
class IndoorApplication : Application() {
    // map with name and location, and estimote cloud credentials
    val locationsById: MutableMap<String, Location> = mutableMapOf()
    val cloudCredentials = EstimoteCloudCredentials(
        "test-indoor-app-77p",
        "26e6132406e0e817506e5c91d5994d47")
}

// LocationListAdapter /////////////////////////////////////////////////////////////////////////////
// the location list adapter helps display location data via RecyclerView Adapter abstract class
class LocationListAdapter (private var mLocations: List<Location>):
    RecyclerView.Adapter<LocationListAdapter.LocationHolder>() {
    // empty lambda which is assigned later via setLocations
    private var mListener: ((String) -> Unit)? = null

    // internal class LocationHolder used to store specific location data from RecycleView
    // ViewHolder abstract class
    class LocationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.location_name) as TextView
        val id  : TextView = itemView.findViewById(R.id.location_id) as TextView
        fun setOnClickListener(listener: View.OnClickListener) {
            itemView.setOnClickListener(listener)
        }
    }

    // creates a LocationHolder Object from the aforementioned class from a ViewGroup object param
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationHolder {
        return LocationHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.location_list_item, parent, false))
    }

    // modifies the view holder by adding data about the list of locations
    override fun onBindViewHolder(holder: LocationHolder, position: Int) {
        holder.name.text = mLocations[position].name
        holder.id.text = mLocations[position].identifier
        holder.setOnClickListener { mListener?.invoke(mLocations[position].identifier) }
    }

    // shows size of the number of locations
    override fun getItemCount(): Int {
        return mLocations.size
    }

    // resets the locations to a new list of locations
    @SuppressLint("NotifyDataSetChanged")
    fun setLocations(list: List<Location>) {
        this.mLocations = list
        notifyDataSetChanged()
    }

    // resets the mListener lambda
    fun setOnClickListener(listener: (String) -> Unit) {
        mListener = listener
    }
}

// LocationListActivity class //////////////////////////////////////////////////////////////////////
// allows the user to see a list of locations
class LocationListActivity: AppCompatActivity() {

    // internal variable declaration for views and adapter
    private lateinit var mRecyclerView   : RecyclerView
    private lateinit var mAdapter        : LocationListAdapter
    private lateinit var mNoLocationsView: TextView
    private lateinit var mLayoutManager  : RecyclerView.LayoutManager

    // overridden constructor for LocationListActivity, sets the LocationListAdapter with an empty
    // set of data to start with
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_list)

        mNoLocationsView = findViewById(R.id.no_locations_view)
        mRecyclerView    = findViewById(R.id.my_recycler_view)

        mRecyclerView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLayoutManager
        mAdapter = LocationListAdapter(emptyList())
        mRecyclerView.adapter = (mAdapter)

        mAdapter.setOnClickListener { locationId ->
            startActivity(MainActivity.createIntent(this, locationId))
        }
    }

    // on start the locations are added to the LocationListAdapter if there are any
    override fun onStart() {
        super.onStart()
        val locations = (application as IndoorApplication).locationsById.values.toList()
        if (locations.isEmpty()) {
            mNoLocationsView.visibility = View.VISIBLE
            mRecyclerView.visibility = View.GONE
        } else {
            mAdapter.setLocations((application as IndoorApplication).locationsById.values.toList())
        }
    }
}

// Splash Activity /////////////////////////////////////////////////////////////////////////////////
// Start screen while the app is loading
@SuppressLint("CustomSplashScreen")
class SplashActivity: AppCompatActivity() {

    // overridden constructor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LUKE", "splash")
        // Make actionbar invisible.
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        supportActionBar?.hide()
        setContentView(R.layout.activity_splash)

        // Create object for communicating with Estimote cloud.
        // IMPORTANT - you need to put here your Estimote Cloud credentials.
        val cloudManager = IndoorCloudManagerFactory()
            .create(applicationContext, (application as IndoorApplication).cloudCredentials)

        // Launch request for all locations connected to your account.
        // If you don't see any - check your cloud account - maybe you should create those locations
        // first?
        cloudManager.getAllLocations(object : CloudCallback<List<Location>> {
            override fun success(t: List<Location>) {
                // Take location objects and map them to their identifiers
                val locationIds = t.associateBy { it.identifier }

                // save mapped locations to global pseudo "storage". You can do this in many various
                // way
                (application as IndoorApplication).locationsById.putAll(locationIds)

                // If all is fine, go ahead and launch activity with list of your locations
                startMainActivity()
            }

            override fun failure(serverException: EstimoteCloudException) {
                val message = "Unable to fetch location data from cloud. Check your internet " +
                        "connection and make sure you initialised our SDK with your AppId/AppToken"
                // For the sake of this demo, you need to make sure you have an internet connection
                // and AppID/AppToken set
                Toast.makeText(this@SplashActivity, message, Toast.LENGTH_LONG).show()
            }
        })
    }

    // launches activity
    private fun startMainActivity() {
        startActivity(Intent(this, LocationListActivity::class.java))
    }
}

// MainActivity ////////////////////////////////////////////////////////////////////////////////////
class MainActivity: AppCompatActivity() {

    private lateinit var backendServer        : BackendServer
    private lateinit var indoorLocationView   : IndoorLocationView
    private lateinit var indoorLocationManager: ScanningIndoorLocationManager
    private lateinit var location             : Location

    var x : Double = 0.0
    var y : Double = 0.0

    companion object {
        const val intentKeyLocationId = "location_id"
        fun createIntent(context: Context, locationId: String): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(intentKeyLocationId, locationId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        backendServer.constructor(applicationContext)

        // Declare notification that will be displayed in user's notification bar.
        // You can modify it as you want/

        val notificationManager = getSystemService (
            Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "IndoorAppChannelID",
            "IndoorAppChannel",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Indoor App Notification Channel"
            notificationManager.createNotificationChannel(this)
        }

        val notification = Notification.Builder (
            this,
            channel.id
        ).apply {
            setSmallIcon(com.estimote.indoorsdk.R.drawable.beacon_beetroot_small)
            setContentTitle("Group 12 TSE")
            setContentText("Indoor location is running")
        }.build()

        // Get location id from intent and get location object from list of locations
        setupLocation()

        // Init indoor location view here
        indoorLocationView = findViewById(R.id.indoor_view)

        // Give location object to your view to draw it on your screen
        indoorLocationView.setLocation(location)

        // Create IndoorManager object.
        // Long story short - it takes list of scanned beacons, does the magic and returns estimated
        // position (x,y)
        // You need to setup it with your app context,  location data object,
        // and your cloud credentials that you declared in IndoorApplication.kt file
        // we are using .withScannerInForegroundService(notification)
        // this will allow for scanning in background and will ensures that the system won't kill
        // the scanning.
        // You can also use .withSimpleScanner() that will be handled without service.
        indoorLocationManager = IndoorLocationManagerBuilder(this,
            location, (application as IndoorApplication).cloudCredentials)
            .withScannerInForegroundService(notification)
            .build()

        //location.beacons
        val a = location.beacons[0].toString()
        val b = location.beacons[1].toString()
        val c = location.beacons[2].toString()

        Log.d("LUKE", "beacons:\n\t$a\n\t$b\n\t$c")

        // Hook the listener for position update events
        indoorLocationManager.setOnPositionUpdateListener(object: OnPositionUpdateListener {
            override fun onPositionOutsideLocation() {
                Log.d("LUKE","outside location")
                indoorLocationView.hidePosition()
            }

            override fun onPositionUpdate(locationPosition: LocationPosition) {
                x = locationPosition.x
                y = locationPosition.y
                Log.d("LUKE","$x, $y")

                //reports position to backend server
                backendServer.reportLocation(x, y)
                indoorLocationView.updatePosition(locationPosition)
            }
        })

        // Check if bluetooth is enabled, location permissions are granted, etc.
        RequirementsWizardFactory.createEstimoteRequirementsWizard()
            .fulfillRequirements(this,
                onRequirementsFulfilled = {
                    Log.d("LUKE", "Connection requirement fulfilled")
                    indoorLocationManager.startPositioning()

                },
                onRequirementsMissing = {
                    Log.d("LUKE", "Unable to scan for beacons. Requirements missing: " +
                            it.joinToString())
                    Toast.makeText(applicationContext,
                        "Unable to scan for beacons. Requirements missing: " +
                        it.joinToString(),
                        Toast.LENGTH_SHORT).show()
                },
                onError = {
                    Log.d("LUKE","Unable to scan for beacons. Error: " + it.message)
                    Toast.makeText(applicationContext,
                        "Unable to scan for beacons. Error: " + it.message,
                        Toast.LENGTH_SHORT).show()
                }
            )
    }

    private fun setupLocation() {
        // get id of location to show from intent

        // get object of location. If something went wrong, we build empty location with no data.
        location = (application as IndoorApplication)
            .locationsById[intent.extras?.getString(intentKeyLocationId)] ?: buildEmptyLocation()

        // Set the Activity title to you location name
        title = location.name
    }

    // creates an empty location
    private fun buildEmptyLocation(): Location {
        return Location("", "", true, "", 0.0,
            emptyList(), emptyList(), emptyList())
    }

    // destructor to when the MainActivity is removed from the heap
    override fun onDestroy() {
        indoorLocationManager.stopPositioning()
        super.onDestroy()
    }
}