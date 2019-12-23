package com.example.smartdoor.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.smartdoor.R
import com.example.smartdoor.service.HttpProtocol
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.SignupButton
import kotlinx.android.synthetic.main.activity_login.idEditText
import kotlinx.android.synthetic.main.activity_login.passEditText
import android.content.SharedPreferences
import android.util.Log
import com.example.smartdoor.dto.NumberResult
import com.example.smartdoor.dto.MemberDTO
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Signup 버튼 클릭시, SignupActivity 로 화면전환
        SignupButton.setOnClickListener {
            startActivity<SignupActivity>()
        }
        var auto: SharedPreferences = getSharedPreferences("auto", 0)
        if (auto!!.getString("id", "")!!.length != 0) {
            //한적있음
            Toast.makeText(
                this@LoginActivity,
                "${auto?.getString("id", "").toString()} 님 안녕하세요!",
                Toast.LENGTH_LONG
            ).show()
            startActivity<ConnectionActivity>()
        }
        //Login 버튼 클릭시 , 서버와 통신
        LoginButton.setOnClickListener {
            HttpProtocol.retrofitService.logIn(
              MemberDTO(idEditText.text.toString(),passEditText.text.toString())
            ).enqueue(object : Callback<NumberResult> {
                override fun onResponse(call: Call<NumberResult>, response: Response<NumberResult>?) {
                    when (response!!.code()) {
                        200 -> {
                            Toast.makeText(this@LoginActivity, "로그인 성공 !", Toast.LENGTH_LONG).show()
                            Log.d("response ::" , response?.body().toString())
                            val login_status = response?.body()?.state.toString()

                            when(login_status){
                                "false" ->{
                                    //정보가 일치 X
                                    Toast.makeText(this@LoginActivity, "잘못된 정보 :: 로그인 실패 !", Toast.LENGTH_LONG).show()
                                }
                                "true"->{
                                    //로그인 정보 O
                                    var editor = auto?.edit()
                                    editor?.putString("id", idEditText.text.toString())
                                    editor?.putString("phone", idEditText.text.toString())
                                    editor?.commit()
                                    startActivity<ConnectionActivity>()
                                }
                            }

                        }
                        405 -> {
                            Toast.makeText(this@LoginActivity, "로그인 실패 !", Toast.LENGTH_LONG).show()
                        }
                        500 -> {
                            Toast.makeText(
                                this@LoginActivity,
                                "로그인 실패 : 서버 오류 !",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                this@LoginActivity,
                                "에러 ${response.code()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                override fun onFailure(call: Call<NumberResult>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "접속 실패", Toast.LENGTH_LONG).show()
                }
            })
        }
    }


}
