package com.pe.allpafood.api.core.utils.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class FileUtil {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "application/csv");

    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList("exe", "sh", "bat", "cmd", "js", "vbs");

    private static final Pattern INVALID_FILE_NAME_PATTERN = Pattern.compile("[^a-zA-Z0-9\\-_.]");


    /**
     * Verifica si el archivo es válido según las extensiones permitidas.
     * @param file El archivo a verificar.
     * @param allowedExtensions Lista de extensiones permitidas.
     * @return true si el archivo es válido, false si no lo es.
     */
    public static boolean isValidFile(MultipartFile file, List<String> allowedExtensions) {

        String fileExtension = getFileExtension(file.getOriginalFilename());


        if (
            file.getSize() > MAX_FILE_SIZE
            || INVALID_FILE_NAME_PATTERN.matcher(Objects.requireNonNull(file.getOriginalFilename())).find()
            || !allowedExtensions.contains(fileExtension)
            || DANGEROUS_EXTENSIONS.contains(fileExtension)
        ) return false;


        String mimeType = file.getContentType();
        return mimeType != null && ALLOWED_MIME_TYPES.contains(mimeType);
    }

    /**
     * Obtiene la extensión del archivo.
     * @param fileName Nombre del archivo.
     * @return Extensión del archivo.
     */
    private static String getFileExtension(String fileName) {
        if (fileName != null && fileName.lastIndexOf('.') > 0) return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        return "";
    }

    /**
     * Guarda el archivo en el directorio especificado.
     * @param file El archivo que se va a guardar.
     * @param directory El directorio en el que se guardará el archivo.
     * @return El archivo guardado.
     */
    public static File saveFile(MultipartFile file, String fileName,String directory) throws IOException {
        File destinationFile = new File(directory + File.separator + fileName);
        file.transferTo(destinationFile);
        return destinationFile;
    }

    /**
     * Obtiene un archivo del directorio especificado.
     * @param directory El directorio de donde obtener el archivo.
     * @param fileName El nombre del archivo.
     * @return El archivo obtenido.
     */
    public static File getFileFromDirectory(String directory, String fileName) {
        return new File(directory + File.separator + fileName);
    }
}