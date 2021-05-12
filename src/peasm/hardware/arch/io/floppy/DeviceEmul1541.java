/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peasm.hardware.arch.io.floppy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import peasm.hardware.arch.io.Device;
import peasm.hardware.arch.io.DriveDevice;

/**
 *
 * @author eddy
 *
 * D64 format : Information get from
 * http://unusedino.de/ec64/technical/formats/d64.html
 */
public class DeviceEmul1541 implements Device, DriveDevice {

    final static int DISK_SIZE = 174848;
    final static int SECTOR_SIZE = 256;
    final static int TRACK_SIZE = 40;
    final static int FIRST_TRACK_TO_READ = 17; // (Track 18 - 1)

    File localFile;
    private byte[] imageDrive;

    private int trackInfo[][] = {
        {21, 0x00000},
        {21, 0x01500},
        {21, 0x02A00},
        {21, 0x03F00},
        {21, 0x05400},
        {21, 0x06900},
        {21, 0x07E00},
        {21, 0x09300},
        {21, 0x0A800},
        {21, 0x0BD00},
        {21, 0x0D200},
        {21, 0x0E700},
        {21, 0x0FC00},
        {21, 0x11100},
        {21, 0x12600},
        {21, 0x13B00},
        {21, 0x15000},
        {19, 0x16500},
        {19, 0x17800},
        {19, 0x18B00},
        {19, 0x19E00},
        {19, 0x1B100},
        {19, 0x1C400},
        {19, 0x1D700},
        {18, 0x1EA00},
        {18, 0x1FC00},
        {18, 0x20E00},
        {18, 0x22000},
        {18, 0x23200},
        {18, 0x24400},
        {17, 0x25600},
        {17, 0x26700},
        {17, 0x27800},
        {17, 0x28900},
        {17, 0x29A00},
        {17, 0x2AB00},
        {17, 0x2BC00},
        {17, 0x2CD00},
        {17, 0x2DE00},
        {17, 0x2EF00}
    };

    @Override
    public String getDeviceName() {
        return "PEA_1541_DRIVE";
    }

    @Override
    public String getDeviceVersion() {
        return "0.1";
    }

    @Override
    public byte[] readSector(int track_number, int sector_number) {
        byte[] sector = new byte[SECTOR_SIZE];
        if (sector_number < getNumberOfSectors(track_number)) {
            int sectorOffset = trackInfo[track_number][1] + sector_number * SECTOR_SIZE;
            for (int i = 0; i < SECTOR_SIZE; i++) {
                sector[i] = imageDrive[sectorOffset + i];
            }
        }
        return sector;
    }

    @Override
    public int getNumberOfSectors(int track_number) {
        return trackInfo[track_number][0];
    }

    @Override
    public int getNumberOfTrack() {
        return TRACK_SIZE;
    }

    @Override
    public boolean setLocalFile(String filename) {
        try {
            localFile = new File(filename);
            imageDrive = Files.readAllBytes(Paths.get(filename));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public byte[] getData() {
        return imageDrive;
    }

    @Override
    public boolean writeSector(int track_number, int sector_number, byte[] data) {
        int offset = trackInfo[track_number][1] + (SECTOR_SIZE * sector_number);
        if (sector_number < trackInfo[track_number][0]) {
            for (int i = 0; i < SECTOR_SIZE; i++) {
                imageDrive[offset + i] = data[i];
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void eraseAll() {
        imageDrive = new byte[DISK_SIZE];

    }

    @Override
    public boolean commit() {
        try {
            Path write = Files.write(localFile.toPath(), imageDrive);
        } catch (IOException ex) {
            Logger.getLogger(DeviceEmul1541.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

}
