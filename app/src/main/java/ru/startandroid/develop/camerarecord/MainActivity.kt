package ru.startandroid.develop.camerarecord

import android.app.Activity
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import java.io.File
import java.io.FileOutputStream

class MainActivity : Activity() {
    private var surfaceView: SurfaceView? = null
    var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null
    private var photoFile: File? = null
    private var videoFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pictures: File = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        photoFile = File(pictures, "myphoto.jpg")
        videoFile = File(pictures, "myvideo.3gp")
        surfaceView = findViewById<View>(R.id.surfaceView) as SurfaceView
        val holder = surfaceView!!.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    camera!!.setPreviewDisplay(holder)
                    camera!!.startPreview()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int,
                width: Int, height: Int,
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })
    }

    override fun onResume() {
        super.onResume()
        camera = Camera.open()
    }

    override fun onPause() {
        super.onPause()
        releaseMediaRecorder()
        if (camera != null) camera!!.release()
        camera = null
    }

    fun onClickPicture(view: View?) {
        camera!!.takePicture(null, null, PictureCallback { data, _ ->
            try {
                val fos = FileOutputStream(photoFile)
                fos.write(data)
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun onClickStartRecord(view: View?) {
        if (prepareVideoRecorder()) {
            mediaRecorder!!.start()
        } else {
            releaseMediaRecorder()
        }
    }

    fun onClickStopRecord(view: View?) {
        if (mediaRecorder != null) {
            mediaRecorder!!.stop()
            releaseMediaRecorder()
        }
    }

    private fun prepareVideoRecorder(): Boolean {
        camera!!.unlock()
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setCamera(camera)
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mediaRecorder!!.setProfile(CamcorderProfile
            .get(CamcorderProfile.QUALITY_HIGH))
        mediaRecorder!!.setOutputFile(videoFile!!.absolutePath)
        mediaRecorder!!.setPreviewDisplay(surfaceView!!.holder.surface)
        try {
            mediaRecorder!!.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
            releaseMediaRecorder()
            return false
        }
        return true
    }

    private fun releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder!!.reset()
            mediaRecorder!!.release()
            mediaRecorder = null
            camera!!.lock()
        }
    }
}