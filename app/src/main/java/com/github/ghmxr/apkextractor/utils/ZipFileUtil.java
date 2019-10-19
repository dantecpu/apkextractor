package com.github.ghmxr.apkextractor.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ghmxr.apkextractor.items.FileItem;
import com.github.ghmxr.apkextractor.items.ImportItem;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipFileUtil {

    public static @Nullable ZipFileInfo getZipFileInfoOfImportItem(@NonNull ImportItem importItem){
        FileItem fileItem=importItem.getFileItem();
        try{
            if(fileItem.isDocumentFile()){
                return getZipFileInfoOfZipInputStream(fileItem.getInputStream());
            }else if(fileItem.isFileInstance()){
                return getZipFileInfoOfZipFile(new ZipFile(fileItem.getFile()));
            }
        }catch (Exception e){e.printStackTrace();}
        return null;
    }

    private static @NonNull ZipFileInfo getZipFileInfoOfZipFile(ZipFile zipFile){
        ZipFileInfo zipFileInfo=new ZipFileInfo();
        try{
            Enumeration entries =zipFile.entries();
            while (entries.hasMoreElements()){
                ZipEntry zipEntry=(ZipEntry) entries.nextElement();
                String entryPath=zipEntry.getName().replaceAll("\\*", "/");
                if((entryPath.toLowerCase().startsWith("android/data"))&&!zipEntry.isDirectory()){
                    zipFileInfo.addEntry(entryPath);
                    zipFileInfo.addDataSize(zipEntry.getSize());
                }
                if((entryPath.toLowerCase().startsWith("android/obb"))&&!zipEntry.isDirectory()){
                    zipFileInfo.addEntry(entryPath);
                    zipFileInfo.addObbSize(zipEntry.getSize());
                }
                if((entryPath.toLowerCase().endsWith(".apk"))&&!zipEntry.isDirectory()){
                    zipFileInfo.addEntry(entryPath);
                    zipFileInfo.addApkSize(zipEntry.getSize());
                }
            }

        }catch (Exception e){e.printStackTrace();}
        return zipFileInfo;
    }

    /**
     * 获取一个zip文件中data或者obb的大小，为耗时阻塞方法
     * @return 字节
     */
    private static @NonNull ZipFileInfo getZipFileInfoOfZipInputStream(@NonNull InputStream inputStream){
        ZipFileInfo zipFileInfo=new ZipFileInfo();
        try{
            ZipInputStream zipInputStream=new ZipInputStream(inputStream);
            ZipEntry zipEntry=zipInputStream.getNextEntry();
            while (zipEntry!=null){
                String entryPath=zipEntry.getName().replaceAll("\\*", "/");
                if((entryPath.toLowerCase().startsWith("android/data"))&&!zipEntry.isDirectory()){
                    zipFileInfo.addEntry(entryPath);
                    long total_this_file=0;
                    int len;
                    byte[] buffer=new byte[1024];
                    while ((len=zipInputStream.read(buffer))!=-1){
                        total_this_file+=len;
                    }
                    zipFileInfo.addDataSize(total_this_file);
                }
                if(entryPath.toLowerCase().startsWith("android/obb")&&!zipEntry.isDirectory()){
                    zipFileInfo.addEntry(entryPath);
                    long total_this_file=0;
                    int len;
                    byte[] buffer=new byte[1024];
                    while ((len=zipInputStream.read(buffer))!=-1){
                        total_this_file+=len;
                    }
                    zipFileInfo.addObbSize(total_this_file);
                }
                if(entryPath.toLowerCase().endsWith(".apk")&&!zipEntry.isDirectory()){
                    zipFileInfo.addEntry(entryPath);
                    long total_this_file=0;
                    int len;
                    byte[] buffer=new byte[1024];
                    while ((len=zipInputStream.read(buffer))!=-1){
                        total_this_file+=len;
                    }
                    zipFileInfo.addApkSize(total_this_file);
                }

                zipEntry=zipInputStream.getNextEntry();
            }
            zipInputStream.close();
        }catch (Exception e){e.printStackTrace();}
        return zipFileInfo;
    }

    /**
     * 包含此zip包中的data,obb大小信息以及获取主存储中已存在的重复文件信息
     */
    public static class ZipFileInfo{
        private final ArrayList<String>entryPaths=new ArrayList<>();
        long dataSize=0;
        long obbSize=0;
        long apkSize=0;
        private ZipFileInfo (){}

        private void addEntry(String entryPath){
            entryPaths.add(entryPath);
        }

        private void addDataSize(long dataSize) {
            this.dataSize+= dataSize;
        }

        private void addObbSize(long obbSize) {
            this.obbSize+= obbSize;
        }

        private void addApkSize(long apkSize){
            this.apkSize+=apkSize;
        }

        public long getDataSize() {
            return dataSize;
        }

        public long getObbSize() {
            return obbSize;
        }

        public long getApkSize() {
            return apkSize;
        }

        public @NonNull String getAlreadyExistingFilesInfoInMainStorage(){
            StringBuilder builder=new StringBuilder();
            try{
                for(String s:entryPaths){
                    File exportWritingTarget=new File(StorageUtil.getMainExternalStoragePath()+"/"+s);
                    if(exportWritingTarget.exists()){
                        builder.append(exportWritingTarget.getAbsolutePath());
                        builder.append("\n\n");
                    }
                }
            }catch (Exception e){e.printStackTrace();}
            return builder.toString();
        }
    }
}