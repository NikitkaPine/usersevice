package com.example.usersevice.repository

import com.example.usersevice.model.User
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.assertj.core.api.Assertions.assertThat

@DataJpaTest
class UserRepositoryTest{

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `should save and find user by identifier`(){
        val user = User(
            identifier = "testUser",
            passwordHash = "testPassword"
        )

        val saved = userRepository.save(user)
        val found = userRepository.findByIdentifier("testUser")

        assertThat(found).isPresent
        assertThat(found.get().id).isEqualTo(saved.id)

    }
}