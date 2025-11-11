package com.example.usersevice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class UserseviceApplication

fun main(args: Array<String>) {
	runApplication<UserseviceApplication>(*args)
}
