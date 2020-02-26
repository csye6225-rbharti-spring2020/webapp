package com.rohan.cloudProject.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
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
import java.nio.file.Path;
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

    private String fileUploadPath;

    @Autowired
    private FileRepository fileRepository;

    @Autowired(required = false)
    private AmazonS3 amazonS3Client;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${amazon.s3.bucketName}")
    private String bucketName;

    /**
     * Takes in the Multipart file. Performs all the necessary validations and returns the created File Object to be saved.
     *
     * @param file
     * @return
     */
    public File createNewFile(MultipartFile file, String billId) {

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (fileName.contains("..")) {
                throw new StorageException("The File Name is not a valid file name: " + fileName);
            }

            String extension = file.getOriginalFilename().split("\\.")[1];
            if (!(extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("pdf") ||
                    extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("jpg"))) {
                throw new StorageException(("The file has to be of the following formats: png, jpeg, jpg, pdf!"));
            }

            String finalFilePath = null;

            if (activeProfile.equals("dev")) {
                fileUploadPath = System.getProperty("user.home");

                String fileStoragePath = fileUploadPath + "/files/";

                Path path = Paths.get(fileStoragePath);

                if (!Files.exists(path)) {
                    Files.createDirectory(path);
                    logger.info("File storage Directory created");
                } else {
                    System.out.println("File storage Directory already exists");
                }

                finalFilePath = fileStoragePath + fileName;

                Files.copy(file.getInputStream(), Paths.get(finalFilePath + "-" + billId), StandardCopyOption.REPLACE_EXISTING);

                logger.info("The file was stored successfully on the local FileSystem!");
            }

            if (activeProfile.equals("aws") && (!bucketName.equals("notAvailable"))) {
                fileUploadPath = bucketName;

                String fileStoragePath = fileUploadPath;
                fileName = fileName.concat("/").concat(billId);
                finalFilePath = "https://" + fileStoragePath + ".s3.amazonaws.com" + "/" + fileName;

                try {
                    java.io.File s3Bucketfile = convertMultiPartToFile(file);
                    uploadFileTos3bucket(fileName, s3Bucketfile);
                } catch (AmazonServiceException ase) {
                    logger.info("Caught an AmazonServiceException from GET requests, rejected reasons:");
                    logger.info("Error Message:    " + ase.getMessage());
                    logger.info("HTTP Status Code: " + ase.getStatusCode());
                    logger.info("AWS Error Code:   " + ase.getErrorCode());
                    logger.info("Error Type:       " + ase.getErrorType());
                    logger.info("Request ID:       " + ase.getRequestId());
                } catch (AmazonClientException ace) {
                    logger.info("Caught an AmazonClientException: ");
                    logger.info("Error Message: " + ace.getMessage());
                } catch (IOException ioe) {
                    logger.info("IOE Error Message: " + ioe.getMessage());
                }

                logger.info("The file was stored successfully on the S3 Bucket!");
            }


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

    private java.io.File convertMultiPartToFile(MultipartFile file) throws IOException {
        java.io.File convFile = new java.io.File(file.getOriginalFilename());
        return convFile;
    }

    private void uploadFileTos3bucket(String fileName, java.io.File file) {
        amazonS3Client.putObject(
                new PutObjectRequest(bucketName, fileName, file));
        //.withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public boolean deleteFileFromS3Bucket(String fileName) {
        try {
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
            logger.info("File deleted successfully from the S3 Bucket");
            return true;
        } catch (Exception e) {
            logger.error("File wasn't deleted successfully from the S3 Bucket");
            return false;
        }
    }


}
