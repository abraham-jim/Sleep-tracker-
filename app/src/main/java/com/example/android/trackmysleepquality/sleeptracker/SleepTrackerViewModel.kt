

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(

        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

        private var viewModelJob = Job()

        override fun onCleared() {
                super.onCleared()
                viewModelJob.cancel()
        }

        private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        private var tonight = MutableLiveData<SleepNight?>()
        private val nights = database.getAllNights()

        val nightsString = Transformations.map(nights) { nights ->
                formatNights(nights,application.resources)
        }

        private val _navigateToSleepQuality = MutableLiveData<SleepNight>()

        val navigateToSleepQuality: LiveData<SleepNight>
                get() = _navigateToSleepQuality

        fun doneNavigating() {
                _navigateToSleepQuality.value = null
        }

        init {
                initializeTonight()
        }

        private fun initializeTonight() {
                uiScope.launch {
                        tonight.value = getTonightFromDatabase()
                }
        }
        /**
         *  Handling the case of the stopped app or forgotten recording,
         *  the start and end times will be the same.j
         *
         *  If the start time and end time are not the same, then we do not have an unfinished
         *  recording.
         */

        private suspend fun getTonightFromDatabase(): SleepNight? {
                return withContext(Dispatchers.IO) {

                        var night = database.getTonight()

                        if (night?.endTimeMilli != night?.startTimeMilli) {
                                night = null
                        }
                        night
                }

        }
        /**
         * Executes when the START button is clicked.
         */
        fun onStartTracking() {
                uiScope.launch {
                        // Create a new night, which captures the current time,
                        // and insert it into the database.
                        val newNight = SleepNight()
                        insert(newNight)
                        tonight.value = getTonightFromDatabase()

                }
        }

        private suspend fun insert(night: SleepNight) {
                withContext(Dispatchers.IO) {
                        database.insert(night)
                }
        }
        /**
         * Executes when the STOP button is clicked.
         */
        fun onStopTracking() {
                uiScope.launch {
                        // In Kotlin, the return@label syntax is used for specifying which function among
                        // several nested ones this statement returns from.
                        // In this case, we are specifying to return from launch(),
                        // not the lambda.
                        val oldNight = tonight.value ?: return@launch

                        // Update the night in the database to add the end time.
                        oldNight.endTimeMilli = System.currentTimeMillis()
                        update(oldNight)
                        // Set state to navigate to the SleepQualityFragment.
                        _navigateToSleepQuality.value = oldNight
                }
        }
        private suspend fun update(night: SleepNight) {
                withContext(Dispatchers.IO) {
                        database.update(night)
                }
        }
        /**
         * Executes when the CLEAR button is clicked.
         */
        fun onClear() {
                uiScope.launch {
                        // Clear the database table.
                        clear()
                        // And clear tonight since it's no longer in the database
                        tonight.value = null
                }
        }

        suspend fun clear() {
                withContext(Dispatchers.IO) {
                        database.clear()
                }
        }


        /**
         * ========================================================================
         */

        /**
         * ViewModel for SleepTrackerFragment.
         */
//        class SleepTrackerViewModel(
//                val database: SleepDatabaseDao,
//                application: Application) : AndroidViewModel(application) {
//
//                private var tonight = MutableLiveData<SleepNight?>()
//
//                private val nights = database.getAllNights()
//
//                /**
//                 * Converted nights to Spanned for displaying.
//                 */
//                val nightsString = Transformations.map(nights) { nights ->
//                        formatNights(nights, application.resources)
//                }
//
//                /**
//                 * Variable that tells the Fragment to navigate to a specific [SleepQualityFragment]
//                 *
//                 * This is private because we don't want to expose setting this value to the Fragment.
//                 */
//                private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
//
//                /**
//                 * If this is non-null, immediately navigate to [SleepQualityFragment] and call [doneNavigating]
//                 */
//                val navigateToSleepQuality: LiveData<SleepNight>
//                        get() = _navigateToSleepQuality
//
//                /**
//                 * Call this immediately after navigating to [SleepQualityFragment]
//                 *
//                 * It will clear the navigation request, so if the user rotates their phone it won't navigate
//                 * twice.
//                 */
//                fun doneNavigating() {
//                        _navigateToSleepQuality.value = null
//                }
//
//                init {
//                        initializeTonight()
//                }
//
//                private fun initializeTonight() {
//                        viewModelScope.launch {
//                                tonight.value = getTonightFromDatabase()
//                        }
//                }
//
//                /**
//                 *  Handling the case of the stopped app or forgotten recording,
//                 *  the start and end times will be the same.j
//                 *
//                 *  If the start time and end time are not the same, then we do not have an unfinished
//                 *  recording.
//                 */
//                private suspend fun getTonightFromDatabase(): SleepNight? {
//                        var night = database.getTonight()
//                        if (night?.endTimeMilli != night?.startTimeMilli) {
//                                night = null
//                        }
//                        return night
//                }
//
//
//                private suspend fun clear() {
//                        database.clear()
//                }
//
//                private suspend fun update(night: SleepNight) {
//                        database.update(night)
//                }
//
//                private suspend fun insert(night: SleepNight) {
//                        database.insert(night)
//                }
//
//                /**
//                 * Executes when the START button is clicked.
//                 */
//                fun onStartTracking() {
//                        viewModelScope.launch {
//                                // Create a new night, which captures the current time,
//                                // and insert it into the database.
//                                val newNight = SleepNight()
//
//                                insert(newNight)
//
//                                tonight.value = getTonightFromDatabase()
//                        }
//                }
//
//                /**
//                 * Executes when the STOP button is clicked.
//                 */
//                fun onStopTracking() {
//                        viewModelScope.launch {
//                                // In Kotlin, the return@label syntax is used for specifying which function among
//                                // several nested ones this statement returns from.
//                                // In this case, we are specifying to return from launch(),
//                                // not the lambda.
//                                val oldNight = tonight.value ?: return@launch
//
//                                // Update the night in the database to add the end time.
//                                oldNight.endTimeMilli = System.currentTimeMillis()
//
//                                update(oldNight)
//
//                                // Set state to navigate to the SleepQualityFragment.
//                                _navigateToSleepQuality.value = oldNight
//                        }
//                }
//
//                /**
//                 * Executes when the CLEAR button is clicked.
//                 */
//                fun onClear() {
//                        viewModelScope.launch {
//                                // Clear the database table.
//                                clear()
//
//                                // And clear tonight since it's no longer in the database
//                                tonight.value = null
//                        }
//                }
//
//                /**
//                 */
//        }
//=====================================================================================
//
        //
        //        val database: SleepDatabaseDao,
//        application: Application) : AndroidViewModel(application) {
//
//        //TODO (01) Declare Job() and cancel jobs in onCleared().
//        /**
//         */
//
//        //TODO (02) Define uiScope for coroutines.
//        /**
//         *
//         *
//         */
//
//        //TODO (03) Create a MutableLiveData variable tonight for one SleepNight.
//        private var tonight = MutableLiveData<SleepNight?>()
//
//        //TODO (04) Define a variable, nights. Then getAllNights() from the database
//        //and assign to the nights variable.
//        private val nights = database.getAllNights()
//
//        //TODO (05) In an init block, initializeTonight(), and implement it to launch a coroutine
//        //to getTonightFromDatabase().
//        /**
//         * Converted nights to Spanned for displaying.
//         */
//        val nightsString = Transformations.map(nights) { nights ->
//                formatNights(nights, application.resources)
//        }
//
//        //TODO (06) Implement getTonightFromDatabase()as a suspend function.
//        init {
//                initializeTonight()
//        }
//
//        //TODO (07) Implement the click handler for the Start button, onStartTracking(), using
//        //coroutines. Define the suspend function insert(), to insert a new night into the database.
//        private fun initializeTonight() {
//                viewModelScope.launch {
//                        tonight.value = getTonightFromDatabase()
//                }
//        }
//
//        //TODO (08) Create onStopTracking() for the Stop button with an update() suspend function.
//        /**
//         *  Handling the case of the stopped app or forgotten recording,
//         *  the start and end times will be the same.j
//         *
//         *  If the start time and end time are not the same, then we do not have an unfinished
//         *  recording.
//         */
//        private suspend fun getTonightFromDatabase(): SleepNight? {
//                var night = database.getTonight()
//                if (night?.endTimeMilli != night?.startTimeMilli) {
//                        night = null
//                }
//                return night
//        }
//
//        //TODO (09) For the Clear button, created onClear() with a clear() suspend function.
//
//        //TODO (12) Transform nights into a nightsString using formatNights().
//        private suspend fun clear() {
//                database.clear()
//        }
//
//        private suspend fun update(night: SleepNight) {
//                database.update(night)
//        }
//
//        private suspend fun insert(night: SleepNight) {
//                database.insert(night)
//        }
//
//        /**
//         * Executes when the START button is clicked.
//         */
//        fun onStartTracking() {
//                viewModelScope.launch {
//                        // Create a new night, which captures the current time,
//                        // and insert it into the database.
//                        val newNight = SleepNight()
//
//                        insert(newNight)
//
//                        tonight.value = getTonightFromDatabase()
//                }
//        }
//
//        /**
//         * Executes when the STOP button is clicked.
//         */
//        fun onStopTracking() {
//                viewModelScope.launch {
//                        // In Kotlin, the return@label syntax is used for specifying which function among
//                        // several nested ones this statement returns from.
//                        // In this case, we are specifying to return from launch(),
//                        // not the lambda.
//                        val oldNight = tonight.value ?: return@launch
//
//                        // Update the night in the database to add the end time.
//                        oldNight.endTimeMilli = System.currentTimeMillis()
//
//                        update(oldNight)
//                }
//        }
//
//        /**
//         * Executes when the CLEAR button is clicked.
//         */
//        fun onClear() {
//                viewModelScope.launch {
//                        // Clear the database table.
//                        clear()
//
//                        // And clear tonight since it's no longer in the database
//                        tonight.value = null
//                }
//        }



}



