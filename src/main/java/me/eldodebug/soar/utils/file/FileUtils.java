package me.eldodebug.soar.utils.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JFileChooser;

import net.minecraft.util.Util;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import me.eldodebug.soar.logger.GlideLogger;
import me.eldodebug.soar.utils.file.filter.PngFileFilter;
import me.eldodebug.soar.utils.file.filter.SoundFileFilter;
import org.lwjgl.Sys;

public class FileUtils {

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }

            return directory.delete();
        }
        return false;
    }
    
    public static long getDirectorySize(File directory) {
    	
        long size = 0;

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else if (file.isDirectory()) {
                        size += getDirectorySize(file);
                    }
                }
            }
        } else if (directory.isFile()) {
            size = directory.length();
        }

        return size;
    }
    
    public static void unzip(final File file, final File dest) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                final File f = new File(dest, ze.getName());
                if (ze.isDirectory()) {
                    f.mkdirs();
                }
                else {
                    final FileOutputStream fos = new FileOutputStream(f);
                    final byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
            }
            zis.close();
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
	public static File selectImageFile() {
        
        JFileChooser fileChooser = new JFileChooser();
        
        fileChooser.setFileFilter(new PngFileFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);

        int result = fileChooser.showOpenDialog(null);
        
        if(result == JFileChooser.APPROVE_OPTION) {
        	return fileChooser.getSelectedFile();
        }
        
        return null;
	}
	
	public static File selectSoundFile() {
        
        JFileChooser fileChooser = new JFileChooser();
        
        fileChooser.setFileFilter(new SoundFileFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);

        int result = fileChooser.showOpenDialog(null);
        
        if(result == JFileChooser.APPROVE_OPTION) {
        	return fileChooser.getSelectedFile();
        }
        
        return null;
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		
		FileInputStream input = null;
		FileOutputStream output = null;
		
        try {
        	
            input = new FileInputStream(sourceFile);
            output = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int length;
            
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        } finally{ 
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        }
	}
	
    public static void downloadFile(String url, File output) {
    	try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    try (InputStream inputStream = entity.getContent();
                         OutputStream outputStream = new FileOutputStream(output)) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
            httpClient.close();
    	}catch(Exception e) {
    		GlideLogger.error("Failed to download file: " + url, e);
    	}
    }
    
	public static String getBaseName(String fileName) {
		
        if (fileName == null) {
            return "null";
        }
        
        int point = fileName.lastIndexOf(".");
        
        if (point != -1) {
            return fileName.substring(0, point);
        }
        
        return fileName;
	}
	
	public static String getBaseName(File file) {
		return getBaseName(file.getName());
	}
	
	public static String getExtension(String fileName) {
		
		if(fileName == null) {
			return null;
		}
		
	    int lastIndexOf = fileName.lastIndexOf(".");
	    
	    if (lastIndexOf == -1) {
	        return "null";
	    }
	    
	    return fileName.substring(lastIndexOf + 1);
	}
	
	public static String getExtension(File file) {
		return getExtension(file.getName());
	}
	
	public static boolean isAudioFile(String fileName) {
		
		if(fileName == null) {
			return false;
		}
		
		String ext = getExtension(fileName);
		
		return ext.equals("mp3") || ext.equals("wav") || ext.equals("ogg");
	}
	
	public static boolean isAudioFile(File file) {
		return isAudioFile(file.getName());
	}
	
	public static boolean isImageFile(String fileName) {
		
		if(fileName == null) {
			return false;
		}
		
		String ext = getExtension(fileName);
		
		return ext.equals("jpeg") || ext.equals("png") || ext.equals("jpg");
	}
	
	public static boolean isImageFile(File file) {
		return isImageFile(file.getName());
	}

    /*
     * This is from the GuiScreenResourcePacks class
     * Copyright mojang
     *
     * This code accepts a path and tries to open it in the file browser
     */
    public static void openFolderAtPath(File folder) {
            String absolutePath = folder.getAbsolutePath();

            if (Util.getOSType() == Util.EnumOS.OSX) {
                try {
                    Runtime.getRuntime().exec(new String[] {"/usr/bin/open", absolutePath}); return;
                } catch (IOException ignored) {}
            }
            else if (Util.getOSType() == Util.EnumOS.WINDOWS) {
                try {
                    Runtime.getRuntime().exec(String.format("cmd.exe /C start \"Open file\" \"%s\"", absolutePath)); return;
                } catch (IOException ignored) {}
            }

            try {
                Class<?> oclass = Class.forName("java.awt.Desktop");
                Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
                oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, folder.toURI());
            } catch (Throwable throwable) {
                 Sys.openURL("file://" + absolutePath);
            }

        }

}
