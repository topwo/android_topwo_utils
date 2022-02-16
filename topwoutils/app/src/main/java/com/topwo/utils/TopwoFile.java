package com.topwo.utils;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TopwoFile {
    private static final String TAG = TopwoFile.class.getSimpleName();

    public interface UnzipInterface {
        byte[] handleOutStream(ByteArrayOutputStream outputStream);
    }

    private static Context sContext = null;

    public static final int BUFFER = 4096;
    private static String s_writable_path;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
        s_writable_path = sContext.getFilesDir().getAbsolutePath();
    }

    public static String getWritablePath() {
        return s_writable_path;
    }

    public static String getWritableDirPath() {
        return s_writable_path + File.separator;
    }

    /**
     * 将内容写入文件
     * @param file 要写入的File对象
     * @param buffer  待写入的字节组
     * @throws IOException
     */
    public static void writeFile(File file, byte[] buffer) throws Exception {
        if(file == null){
            return;
        }
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();//创建父文件夹
        }
        //打开文件输出流
        FileOutputStream outputStream = new FileOutputStream(file);
        //写数据到文件中
        outputStream.write(buffer);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 从sd card文件中读取数据
     * @param filename 待读取的文件路径
     * @return
     * @throws IOException
     */
    public static byte[] readFile(String filename) throws Exception {
        //打开文件输入流
        return readFile(new FileInputStream(filename));
    }

    public static byte[] readFile(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buffer[] = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        baos.flush();
        //关闭输入流
        inputStream.close();
        return baos.toByteArray();
    }

    /**
     * 传File的
     */
    public static void unzip(File file, String outDirPath, UnzipInterface unzipInterface) throws Exception {
        if(file == null){
            return;
        }
        if(file.exists()){
            unzip(new FileInputStream(file), outDirPath, unzipInterface);
        }
    }

    /**
     * 传FileInputStream的
     * zip文件必须存在
     * 解压完整一个文件再写到文件里
     */
    public static void unzip(InputStream fis, String outDirPath, UnzipInterface unzipInterface) throws Exception {
        if(fis == null){
            return;
        }
        BufferedInputStream buffer_stream = new BufferedInputStream(fis);
        ZipInputStream inZips = new ZipInputStream(buffer_stream);
        ZipEntry zipEntry = null;
        String szName = "";
        while ((zipEntry = inZips.getNextEntry()) != null) {
            szName = zipEntry.getName();
            try {
                File entryFile = new File(outDirPath + szName);
                if (szName.endsWith(File.separator)) {
                    entryFile.mkdirs();
                    continue;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                byte buffer[] = new byte[TopwoFile.BUFFER];
                while ((len = inZips.read(buffer, 0, TopwoFile.BUFFER)) != -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
                baos.close();
                if (null != unzipInterface) {
                    TopwoFile.writeFile(entryFile, unzipInterface.handleOutStream(baos));
                }
                else {
                    TopwoFile.writeFile(entryFile, baos.toByteArray());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        inZips.close();
        if (fis != null) {
            fis.close();
        }
    }

    //获取url对应的路径，以"/"结尾
    public static String getFileDirByUrl(String urlString /*"http://game.com/game/index.html"*/) {
        int lastSlash = urlString.lastIndexOf('/');
        String server = urlString.substring(0, lastSlash + 1);
        return server.replaceFirst("://", "/").replace(":", "#0A");
    }

    /**
     * 获取sd卡路径
     */
    public static String getExternalCacheDirPath() {
        //获取外部存储卡的可用状态
        String storageState = Environment.getExternalStorageState();

        //判断是否存在可用的的SD Card
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {

            //路径： /storage/emulated/0/Android/data/com.yoryky.demo/cache/yoryky.txt
            return sContext.getExternalCacheDir().getAbsolutePath() + File.separator;
        }
        return "";
    }

    /**
     * 获取assets目录下的文件
     */
    public static InputStream getAssetsFile(String filePathString) throws Exception {
        try {
            return sContext.getAssets().open(filePathString);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取assets目录下的文件
     */
    public static ArrayList<String> getAssetsDirFiles(String dirPathString) {
        ArrayList<String> fileList = new ArrayList<String>();
        try {
            String[] files = sContext.getAssets().list(dirPathString);
            if (files != null && files.length > 0) {
                for (String f : files) {
                    if (dirPathString != "") {// 如果当前目录不是根目录时，创建子目录
                        fileList.addAll(getAssetsDirFiles(dirPathString + File.separator + f));
                    }
                    else {
                        fileList.addAll(getAssetsDirFiles(f));
                    }
                }
            }
            else{
                fileList.add(dirPathString);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fileList.add(dirPathString);
        }
        return fileList;
    }
}
