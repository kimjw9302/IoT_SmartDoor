package com.example.smartdoor.service

import java.security.MessageDigest

class Hasher {
    //아이디 핸드폰번호 날짜
    public fun hash(id:String="",phone:String="",date:String="",starttime:String="",endtime:String=""): String {
        val bytes = ("$id$phone$date$starttime$endtime").toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}