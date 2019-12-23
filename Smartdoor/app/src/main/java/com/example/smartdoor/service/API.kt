package com.example.smartdoor.service


import com.example.smartdoor.dto.NumberResult
import com.example.smartdoor.dto.MemberDTO
import com.example.smartdoor.dto.VisitInfoDTO
import retrofit2.Call
import retrofit2.http.*

interface API {
    @Headers("Content-Type: application/json")
    @POST("loginMember")  //로그인
    fun logIn(@Body member: MemberDTO) :  Call<NumberResult>

    @Headers("Content-Type: application/json")
    @POST("addDoorMember")
    fun signup( @Body member: MemberDTO) :  Call<Void>

    @Headers("Content-Type: application/json")
    @POST("visitInfoIsNull") //회원가입
    fun find( @Body visitInfo : VisitInfoDTO) :  Call<NumberResult>

    @Headers("Content-Type: application/json")
    @POST("addVisitInfo") //회원가입
    fun addVisitInfo( @Body visitInfo : VisitInfoDTO) :  Call<NumberResult>

    @Headers("Content-Type: application/json")
    @POST("selectUUID") //회원가입
    fun getUUID( @Body member : MemberDTO) :  Call<NumberResult>

}