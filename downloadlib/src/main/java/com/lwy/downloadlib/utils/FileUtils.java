package com.lwy.downloadlib.utils;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class FileUtils {

    public static String getRandomFileName(String suffix) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss",
                java.util.Locale.getDefault());
        String formatDate = format.format(new Date());
        int random = new Random().nextInt(10000);
        return new StringBuilder().append(formatDate).append("_").append(random).append(".").append(suffix)
                .toString();
    }

    public static String is2String(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static Boolean isFileExist(String filePath) {
        File f = new File(filePath);
        return isFileExist(f);
    }

    public static Boolean isFileExist(File file) {
        return file.exists();
    }

    public static String getRandomFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String formatDate = format.format(new Date());
        int random = new Random().nextInt(1000);
        return new StringBuffer().append(formatDate).append("_").append(random).toString();
    }

    public static void deleteFile(String filePath) {
        File myFile = new File(filePath);
        deleteFile(myFile);
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    public static boolean deleteAllFiles(String path) {
        boolean flag = true;
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        if (!file.isDirectory()) {
            return false;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            temp = new File(path, tempList[i]);
            if (temp.isDirectory())
                deleteAllFiles(temp.getAbsolutePath());
            else {
                temp.delete();
            }

        }
        return flag;
    }

    public static void delFilesBeforeDate(int day, File dir) {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE)
                - day);
        Date borderDate = now.getTime();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File f : files) {
                long time = f.lastModified();
                now.setTimeInMillis(time);
                if (now.getTime().before(borderDate)) {
                    f.delete();
                }
            }
        }
    }

    public static boolean string2file(String str, String filePath) {
        boolean flag = false;
        BufferedWriter bw = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                if (!file.getParentFile().exists())
                    file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(str);// 把整个json文件保存起来
            bw.flush();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public static String file2string(String filePath) {
        File file = new File(filePath);
        BufferedReader br = null;
        try {
            FileReader fr = new FileReader(file);
            br = new BufferedReader(fr);
            String str = null;
            StringWriter sw = new StringWriter();
            while ((str = br.readLine()) != null) {
                sw.write(str);
            }
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public static void saveOwnObject(Context context, Object obj, String fileName, StringBuffer errorMsg) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            File file = new File(context.getFilesDir(), fileName);
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
        } catch (Exception e) {
            e.printStackTrace();
            if (errorMsg != null)
                errorMsg.append(e.toString());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    if (errorMsg != null)
                        errorMsg.append(e.toString());
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    if (errorMsg != null)
                        errorMsg.append(e.toString());
                    e.printStackTrace();
                }
            }
        }
    }

    public static void write2file(byte[] bfile, String filePath) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            file = new File(filePath);
            File dir = file.getParentFile();
            if (!dir.exists() && dir.isDirectory()) {//
                dir.mkdirs();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static Object getOwnObject(Context context, String fileName, StringBuffer errorMsg) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            File file = new File(context.getFilesDir(), fileName);
            if (!file.exists()) {
                errorMsg.append("文件不存在");
                return null;
            }
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            if (errorMsg != null)
                errorMsg.append(e.toString());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    if (errorMsg != null)
                        errorMsg.append(e.toString());
                    e.printStackTrace();
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    if (errorMsg != null)
                        errorMsg.append(e.toString());
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static boolean deleteOwnObject(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        if (file.exists()) {
            return file.delete();
        } else
            return true;
    }

    public static void saveExternalObject(Context context, Object obj, String filePath, StringBuffer errorMsg) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            } else if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();

            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
        } catch (Exception e) {
            e.printStackTrace();
            if (errorMsg != null)
                errorMsg.append(e.toString());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    if (errorMsg != null)
                        errorMsg.append(e.toString());
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    if (errorMsg != null)
                        errorMsg.append(e.toString());
                    e.printStackTrace();
                }
            }
        }
    }

    public static Object getExternalObject(Context context, String filePath, StringBuffer errorMsg) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                errorMsg.append("文件不存在");
                return null;
            }
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            if (errorMsg != null)
                errorMsg.append(e.toString());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    if (errorMsg != null)
                        errorMsg.append(e.toString());
                    e.printStackTrace();
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    if (errorMsg != null)
                        errorMsg.append(e.toString());
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static boolean copyToFile(InputStream inputStream, File destFile) throws IOException {
        if (destFile.exists()) {
            destFile.delete();
        }
        OutputStream out = new FileOutputStream(destFile);
        try {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) >= 0) {
                out.write(buffer, 0, bytesRead);
            }
        } finally {
            out.close();
        }
        return true;
    }

    public static String getStringFromFile(String filePath) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        File file = new File(filePath);
        InputStream is = new FileInputStream(file);
        BufferedReader bf = new BufferedReader(new InputStreamReader(is
        ));
        String line;
        while ((line = bf.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

}
