package com.adplayer.activity

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import com.adplayer.R
import com.adplayer.bean.ResultJSON
import com.adplayer.bean.ResultJSON.Companion.PARAMS_ERROR
import com.adplayer.fragment.CirclePLayer
import com.adplayer.utils.ConnectManager
import com.adplayer.utils.PlayManager
import com.bumptech.glide.Glide
import com.chichiangho.common.base.BaseActivity
import com.chichiangho.common.extentions.appCtx
import com.chichiangho.common.extentions.getTime
import com.chichiangho.common.extentions.screenWidth
import com.chichiangho.common.extentions.toast
import java.io.*
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException


class MainActivity : BaseActivity() {
    private val sourceList = ArrayList<String>()

    private lateinit var circlePLayer: CirclePLayer
    private lateinit var mapView: ImageView
    private var mCamera: Camera? = null

    fun getIP(context: Context): String? {

        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress().toString()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }

        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        if (System.currentTimeMillis() > "2018-10-01 00:00:00".getTime())
            finish()

        toast("IP: " + getIP(this) + ":" + ConnectManager.PORT_LISTEN_DEFAULT, Toast.LENGTH_LONG)

        mapView = findViewById(R.id.mapView)
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

                    for (x in parameters.supportedPictureSizes) {
                        if (x.width >= screenWidth) {
                            parameters.setPictureSize(x.width, x.height)
                            break
                        }
                    }

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
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT || numberOfCameras == 1) {//前置摄像头或唯一摄像头
                            mCamera = Camera.open(i)
                            mCamera?.let {
                                it.setPreviewDisplay(holder)
                                setCameraDisplayOrientation(this@MainActivity, i, it)
                                it.setDisplayOrientation(0)
                                it.startPreview()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                taking = false
            }
        })
// 这些代码在广告机上会崩溃
//        AndPermission.with(this)
//                .runtime()
//                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
//                .onGranted {
        initDatas()
//                }
//                .onDenied { finish() }
//                .start()
//
        ConnectManager.init()
//        OnLineReceiver.addOnLineListener(object : OnLineReceiver.ChangeListener {
//            override fun onLine() {
//                ConnectManager.stop()
//                ConnectManager.init()
//            }
//        })
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
                    ConnectManager.REFRESH -> {
                        initDatas()
                    }
                    else -> result(ResultJSON(ResultJSON.NO_SUCH_COMMAND))
                }
            }
        }
    }

    override fun onDestroy() {
        ConnectManager.stop()
        super.onDestroy()
    }

    private var lastTouchTime = 0L

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 259 && System.currentTimeMillis() - lastTouchTime > 1000) {//地图触摸按键
            lastTouchTime = System.currentTimeMillis()
            showMap()
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showMap() {
        if (File(PlayManager.getMapPath()).exists()) {
            mapView.visibility = View.VISIBLE
            circlePLayer.stop()
            Glide.with(this).load(PlayManager.getMapPath()).into(mapView)
            Handler().postDelayed({
                mapView.visibility = View.GONE
                circlePLayer.start()
            }, 10000)
        }
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

        try {
            mCamera?.setOneShotPreviewCallback { bytes: ByteArray, camera: Camera ->
                val size = camera.parameters.previewSize // 获取预览大小，若生成图片给的高宽和预览的高宽不一致，会导致生成的预览图出现花图的现象
                val w = size.width // 宽度
                val h = size.height
                val image = YuvImage(bytes, ImageFormat.NV21, w, h, null)
                val os = ByteArrayOutputStream()

                if (!image.compressToJpeg(Rect(0, 0, w, h), 100, os)) {
                    return@setOneShotPreviewCallback
                }
                val temp = os.toByteArray()

                val source1 = BitmapFactory.decodeByteArray(temp, 0, temp.size)

                val matrix = Matrix()
                matrix.postRotate(180F)
                val source = Bitmap.createBitmap(source1, 0, 0, source1.width, source1.height, matrix, true)

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
                mCamera?.stopPreview()
            } ?: let {
                result(ResultJSON(ResultJSON.CAMERA_NOT_READY))
                taking = false
            }
            mCamera?.startPreview()
        } catch (e: Exception) {
            result(ResultJSON(ResultJSON.CAMERA_NOT_READY))
        }
    }

    private fun play(name: String, text: String): ResultJSON {
        val path = PlayManager.getPath(name)
        if (path.isBlank())
            return ResultJSON(ResultJSON.TYPE_NOT_SUPPORT)
        return circlePLayer.play(path, text)
    }

    private fun setCameraDisplayOrientation(activity: Activity, cameraId: Int, camera: android.hardware.Camera) {
        val info = android.hardware.Camera.CameraInfo()
        android.hardware.Camera.getCameraInfo(cameraId, info)
        val rotation = activity.windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360   // compensate the mirror
        } else {
            // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(result)
    }
}
