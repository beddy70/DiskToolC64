/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peasm.hardware.arch.io;

import java.util.HashMap;

/**
 *
 * @author eddy
 */
public interface FilesSystem {

    public String getDriveName();

    public DirectoryInfo readDirectory();
    
    public String getVersion();

    //public FileDescriptor createFile(String filename, int filetype);

    public int removeFile(FileDescriptor fd);

    public int addFile(FileDescriptor fd,byte [] data);

    public byte[] getFile(FileDescriptor fd);

    public int moveFile(DirectoryInfo dis, DirectoryInfo did);

    public int renameFile(FileDescriptor fd, String newname);

    public int setPath(String path);

    public String getPath();

    public boolean isFormatted();

    public boolean format(String driveName, HashMap  <String,Object> options);



}
