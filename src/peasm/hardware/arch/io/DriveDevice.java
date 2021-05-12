/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peasm.hardware.arch.io;

/**
 *
 * @author eddy
 */
public interface DriveDevice {
    
    public byte[] readSector(int track_number,int sector_number); // sector_number is sector from track reférence ex ( sector 2 and 5 is different sectors) 
    public boolean writeSector(int track_number,int sector_number, byte[] data); // sector_number is sector from track reférence ex ( sector 2 and 5 is different sectors) 
    public int getNumberOfSectors(int track_number);
    public int getNumberOfTrack();
    public void eraseAll();
    public boolean commit();

}
