/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peasm.hardware.arch.io;

import java.io.File;

/**
 *
 * @author eddy
 */
public interface Device {
    
    public String getDeviceName();
    public String getDeviceVersion();
    public boolean setLocalFile(String filename);
    public byte[] getData();
}
