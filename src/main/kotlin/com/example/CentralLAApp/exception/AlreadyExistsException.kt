package com.example.CentralLAApp.exception

class AlreadyExistsException(message : String, val existList: List<Int>) : RuntimeException(message) {
}