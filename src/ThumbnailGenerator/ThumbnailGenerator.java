
package ThumbnailGenerator;


import java.io.File;
import java.io.IOException;


public class ThumbnailGenerator implements Runnable{
    private ImageFileResizer resizer;
    private Thread thread;
    
    public ThumbnailGenerator(String inputDirectory, String outputDirectory, int width, int height){
        this.resizer = ImageFileResizer.initializeResizer(inputDirectory, outputDirectory, width, height);
    }
    
    public void start(){
        this.thread = new Thread(this);
        thread.start();
    }
    
    
    @Override
    public void run() {
        resizer.start();
    }


//    public static void main(String[] args) {
//        // TODO code application logic here
//        ThumbnailGenerator generator = new ThumbnailGenerator("./folder1", "./folder2", 300, 200);
//        generator.start();
//    }
    
}
