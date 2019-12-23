package com.example.smartdoor.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.smartdoor.R
import kotlinx.android.synthetic.main.activity_signup.*
import android.widget.Toast
import com.example.smartdoor.service.HttpProtocol
import com.example.smartdoor.dto.MemberDTO
import kotlinx.android.synthetic.main.activity_signup.SignupButton
import org.jetbrains.anko.startActivity

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        //개인정보 입력후 회원가입, 서버 통신하기.
        SignupButton.setOnClickListener {
            HttpProtocol.retrofitService.signup(
                MemberDTO(
                    idEditText.text.toString(),
                    passEditText.text.toString(),
                    phoneEditText.text.toString(),
                    "1",
                    modelNumEditText.text.toString()
                )
            ).enqueue(object: Callback<Void>{
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    when(response!!.code()) {
                        200 -> {
                            Toast.makeText(this@SignupActivity , "회원가입 성공 !",Toast.LENGTH_LONG).show()
                            startActivity<LoginActivity>();
                        }
                        405 -> {
                            Toast.makeText(this@SignupActivity, "회원가입 실패 !",Toast.LENGTH_LONG).show()
                        }
                        500 -> {
                            Toast.makeText(this@SignupActivity, "회원가입 실패 : 서버 오류 !",Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Log.d("테스트",idEditText.toString()+
                                    passEditText.text.toString()+
                                    phoneEditText.text.toString()+
                                    modelNumEditText.text.toString())
                        }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {

                }
            })

        }
    }
}
