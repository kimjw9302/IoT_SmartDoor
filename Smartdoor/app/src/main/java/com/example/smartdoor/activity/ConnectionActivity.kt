package com.example.smartdoor.activity

import android.Manifest
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.smartdoor.R
import com.example.smartdoor.dto.NumberResult
import com.example.smartdoor.dto.VisitInfoDTO
import com.example.smartdoor.service.Hasher
import com.example.smartdoor.service.HttpProtocol
import kotlinx.android.synthetic.main.activity_connection.*
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ConnectionActivity : AppCompatActivity() {
    var isEnd: Boolean = false
    lateinit var btManager: BluetoothManager
    lateinit var btAdapter: BluetoothAdapter
    lateinit var btScanner: BluetoothLeScanner
    private val SCAN_PERIOD = 10000L
    var isScanning = false
    lateinit var scanHandler: Handler

    val scanRunnable: Runnable = object : Runnable {
        override fun run() {
            if (isScanning) {
                if (btAdapter != null) {
                    btScanner.stopScan(mScanCallback)
                }
            } else {
                if (btAdapter != null) {
                    btScanner.startScan(mScanCallback)
                }
            }
            isScanning = !isScanning
            scanHandler.postDelayed(this, SCAN_PERIOD)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scanHandler.removeCallbacks(scanRunnable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            10
        )
        btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter
        btScanner = btAdapter.bluetoothLeScanner
        scanHandler = Handler()
        scanHandler.post(scanRunnable);

        logoutButton.setOnClickListener {
            var auto = getSharedPreferences("auto", 0)
            var editor = auto.edit()
            editor.clear()
            editor.commit()
            Toast.makeText(this@ConnectionActivity, "로그아웃", Toast.LENGTH_LONG).show()
            scanHandler.removeCallbacks(scanRunnable)
            startActivity<LoginActivity>()
        }
        visiterButton.setOnClickListener {
            scanHandler.removeCallbacks(scanRunnable)
            startActivity<VisiterActivity>()
        }
    }

    val mScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult): Unit {
            if (isEnd) {
                return
            }
            Log.i("TAG :: ", "callbackType $callbackType")
            val scanRecord = result.scanRecord!!.bytes
            findBeaconPattern(scanRecord)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (sr in results) {
                Log.i("TAG :: ", "ScanResult - Results$sr")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            btAdapter.disable()
            btAdapter.enable()
            Log.e("TAG :: ", "Scan Failed Error Code: $errorCode")
        }
    }

    private fun findBeaconPattern(scanRecord: ByteArray) {
        var startByte = 2
        var patternFound = false
        while (startByte <= 5) {
            if (scanRecord[startByte + 2].toInt() and 0xff == 0x02 &&  //Identifies an iBeacon
                scanRecord[startByte + 3].toInt() and 0xff == 0x15
            ) { //Identifies correct data length
                patternFound = true
                break
            }
            startByte++
        }
        if (patternFound) { //Convert to hex String
            val uuidBytes = ByteArray(16)
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16)
            val hexString = bytesToHex(uuidBytes)
            //UUID detection
            val uuid = hexString.substring(0, 8) + "-" +
                    hexString.substring(8, 12) + "-" +
                    hexString.substring(12, 16) + "-" +
                    hexString.substring(16, 20) + "-" +
                    hexString.substring(20, 32)
            // major
            val major: Int =
                (scanRecord[startByte + 20].toInt() and 0xff) * 0x100 + (scanRecord[startByte + 21].toInt() and 0xff)
            // minor
            val minor: Int =
                (scanRecord[startByte + 22].toInt() and 0xff) * 0x100 + (scanRecord[startByte + 23].toInt() and 0xff)
            //UUID가 여깄네.
            var auto: SharedPreferences = getSharedPreferences("auto", 0)
            //2개날려야함.. date가 있는거랑 date가 없는것.
            var hashStr = Hasher().hash(
                "${auto?.getString("id", "").toString()}",
                "${auto?.getString("id", "").toString()}",
                "${auto?.getString("id", "").toString()}",
                "${auto?.getString("id", "").toString()}"
            )
            //집주일경우
            when(sendJsonData(hashStr,uuid,"집주인")){
                true->{
                    //집주인이 더 우선순위
                }
                false->{

                    hashStr = Hasher().hash(
                        "${auto?.getString("id", "").toString()}",
                        "",
                        getDate(), //현재날짜
                        getTime().toString(), //현재시간,
                        (getTime().toInt()+100).toString() // 마감시간
                    )
                    Log.d("hashFind :: " , "${auto?.getString("id", "").toString()} // ${getDate()} // ${getTime().toString()}")
                    //방문객일때
                    sendJsonData(hashStr,uuid,"방문객")
                }
            }
            Log.i(
                "tag",
                "UUID: $uuid  // nmajor: $major // nminor: $minor // $hashStr"
            )
        }
    }
    fun sendJsonData(hashStr: String, uuid: String,oauthStr:String): Boolean {
        var bool = false
        HttpProtocol.retrofitService.find(VisitInfoDTO(hashStr, uuid)).enqueue(
            object : Callback<NumberResult> {
                override fun onResponse(
                    call: Call<NumberResult>,
                    response: Response<NumberResult>?
                ) {
                    when (response!!.code()) {
                        200 -> {
                            Log.d("response ::", response?.body().toString())
                            val login_status = response?.body()?.state.toString()
                            when (login_status) {
                                "false" -> {

                                }
                                "true" -> {
                                    findTextView.text = "$oauthStr $uuid 을 탐색"
                                    isEnd = true
                                    bool = true
                                }
                            }

                        }
                        405 -> {
                        }
                        500 -> {
                        }
                        else -> {
                            Toast.makeText(
                                this@ConnectionActivity,
                                "에러 ${response.code()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<NumberResult>, t: Throwable) {
                }

            }
        )
        return bool
    }
    val hexArray = "0123456789ABCDEF".toCharArray()
    open fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v: Int = (bytes[j].toInt() and 0xFF)
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }
    fun getTime(): String {
        var date = Date()
        var sdf = SimpleDateFormat("HH00")
        return sdf.format(date)
        }
    fun getDate() : String{
        var date = Date()
        var sdf = SimpleDateFormat("YYYYMMdd")
        return sdf.format(date)
    }
}