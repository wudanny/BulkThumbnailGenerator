
package ThumbnailGenerator;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.AlphaComposite;
import java.util.concurrent.*;


public class ImageFileResizer implements Runnable {
    private final File[] fileOfImage;
    private final int resizeWidth;
    private final int resizeHeight;
    private final String outputDirectory;
    private final ExecutorService threadPool;
    private int counter;
    private final int maxThreadPoolSize = 5;

    private ImageFileResizer(File[] image, String outputDirectory, int width, int height) {
        this.fileOfImage = image;
        this.resizeHeight = height;
        this.resizeWidth = width;
        this.outputDirectory = outputDirectory;
        threadPool = Executors.newFixedThreadPool(maxThreadPoolSize);
        this.counter = 0;
    }

    public static ImageFileResizer initializeResizer(String inputDirectory, String outputDirectory, int resizeWidth, int resizeHeight) {
        File inputFolder = new File(inputDirectory);
        File[] inputFiles = inputFolder.listFiles();
        ImageFileResizer resizer = new ImageFileResizer(inputFiles, outputDirectory, resizeWidth, resizeHeight);
        return resizer;
    }

    private synchronized boolean hasNext() {
        if (counter < fileOfImage.length) {
            return true;
        }
        return false;
    }

    private synchronized File getNext() {
        if (hasNext()) {
            File currentFile = fileOfImage[counter];
            counter++;
            return currentFile;
        }

        return null;
    }

    public void start() {
        for (int i = 0, j = fileOfImage.length; i < j; i++) {
            threadPool.execute(this);
        }
        
        threadPool.shutdown();
        
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.print(e);
        }
    }

    private void resizeNextImageFile() {
        try {
            File imageFile = getNext();
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                return;
            }
            int imageType = image.getType() == 0 ? BufferedImage.TYPE_4BYTE_ABGR : image.getType();
            BufferedImage resized = resizeHint(image, resizeWidth, resizeHeight, imageType);
            String name = imageFile.getName();
            int indexOfExtension = name.lastIndexOf(".");
            String extention = indexOfExtension > 0 ? name.substring(indexOfExtension + 1) : "PNG";
            ImageIO.write(resized, extention, new File(outputDirectory + "/" + name));

        } catch (IOException ex) {
            System.err.println(ex.toString());
        }

    }

    public BufferedImage resizeHint(BufferedImage img, int newWidth, int newHeight, int type) {
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, type);
        Graphics2D resizedGraphic = resizedImage.createGraphics();
        resizedGraphic.setComposite(AlphaComposite.Src);

        resizedGraphic.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        resizedGraphic.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        resizedGraphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        resizedGraphic.drawImage(img, 0, 0, newWidth, newHeight, null);
        resizedGraphic.dispose();

        return resizedImage;
    }

    @Override
    public void run() {
        resizeNextImageFile();
    }

}
