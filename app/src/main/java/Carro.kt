package com.example.notificar

data class Carro(
    val placa: String = "",
    val modelo: String = "",
    val cor: String = "", // <-- A VÍRGULA ESTAVA EM FALTA AQUI
    val marca: String = ""
)