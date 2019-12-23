package com.example.smartdoor.activity

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.smartdoor.R
import com.example.smartdoor.dto.MemberDTO
import com.example.smartdoor.dto.NumberResult
import com.example.smartdoor.dto.VisitInfoDTO
import com.example.smartdoor.service.Hasher
import com.example.smartdoor.service.HttpProtocol
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_visiter.*
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VisiterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visiter)
        //연동 코드
        var auto =  getSharedPreferences("auto", 0)
        HttpProtocol.retrofitService.getUUID(
            MemberDTO("${auto.getString("id","")}")
        ).enqueue(object : Callback<NumberResult>{
            override fun onResponse(
                call: Call<NumberResult>,
                response: Response<NumberResult>?
            ) {
                when (response!!.code()) {
                    200 -> {
                        Log.d("UUID는 ::", response?.body()?.state)
                    }
                    405 -> {
                    }
                    500 -> {
                    }
                    else -> {
                    }
                }

                uuidTextView.text = response?.body()?.state
            }

            override fun onFailure(call: Call<NumberResult>, t: Throwable) {
            }
        })


        submitButton.setOnClickListener {

            var hashStr = Hasher().hash(
                "${visitedEditText.text.toString()}",
                "",
                "${dateEditText.text.toString()}",
                "${timeEditText.text.toString()}",
                "${endEditTime.text.toString()}"
            )
            Log.d("hashFind", "${visitedEditText.text.toString()} // ${dateEditText.text.toString()} // ${timeEditText.text.toString()} // ${endEditTime.text.toString()}")
            HttpProtocol.retrofitService.addVisitInfo(
                VisitInfoDTO(hashStr, uuidTextView.text.toString())
            ).enqueue(object : Callback<NumberResult> {
                override fun onResponse(
                    call: Call<NumberResult>,
                    response: Response<NumberResult>
                ) {
                    when (response!!.code()) {
                        200 -> {
                           Toast.makeText(this@VisiterActivity,"등록이 됐습니다",Toast.LENGTH_LONG).show()
                            visitedEditText.text.clear()
                            dateEditText.text.clear()
                            endEditTime.text.clear()
                            timeEditText.text.clear()

                        }
                        405 -> {
                        }
                        500 -> {
                        }
                        else -> {
                        }
                    }
                }
                override fun onFailure(call: Call<NumberResult>, t: Throwable) {
                }
            })
        }
    }
}
