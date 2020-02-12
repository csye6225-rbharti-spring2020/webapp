package com.rohan.cloudProject.service;

import com.rohan.cloudProject.model.File;
import com.rohan.cloudProject.model.exception.StorageException;
import com.rohan.cloudProject.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * File Service Layer Class for the Spring Boot Application.
 *
 * @author rohan_bharti
 */
@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(BillService.class);

    @Value("${file.upload.dir}")
    private String fileUploadPath;

    @Autowired
    private FileRepository fileRepository;

    /**
     * Takes in the Multipart file. Performs all the necessary validations and returns the created File Object to be saved.
     *
     * @param file
     * @return
     */
    public File createNewFile(MultipartFile file) {

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (fileName.contains("..")) {
                throw new StorageException("The File Name is not a valid file name: " + fileName);
            }

            String finalFilePath = fileUploadPath + fileName;
            Files.copy(file.getInputStream(), Paths.get(finalFilePath), StandardCopyOption.REPLACE_EXISTING);
            logger.info("The file was stored successfully!");

            File storedFile = new File();
            storedFile.setFileName(fileName);
            storedFile.setStorageUrl(finalFilePath);
            storedFile.setUploadDate(new Date());

            //Storing the MD5 Hash of the file
            byte[] uploadBytes = file.getBytes();
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5digest = md5.digest(uploadBytes);
            String md5HashString = new BigInteger(1, md5digest).toString(16);
            storedFile.setMd5Hash(md5HashString);

            //Storing the size of the file
            Long fileSize = file.getSize();
            storedFile.setFileSize(fileSize);

            return storedFile;

        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new StorageException("The file: " + fileName + "could not be stored successfully!", ex);
        }

    }

    /**
     * Deletes the file by its ID.
     *
     * @param id
     */
    public void deleteFileById(String id) {
        fileRepository.deleteById(id);
    }

    /**
     * Gets the file by its ID.
     *
     * @param id
     * @return
     */
    public File getFileById(String id) {
        return fileRepository.findById(id).get();
    }

}
