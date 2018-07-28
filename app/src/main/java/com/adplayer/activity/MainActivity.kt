package com.adplayer.activity

import android.Manifest
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.os.Bundle
import android.os.Handler
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.adplayer.R
import com.adplayer.bean.ResultJSON
import com.adplayer.bean.ResultJSON.Companion.PARAMS_ERROR
import com.adplayer.fragment.CirclePLayer
import com.adplayer.utils.ConnectManager
import com.adplayer.utils.PlayManager
import com.chichiangho.common.base.BaseActivity
import com.chichiangho.common.extentions.appCtx
import com.chichiangho.common.extentions.getTime
import com.yanzhenjie.permission.AndPermission
import java.io.File
import android.graphics.Bitmap
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : BaseActivity() {
    private val sourceList = ArrayList<String>()

    private lateinit var circlePLayer: CirclePLayer
    private var mCamera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (System.currentTimeMillis() > "2018-09-01 00:00:00".getTime())
            finish()

        circlePLayer = findViewById(R.id.circle_player)
        circlePLayer.setDelay(5000).init(fragmentManager)

        val surfaceV = findViewById<SurfaceView>(R.id.surface_view)
        val surfaceHolder = surfaceV.holder
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                mCamera?.let {
                    val parameters = it.parameters// 获取各项参数
                    parameters.pictureFormat = PixelFormat.JPEG // 设置图片格式
                    parameters.jpegQuality = 100 // 设置照片质量

                    /**
                     * 以下不设置在某些机型上报错
                     */
                    val mPreviewHeight = parameters.previewSize.height
                    val mPreviewWidth = parameters.previewSize.width
                    parameters.setPreviewSize(mPreviewWidth, mPreviewHeight)
                    parameters.setPictureSize(mPreviewWidth, mPreviewHeight)

                    it.parameters = parameters
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                mCamera?.stopPreview()
                mCamera?.unlock()
                mCamera?.release()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                try {
                    val cameraInfo = Camera.CameraInfo()
                    val numberOfCameras = Camera.getNumberOfCameras()
                    for (i in 0 until numberOfCameras) {
                        Camera.getCameraInfo(i, cameraInfo)
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//前置摄像头
                            mCamera = Camera.open(i)
                            mCamera?.setPreviewDisplay(holder)
                            mCamera?.setDisplayOrientation(0)
                            mCamera?.startPreview()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                taking = false
            }
        })

        AndPermission.with(this)
                .runtime()
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .onGranted {
                    initDatas()
                }
                .onDenied { finish() }
                .start()

        ConnectManager.init()
        ConnectManager.registerCommandListener { command, params, result ->
            runOnUiThread {
                when (command) {
                    ConnectManager.COMMAND_PLAY -> {
                        if (params.has("path"))
                            result(play(params.optString("path"), params.optString("text")))
                        else
                            result(ResultJSON(PARAMS_ERROR))
                    }
                    ConnectManager.COMMAND_TACK_PICTURE -> {
                        takePic(result)
                    }
                    ConnectManager.COMMAND_SHOW_MAP -> {
                        showMap(result)
                    }
                    ConnectManager.REFRESH -> {
                        initDatas()
                    }
                    else -> result(ResultJSON(ResultJSON.NO_SUCH_COMMAND))
                }
            }
        }
    }

    private fun showMap(result: (result: ResultJSON) -> Unit) {
    }

    private fun initDatas() {
        PlayManager.getPics {
            sourceList.clear()
            sourceList.addAll(it.sortedArray())

            PlayManager.getVideos {
                sourceList.addAll(it)
                circlePLayer.setData(sourceList).start()
            }
        }
    }

    private var taking = false
    private fun takePic(result: (result: ResultJSON) -> Unit) {
        if (taking) {
            result(ResultJSON(ResultJSON.CAMERA_NOT_READY))
            return
        }
        taking = true
        val path = appCtx.getExternalFilesDir("takePhoto").absolutePath + "/" + System.currentTimeMillis() + ".png"

        mCamera?.takePicture(null, null, PictureCallback { data, camera ->
            camera.startPreview()
            val source = BitmapFactory.decodeByteArray(data, 0, data.size)
            val file = File(path)
            if (!file.exists())
                file.createNewFile()
            var out: FileOutputStream? = null
            try {
                out = FileOutputStream(file)
                source.compress(Bitmap.CompressFormat.JPEG, 90, out)
                result(ResultJSON(ResultJSON.TAKE_PIC, path))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                result(ResultJSON(ResultJSON.NO_SUCH_FILE))
            } finally {
                try {
                    out?.flush()
                    out?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            taking = false
            file.delete()
        }) ?: let {
            result(ResultJSON(ResultJSON.CAMERA_NOT_READY))
        }
    }

    private fun play(name: String, text: String): ResultJSON {
        val path = PlayManager.getPath(name)
        if (path.isBlank())
            return ResultJSON(ResultJSON.TYPE_NOT_SUPPORT)
        return circlePLayer.play(path, text)
    }
}
