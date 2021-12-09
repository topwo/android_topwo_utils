package com.topwo.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class TopwoSystem {
    private static final String TAG = TopwoSystem.class.getSimpleName();

    /**
     * Byte转换为KB或者MB，将获取的内存大小规格化
     * @return
     */
    public static String formatFileSize(Context context, long sizeBytes) {
        return Formatter.formatFileSize(context, sizeBytes);
    }

    /** get CPU rate
     * @return
     */
    public static int getProcessCpuRate () {

        StringBuilder tv = new StringBuilder();
        int rate = 0;

        try {
            String Result;
            Process p;
            p = Runtime.getRuntime().exec("top -n 1");

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((Result = br.readLine()) != null) {
                if (Result.trim().length() < 1) {
                    continue;
                } else {
                    String[] CPUusr = Result.split("%");
                    tv.append("USER:" + CPUusr[0] + "\n");
                    String[] CPUusage = CPUusr[0].split("User");
                    String[] SYSusage = CPUusr[1].split("System");
                    tv.append("CPU:" + CPUusage[1].trim() + " length:" + CPUusage[1].trim().length() + "\n");
                    tv.append("SYS:" + SYSusage[1].trim() + " length:" + SYSusage[1].trim().length() + "\n");

                    rate = Integer.parseInt(CPUusage[1].trim()) + Integer.parseInt(SYSusage[1].trim());
                    break;
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(rate + "");
        return rate;
    }


    /**
     * 获取cpu使用率
     * @return
     */
    public static float getCpuUsed() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            String[] toks = load.split(" ");
            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            try {
                Thread.sleep(360);
            } catch (Exception e) {
                e.printStackTrace();
            }
            reader.seek(0);
            load = reader.readLine();
            reader.close();
            toks = load.split(" ");
            long idle2 = Long.parseLong(toks[5]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }


    /**
     * 获取内存可用空间
     * @return
     */
    public static long getAvailMemory(Context context) {// 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem; // 当前系统的可用内存
    }

    /**
     * 获取内存总共空间
     * @return
     */
    public static long getTotalMemory() {
        String dir = "/proc/meminfo";// 系统内存信息文件
        long totalMemorySize = 0;
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();// 读取meminfo第一行，系统总内存大小
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
            totalMemorySize *= 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalMemorySize;
    }


    /**
     * 获取手机内部可用空间大小
     * @return
     */
    static public long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    /**
     * 获取手机内部空间大小
     * @return
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory(); //Gets the Android data directory
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong(); //每个block 占字节数
        long totalBlocks = stat.getBlockCountLong(); //block总数
        return totalBlocks * blockSize;
    }


    /**
     * 外部存储是否可用 (存在且具有读写权限)
     */
    public static boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取手机外部可用空间大小
     * @return
     */
    public static long getAvailableExternalMemorySize() {
        if (isExternalStorageAvailable()) {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return availableBlocks * blockSize;
        } else {
            return 0;
        }
    }

    /**
     * 获取手机外部总空间大小
     * @return
     */
    public static long getTotalExternalMemorySize() {
        if (isExternalStorageAvailable()) {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return totalBlocks * blockSize;
        } else {
            return 0;
        }
    }


    /**
     * [获取应用程序版本名称信息]
     * @param context
     * @return 当前应用的版本名称
     */
    public static synchronized String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * [获取应用程序版本名称信息]
     * @param context
     * @return 当前应用的版本名称
     */
    public static synchronized int getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
