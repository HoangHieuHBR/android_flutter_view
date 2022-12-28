package com.example.android_app_flutter_view

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.StringCodec
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var flutterEngine: FlutterEngine

    private lateinit var flutterView: FlutterView
    private var counter: Int = 0
    private val CHANNEL = "increment"
    private val EMPTY_MESSAGE = ""
    private val PING = "ping"
    private lateinit var messageChannel: BasicMessageChannel<String>

    private fun getArgsFromIntent(intent: Intent): Array<String>? {
        val args = ArrayList<String>()
        if (intent.getBooleanExtra("trace-startup", false)) {
            args.add("--trace-startup")
        }
        if (intent.getBooleanExtra("start-paused", false)) {
            args.add("--start-paused")
        }
        if (intent.getBooleanExtra("enable-dart-profiling", false)) {
            args.add("--enable-dart-profiling")
        }
        return if (args.isNotEmpty()) {
            val argsArray = arrayOfNulls<String>(args.size)
            args.toArray(argsArray)
        } else {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = getArgsFromIntent(intent)
        flutterEngine = FlutterEngine(this, args)
        flutterEngine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())

        setContentView(R.layout.flutter_view_layout)
        supportActionBar?.hide()

        flutterView = findViewById(R.id.flutter_view)
        flutterView.attachToFlutterEngine(flutterEngine!!)

        messageChannel =
            BasicMessageChannel(flutterEngine!!.dartExecutor, CHANNEL, StringCodec.INSTANCE)
        messageChannel.setMessageHandler { _, reply ->
            onFlutterIncrement()
            reply.reply(EMPTY_MESSAGE)
        }

        val fab = findViewById<FloatingActionButton>(R.id.button)
        fab.setOnClickListener { sendAndroidIncrement() }

    }

    private fun sendAndroidIncrement() {
        messageChannel.send(PING)
    }

    private fun onFlutterIncrement() {
        counter++
        val textView = findViewById<TextView>(R.id.button_tap)
        val value = "Flutter button tapped $counter${if (counter == 1) " time" else " times"}"
        textView.text = value
    }

    override fun onResume() {
        super.onResume()
        flutterEngine.lifecycleChannel.appIsResumed()
    }

    override fun onPause() {
        super.onPause()
        flutterEngine.lifecycleChannel.appIsInactive()
    }

    override fun onStop() {
        super.onStop()
        flutterEngine.lifecycleChannel.appIsPaused()
    }

    override fun onDestroy() {
        flutterView.detachFromFlutterEngine()
        super.onDestroy()
    }
}

