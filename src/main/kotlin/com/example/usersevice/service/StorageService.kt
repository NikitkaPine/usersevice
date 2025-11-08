package com.example.usersevice.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class StorageService {
    @Value("\${storage.avatar-path}")
    private lateinit var uploadPath: String

    fun saveAvatar(file: MultipartFile): String{
        if(file.isEmpty){
            throw IllegalArgumentException("File is empty")
        }

        val contentType = file.contentType
        if(contentType == null || !contentType.startsWith("image/")){
            throw IllegalArgumentException("File must be an image")
        }

        val uploadDir = Paths.get(uploadPath)
        if(!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir)
        }

        val extension = file.originalFilename
            ?.substringAfterLast(".","jpg")
            ?: "jpg"
        val fileName = "${UUID.randomUUID()}.$extension"
        val filePath = uploadDir.resolve(fileName)

        Files.copy(
            file.inputStream,
            filePath,
            StandardCopyOption.REPLACE_EXISTING
        )

        return "/uploads/avatars/$fileName"
    }

    fun deleteAvatar(avatarUrl:String){
        try{
            val fileName = avatarUrl.substringAfterLast("/")
            val filePath = Paths.get(uploadPath).resolve(fileName)

            if(Files.exists(filePath)){
                Files.delete((filePath))
            }
        }catch (e: Exception){
            println("Failed to delete avatar: ${e.message}")
        }
    }
}