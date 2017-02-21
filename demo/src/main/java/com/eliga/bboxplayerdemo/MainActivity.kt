package com.eliga.bboxplayerdemo

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.eliga.bbox.ExoMediaPlayer
import com.eliga.bbox.Media
import com.vodyasov.amr.AudiostreamMetadataManager
import com.vodyasov.amr.OnNewMetadataListener
import com.vodyasov.amr.UserAgent

/**
 * Created by eliga on 04/01/2017.
 */

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    private val listener: OnNewMetadataListener = object : OnNewMetadataListener {
        override fun onNewHeaders(stringUri: String?, name: MutableList<String>?,
                                  desc: MutableList<String>?, br: MutableList<String>?,
                                  genre: MutableList<String>?, info: MutableList<String>?) {
            Log.v("Laba diena", "labas")
        }

        override fun onNewStreamTitle(stringUri: String?, streamTitle: String?) {
            Log.v("Laba diena", "title " + streamTitle)
        }

    }

    override fun onResume() {
        super.onResume()
        val player = ExoMediaPlayer(this)
        val uri = Uri.parse("http://soundsession.center:8000/lite")

        player.setMedia(Media(uri))

        AudiostreamMetadataManager.getInstance()
                .setUri(uri)
                .setOnNewMetadataListener(listener)
                .start()

    }
}
