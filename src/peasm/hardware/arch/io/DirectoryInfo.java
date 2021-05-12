/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peasm.hardware.arch.io;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eddy
 */
public class DirectoryInfo {
    
    String drivename;
    List <FileDescriptor> files=new ArrayList();
    int spaceAvailable;
    public void addFile(FileDescriptor fd){
        files.add(fd);
    }

    public String getDrivename() {
        return drivename;
    }

    public void setDrivename(String drivename) {
        this.drivename = drivename;
    }

    public List<FileDescriptor> getFiles() {
        return files;
    }

    public int getSpaceAvailable() {
        return spaceAvailable;
    }

    public void setSpaceAvailable(int spaceAvailable) {
        this.spaceAvailable = spaceAvailable;
    }
    
}
