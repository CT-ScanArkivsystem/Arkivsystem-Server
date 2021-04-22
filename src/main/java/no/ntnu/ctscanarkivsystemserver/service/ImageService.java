package no.ntnu.ctscanarkivsystemserver.service;

import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Convert, scales and handles images.
 * @author TrymV
 */
@Service
public class ImageService {

    /**
     * Checks if file is a image.
     * @param fileName name of file including file type
     * @return true if the file is a image the system supports.
     */
    public boolean isFileAnImage(String fileName) {
        List<String> imageTypes = new ArrayList<>(Arrays.asList(".jpg", ".png", ".PNG", ".gif", ".raw", ".eps", ".bmp"));
        for (String imageType : imageTypes) {
            if (fileName.contains(imageType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Scales an image to same as width param.
     * @param imageBytes Image to scale as byte array.
     * @param imageFileType File type of the image.
     * @param width Width to return image as.
     * @return Scaled image to width as byte array.
     * @throws IOException If reading image fails.
     */
    public byte[] scaleImage(byte[] imageBytes, String imageFileType, Integer width) throws IOException{
        ByteArrayOutputStream thumbOutput = new ByteArrayOutputStream();
        BufferedImage thumbImg;
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        thumbImg = Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, width, Scalr.OP_ANTIALIAS);
        ImageIO.write(thumbImg, imageFileType, thumbOutput);
        return thumbOutput.toByteArray();
    }

    /**
     * Convert a tiff image to png.
     * Cannot convert a tiff stack!
     * @param tiffBytes Tiff image as a byte array.
     * @return Converted image.
     * @throws IOException if reading image fails.
     */
    public byte[] convertTiffToPng(byte[] tiffBytes) throws IOException {
        final BufferedImage tif = ImageIO.read(new ByteArrayInputStream(tiffBytes));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(tif, "png", bos);
        return bos.toByteArray();
    }
}
