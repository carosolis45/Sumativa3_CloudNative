package cl.duoc.ejemplo.gestorguias.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public void subirArchivo(String carpeta, String nombreArchivo, byte[] contenido) {
        String key = carpeta + "/" + nombreArchivo;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(contenido));
    }

    public void eliminarArchivo(String carpeta, String nombreArchivo) {
        String key = carpeta + "/" + nombreArchivo;
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }

    public boolean archivoExiste(String carpeta, String nombreArchivo) {
        String key = carpeta + "/" + nombreArchivo;
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    public String descargarArchivo(String carpeta, String nombreArchivo) {
        String key = carpeta + "/" + nombreArchivo;
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        byte[] contenido = s3Client.getObjectAsBytes(request).asByteArray();
        return new String(contenido);
    }
}