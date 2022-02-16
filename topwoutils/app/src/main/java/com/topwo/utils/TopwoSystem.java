package com.topwo.utils;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class TopwoSystem {
    private static final String TAG = TopwoSystem.class.getSimpleName();

    private static Context sContext = null;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    /**
     * Byte转换为KB或者MB，将获取的内存大小规格化
     * @return
     */
    public static String formatFileSize(long sizeBytes) {
        return Formatter.formatFileSize(sContext, sizeBytes);
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
    public static long getAvailMemory() {// 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) sContext.getSystemService(Context.ACTIVITY_SERVICE);
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
        long blockSize = 0; //每个block 占字节数
        long availableBlocks = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        }
        else {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }

    /**
     * 获取手机内部空间大小
     * @return
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory(); //Gets the Android data directory
        StatFs stat = new StatFs(path.getPath());
        long blockSize = 0; //每个block 占字节数
        long totalBlocks = 0; //block总数
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            totalBlocks = stat.getBlockCountLong();
        }
        else {
            blockSize = stat.getBlockSize();
            totalBlocks = stat.getBlockCount();
        }
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
            long blockSize = 0;
            long availableBlocks = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
                availableBlocks = stat.getAvailableBlocksLong();
            }
            else {
                blockSize = stat.getBlockSize();
                availableBlocks = stat.getAvailableBlocks();
            }
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
            long blockSize = 0;
            long totalBlocks = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
                totalBlocks = stat.getBlockCountLong();
            }
            else {
                blockSize = stat.getBlockSize();
                totalBlocks = stat.getBlockCount();
            }
            return totalBlocks * blockSize;
        } else {
            return 0;
        }
    }

    //获取包的签名
    public static byte[] getSignature() {
        try{
            PackageInfo packageInfo = sContext.getPackageManager().getPackageInfo(sContext.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];

            System.out.println(signs[0].toCharsString());
            return sign.toByteArray();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用程序名称
     */
    public static synchronized String getAppName() {
        try {
            PackageManager packageManager = sContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(sContext.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return sContext.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图标 bitmap
     */
    public static synchronized Bitmap getIconBitmap() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = sContext.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(sContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        Drawable d = packageManager.getApplicationIcon(applicationInfo); //xxx根据自己的情况获取drawable
        BitmapDrawable bd = (BitmapDrawable) d;
        Bitmap bm = bd.getBitmap();
        return bm;
    }

    /**
     * [获取应用程序版本名称信息]
     * @return 当前应用的版本名称
     */
    public static synchronized String getPackageName() {
        try {
            PackageManager packageManager = sContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(sContext.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [获取应用程序版本名称信息]
     * @return 当前应用的版本名称
     */
    public static synchronized String getVersionName() {
        try {
            PackageManager packageManager = sContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(sContext.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * [获取应用程序版本名称信息]
     * @return 当前应用的版本名称
     */
    public static synchronized int getVersionCode() {
        try {
            PackageManager packageManager = sContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(sContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    //获取包的签名的序列号
    public static String getSignatureSerialNumber() {
        try{
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(getSignature()));
            //cert.getSigAlgName();
            String pubKey = cert.getPublicKey().toString();
            String signNumber = cert.getSerialNumber().toString();
            String subjectDN = cert.getSubjectDN().toString();
            String ret = subjectDN.substring(3) + signNumber;
//          FileOutputStream fout = new FileOutputStream("/sdcard/test.txt");
//          byte [] bytes = ret.getBytes();
//          fout.write(bytes);
//          fout.close();
            return ret;
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dpToPx(final float dp) {
        return (int) (dp * sContext.getResources().getDisplayMetrics().density);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int pxToDp(float pxValue) {
        final float scale = sContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * 显示退出对话框
     */
    public static void showExitDialog(int iconResId, int titleResId, int msgResId, int leftButtonResId, int rightButtonResId, DialogInterface.OnClickListener leftOnClickListener, DialogInterface.OnClickListener rightOnClickListener) {
        new AlertDialog.Builder(sContext)
                .setIcon(iconResId)
                .setTitle(titleResId)
                .setMessage(msgResId)
                .setNegativeButton(leftButtonResId, leftOnClickListener)
                .setPositiveButton(rightButtonResId, rightOnClickListener)
                .show();
    }
}
