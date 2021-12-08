package com.topwo.lib;

import android.app.Application;
import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TopwoFileDownloader {
    private static final String TAG = TopwoFileDownloader.class.getSimpleName();
    
    public static void onCreateApplication(Application application, int connectTimeout, int readTimeout) {
        FileDownloader.setupOnApplicationOnCreate(application)
                .connectionCreator(new FileDownloadUrlConnection
                        .Creator(new FileDownloadUrlConnection.Configuration()
                        .connectTimeout(connectTimeout) // set connection timeout.
                        .readTimeout(readTimeout) // set read timeout.
                ))
                .commit();
    }

    public String mSaveFolder = FileDownloadUtils.getDefaultSaveRootPath() + File.separator + "topwo";

    public BaseDownloadTask createBaseDownloadTask(String url, String saveFileName) {
        return FileDownloader.getImpl().create(url)
                //.setPath(mSinglePath, false)
                //.setTag()
                .setPath(mSaveFolder + File.separator + saveFileName,true)
                .setCallbackProgressTimes(300)
                .setMinIntervalUpdateSpeed(400);
    }

    public int startSingle(BaseDownloadTask task, FileDownloadListener fileDownloadListener) {
        return task.setListener(fileDownloadListener).start();
    }

    public void pauseSingle(BaseDownloadTask task) {
        Log.d(TAG,"pause_single task:" + task.getId());
        FileDownloader.getImpl().pause(task.getId());
    }

    public void deleteSingle(BaseDownloadTask task) {
        //删除单个任务的database记录
        boolean deleteData =  FileDownloader.getImpl().clear(task.getId(), mSaveFolder);
        File targetFile = new File(task.getPath());
        boolean delate = false;
        if(targetFile.exists()){
            delate = targetFile.delete();
        }

        Log.d(TAG,"delete_single file,deleteDataBase:" + deleteData + ",mSinglePath:" + task.getPath() + ",delate:" + delate);

        new File(FileDownloadUtils.getTempPath(task.getPath())).delete();
    }

    public void createMulti(List<BaseDownloadTask> tasks, FileDownloadListener fileDownloadListener){
        //(1) 创建 FileDownloadQueueSet
        final FileDownloadQueueSet queueSet = new FileDownloadQueueSet(fileDownloadListener);

        //(2) 设置参数

        // 每个任务的进度 无回调
        //queueSet.disableCallbackProgressTimes();
        // do not want each task's download progress's callback,we just consider which task will completed.

        queueSet.setCallbackProgressTimes(100);
        queueSet.setCallbackProgressMinInterval(100);
        //失败 重试次数
        queueSet.setAutoRetryTimes(3);

        //避免掉帧
        FileDownloader.enableAvoidDropFrame();

        //(3)串行下载
        queueSet.downloadSequentially(tasks);

        //(4)任务启动
        queueSet.start();
    }

    public void stopMulti(FileDownloadListener fileDownloadListener){
        FileDownloader.getImpl().pause(fileDownloadListener);
    }

    public void deleteAllFile(){
        //清除所有的下载任务
        FileDownloader.getImpl().clearAllTaskData();

        //清除所有下载的文件
        int count = 0;
        File file = new File(FileDownloadUtils.getDefaultSaveRootPath());
        do {
            if (!file.exists()) {
                break;
            }

            if (!file.isDirectory()) {
                break;
            }

            File[] files = file.listFiles();

            if (files == null) {
                break;
            }

            for (File file1 : files) {
                count++;
                file1.delete();
            }

        } while (false);

        Log.d(TAG, String.format("Complete delete %d files", count));
    }

    /**
     * 外界可以参考这个listener
     */
    public FileDownloadListener createFileDownloadListener() {
        return new FileDownloadSampleListener(){
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                Log.d(TAG,"pending taskId:" + task.getId() + ",fileName:" + task.getFilename() + ",soFarBytes:" + soFarBytes + ",totalBytes:" + totalBytes + ",percent:" + soFarBytes * 1.0 / totalBytes);
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                Log.d(TAG,"progress taskId:" + task.getId() + ",fileName:" + task.getFilename() + ",soFarBytes:" + soFarBytes + ",totalBytes:" + totalBytes + ",percent:" + soFarBytes * 1.0 / totalBytes + ",speed:" + task.getSpeed());
            }

            @Override
            protected void blockComplete(BaseDownloadTask task) {
                Log.d(TAG,"blockComplete taskId:" + task.getId() + ",filePath:" + task.getPath() + ",fileName:" + task.getFilename() + ",speed:" + task.getSpeed() + ",isReuse:" + task.reuse());
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                Log.d(TAG,"completed taskId:" + task.getId() + ",isReuse:" + task.reuse());
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                Log.d(TAG,"paused taskId:" + task.getId() + ",soFarBytes:" + soFarBytes + ",totalBytes:" + totalBytes + ",percent:" + soFarBytes * 1.0 / totalBytes);
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                Log.d(TAG,"error taskId:" + task.getId() + ",e:" + e.getLocalizedMessage());
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                Log.d(TAG,"warn taskId:" + task.getId());
            }
        };
    }
}
