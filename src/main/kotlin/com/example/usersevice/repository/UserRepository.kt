package com.example.usersevice.repository

import com.example.usersevice.model.User
import org.intellij.lang.annotations.Identifier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository: JpaRepository<User, Long> {
    fun findByIdentifier(identifier:String): Optional<User>
    fun existsByIdentifier(identifier: String): Boolean
}
