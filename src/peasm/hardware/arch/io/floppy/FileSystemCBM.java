/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peasm.hardware.arch.io.floppy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import peasm.hardware.arch.io.DirectoryInfo;
import peasm.hardware.arch.io.FileDescriptor;
import peasm.hardware.arch.io.FilesSystem;

/**
 *
 * @author eddy
 */
public class FileSystemCBM implements FilesSystem {

    public final static int DIR_OFFSET_FILETYPE = 0x02;
    public final static int DIR_OFFSET_FILENAME = 0x05;
    public final static int DIR_FIRST_TRACK_FILE = 0x03;
    public final static int DIR_FIRST_SECTOR_FILE = 0x04;
    public final static int DIR_SECTOR_SIZE_LOW = 0x1e;
    public final static int DIR_SECTOR_SIZE_HIGHT = 0x1f;
    public final static int DIR_MAX_LENGTH_FILENAME = 16;
    public final static int DIR_MAX_ENTRIES_PER_SECTOR = 8;

    public final static int BAM_TRACK_NUMBER = 17; // Track 18  

    public final static int BAM_OFFSET_DRIVE_NAME = 0x90;
    public final static int BAM_OFFSET_DIRECTORY_TRACK_SECTOR = 0x00;
    public final static int BAM_OFFSET_DOS_VERSION = 0x2;
    public final static int BAM_OFFSET_DISK_ID = 0xA2;

    public final static int BAM_OFFSET_DOS_TYPE = 0XA5;
    public final static int BAM_OFFSET_TABLE = 0X04;

    public final static String CBM_PARAM_FILETYPE = "FILETYPE";
    public final static String CBM_PARAM_FILESIZE = "FILESIZE";
    public final static String CBM_PARAM_SECTORUSED = "SECTORUSED";
    public final static String CBM_PARAM_FIRST_TRACK_SECTOR = "FIRSTTRACKSECTOR";
    public final static String CBM_PARAM_TRACK_ENTRY = "TRACKENTRY";
    public final static String CBM_PARAM_SECTOR_ENTRY = "SECTORENTRY";
    public final static String CBM_PARAM_INDEX_ENTRY = "INDEXENTRY";

    public final static byte CBM_FILE_TYPE_SCRATCHED = 0x00;
    public final static byte CBM_FILE_TYPE_DEL = (byte) 0x80;
    public final static byte CBM_FILE_TYPE_SEQ = (byte) 0x81;
    public final static byte CBM_FILE_TYPE_PRG = (byte) 0x82;
    public final static byte CBM_FILE_TYPE_USR = (byte) 0x83;
    public final static byte CBM_FILE_TYPE_REL = (byte) 0x84;

    public final static String FORMAT_OPTION_VERSION = "FORMATOPTION";

    static final byte[] bitInverse = {
        (byte) (0x80 & 0xff),
        0x40,
        0x20,
        0x10,
        0x08,
        0x04,
        0x02,
        0x01
    };
    public static long DISK_SIZE = 174848;
    DirectoryInfo dir;
    DeviceEmul1541 dev;

    public FileSystemCBM(DeviceEmul1541 dev) {
        this.dev = dev;
    }

    @Override
    public String getVersion() {
        return "0.5";
    }

    @Override
    public DirectoryInfo readDirectory() {

        byte[] sector;

        dir = new DirectoryInfo();
        dir.setDrivename(getDriveName());

        //Read sector BAM AREA
        sector = dev.readSector(BAM_TRACK_NUMBER, 0); // read first directory entry from track 18 and sector 0
        int nextTrack = sector[0];
        int nextSector = sector[1];

        do {

            //Read directory
            if (nextTrack == 0) {
                break;
            }
            sector = dev.readSector(nextTrack - 1, nextSector); // read first directory entry from track 18 and sector 0

            for (int i = 0; i < DIR_MAX_ENTRIES_PER_SECTOR; i++) {

                String filetype = null;
                filetype = FileSystemCBM.getfileCBMType((int) (sector[DIR_OFFSET_FILETYPE + 32 * i] & 0xFF));

                String filename = "";
                for (int j = 0; j < DIR_MAX_LENGTH_FILENAME; j++) {
                    int letterPointer = (int) (sector[DIR_OFFSET_FILENAME + i * 32 + j]) & 0xff;
                    if (letterPointer != 0x00 && ((letterPointer & 0xff) != 0xA0)) {
                        filename += (char) letterPointer;
                    }
                }
                if (filename.length() == 0) {
                    break; //no more files in the directory
                }
                //create filedescriptor
                FileDescriptor fd = new FileDescriptor();
                fd.setFiletype(FileDescriptor.TYPE_FILE);
                fd.setFilename(filename);
                fd.addParam(CBM_PARAM_FILETYPE, filetype);

                int sectorUsed = ((int) (sector[i * 32 + DIR_SECTOR_SIZE_LOW] & 0xff) + (int) (sector[i * 32 + DIR_SECTOR_SIZE_HIGHT] & 0xff) * 256);
                fd.addParam(CBM_PARAM_FILESIZE, sectorUsed * 254);
                fd.addParam(CBM_PARAM_SECTORUSED, sectorUsed);

                int[] firstTrackSector = new int[2];
                firstTrackSector[0] = sector[i * 32 + DIR_FIRST_TRACK_FILE];
                firstTrackSector[1] = sector[i * 32 + DIR_FIRST_SECTOR_FILE];
                fd.addParam(CBM_PARAM_FIRST_TRACK_SECTOR, firstTrackSector);

                fd.addParam(CBM_PARAM_TRACK_ENTRY, nextTrack);
                fd.addParam(CBM_PARAM_SECTOR_ENTRY, nextSector);

                fd.addParam(CBM_PARAM_INDEX_ENTRY, i);

                dir.addFile(fd);

            }
            //System.out.println("t:"+nextTrack+" s:"+nextSector);
            dir.setSpaceAvailable(getNumberOfSectorFree() * 254);
            nextTrack = sector[0];
            nextSector = sector[1];

        } while (nextTrack != 0);
        return dir;
    }

    int getNumberOfSectorFree() {
        int freeSpace = 0;
        byte[] bam_sector = dev.readSector(17, 0);
        for (int i = 0; i < 35; i++) {
            if (i != 17) {
                freeSpace += bam_sector[BAM_OFFSET_TABLE + (i * 4)];
            }
        }
        return freeSpace;
    }

//    @Override
//
//    public FileDescriptor createFile(String filename, int filetype) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    @Override
    public int removeFile(FileDescriptor fd) {
        int[] firsttracksector = (int[]) fd.getParam(CBM_PARAM_FIRST_TRACK_SECTOR);
        //System.out.println("file start from t:" + firsttracksector[0] + " s:" + firsttracksector[1]);
        //free sectors
        int nexttrack = firsttracksector[0] - 1;
        int nextsector = firsttracksector[1];
        byte[] sector;
        do {
            //System.out.println("data t:" + (nexttrack + 1) + " s:" + nextsector);
            setBitBAM(nexttrack, nextsector, true);
            sector = dev.readSector(nexttrack, nextsector);
            nexttrack = sector[0] - 1;
            nextsector = sector[1];

        } while (sector[0] != 0);
        //free entry

        int track_entry = (int) fd.getParam(CBM_PARAM_TRACK_ENTRY);
        int sector_entry = (int) fd.getParam(CBM_PARAM_SECTOR_ENTRY);
        int index_entry = (int) fd.getParam(CBM_PARAM_INDEX_ENTRY);

        sector = dev.readSector(track_entry - 1, sector_entry);
        sector[32 * index_entry + DIR_OFFSET_FILETYPE] = CBM_FILE_TYPE_SCRATCHED;
        dev.writeSector(track_entry - 1, sector_entry, sector);
        dev.commit();
        return 0;
    }

    @Override
    public int addFile(FileDescriptor fd, byte[] data) {
        //Check if file don't exist
        DirectoryInfo dir = readDirectory();
        List<FileDescriptor> lstfiles = dir.getFiles();

        for (int i = 0; i < lstfiles.size(); i++) {
            if (lstfiles.get(i).getFilename().equals(normalizeFileName(fd.getFilename()))) {
                System.err.println("File '" + fd.getFilename() + "' exist and replaced");
                removeFile(lstfiles.get(i));
                //return -1;
            }
        }

        int nbsector = 0;
        //compute number of sector to save data
        if (data != null) {
            nbsector = (int) data.length / 254;
            if ((data.length % 254) != 0) {
                nbsector++;
            }
        }
//        System.out.println("Nb sector = " + nbsector);
        //find sectors free
        int[][] tracksectorfree = findTrackSectorFree(nbsector);

        //Show list of track/sector free
//        for (int j = 0; j < tracksectorfree.length; j++) {
//
//            System.out.println("[" + tracksectorfree[j][0] + "][" + tracksectorfree[j][1] + "] ");
//
//            System.out.println();
//        }
        //write data on drive
        if (fd.getFilename().length() != 0) {
            for (int i = 0; i < tracksectorfree.length; i++) {

                byte[] newsector = new byte[DeviceEmul1541.SECTOR_SIZE];
                int sizedata = 0;

                if (i != (tracksectorfree.length - 1)) {
                    newsector[0] = (byte) (tracksectorfree[i + 1][0] & 0xff);
                    newsector[1] = (byte) (tracksectorfree[i + 1][1] & 0xff);
                    sizedata = DeviceEmul1541.SECTOR_SIZE - 2;
                } else {
                    newsector[0] = 0;
                    newsector[1] = (byte) (data.length + 1 - (i * (DeviceEmul1541.SECTOR_SIZE - 2)) & 0xff); //add 1 to offset
                    sizedata = (newsector[1] - 1) & 0xff; //remove 1 to get good size of data
                }
                //System.out.println("Data = " + data.length);
//                System.out.println("Size Data = " + (int)sizedata);
//                System.out.print("[");
                for (int j = 0; j < (sizedata); j++) {
                    newsector[j + 2] = data[j + (i * (DeviceEmul1541.SECTOR_SIZE - 2))];
//                    System.out.print(String.format("0x%2s", Integer.toHexString(newsector[j + 2])).replace(' ', '0') + " ");
                }
//                System.out.println("]");
                dev.writeSector(tracksectorfree[i][0] - 1, tracksectorfree[i][1], newsector);

            }

        }

        //add entry in the directory
        fd.addParam(CBM_PARAM_TRACK_ENTRY, (byte) tracksectorfree[0][0]);
        fd.addParam(CBM_PARAM_SECTOR_ENTRY, (byte) tracksectorfree[0][1]);
        fd.addParam(CBM_PARAM_SECTORUSED, nbsector);
        boolean entryOk = addDirectoryEntry(fd);

        //update BAM table
        if (entryOk) {
            //byte[] sectorRead = dev.readSector(BAM_TRACK_NUMBER, 0); // read first directory entry from track 18 and sector 0
            for (int[] tracksectorfree1 : tracksectorfree) {
                setBitBAM(tracksectorfree1[0] - 1, tracksectorfree1[1], false);
            }

            dev.commit();
        }
        return nbsector;
    }

    @Override
    public int moveFile(DirectoryInfo dis, DirectoryInfo did) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int renameFile(FileDescriptor fd, String newname) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDriveName() {
        String drivename = "";
        //Read sector BAM AREA
        byte[] sector = dev.readSector(17, 0); // read first directory entry from track 18 and sector 0

        for (int i = 0; i < 16; i++) {
            byte letter = sector[BAM_OFFSET_DRIVE_NAME + i];
            if (((letter & 0xff) != 0xa0) && letter != 0x00) {
                drivename += (char) (letter);
            }
        }
        return drivename.trim();
    }

    public static String getfileCBMType(int type) {
        String filetype = null;
        switch (type & 0x07) {
            case 0x00:
                filetype = "DEL";
                break;
            case 0x01:
                filetype = "SEQ";
                break;
            case 0x02:
                filetype = "PRG";
                break;
            case 0x03:
                filetype = "USR";
                break;
            case 0x04:
                filetype = "REL";
                break;
        }
        return filetype;
    }

    @Override
    public int setPath(String path) {
        return -1; //CBM DOS don't support sub directories
    }

    @Override
    public String getPath() {
        return "/";
    }

    @Override
    public byte[] getFile(FileDescriptor fd) {
        byte[] data;
        System.out.println(fd.getFilename());

        int[] firsttracksector = (int[]) fd.getParam(CBM_PARAM_FIRST_TRACK_SECTOR);
        int nbsector = (int) fd.getParam(CBM_PARAM_SECTORUSED);
        data = new byte[(int) fd.getParam(CBM_PARAM_FILESIZE)];
        byte[] sector = dev.readSector(firsttracksector[0] - 1, firsttracksector[1]);
        //System.out.println("first track=" + firsttracksector[0]  + " sector=" + (int) (firsttracksector[1] & 0xff));
        int realsize = 0;
        for (int i = 0; i < nbsector; i++) {
            int nextTrack = sector[0];
            int nextSector = sector[1];
            //System.out.println("next track=" + nextTrack + " sector=" + (int) (nextSector & 0xff));
            if (nextTrack != 0) {
                realsize += 254;
                getDataFromSector(data, sector, i);

                sector = dev.readSector(sector[0] - 1, sector[1]);
            } else {
                realsize += (int) (nextSector & 0xff) - 1; //sub 1 from size 
                getDataFromSector(data, sector, i, (int) (nextSector & 0xff));
                data = Arrays.copyOfRange(data, 0, realsize);
            }

        }
        System.out.println("size=" + fd.getParam(CBM_PARAM_FILESIZE) + " | real size=" + realsize);
        return data;
    }

    @Override
    public boolean isFormatted() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean format(String driveName, HashMap<String, Object> options) {
        char[] version = new char[2];

        if (options != null) {
            version = (char[]) options.get(FORMAT_OPTION_VERSION);
        } else {
            version[0] = '0';
            version[1] = '0';
        }
        byte[] bam_sector = new byte[256];
        dev.eraseAll();
        //Write drive name
        for (int i = 0; i < DIR_MAX_LENGTH_FILENAME; i++) {
            if (i < driveName.length()) {
                bam_sector[BAM_OFFSET_DRIVE_NAME + i] = (byte) driveName.charAt(i);
            } else {
                bam_sector[BAM_OFFSET_DRIVE_NAME + i] = (byte) 0xA0;
            }
        }
        //DOS VERSION
        bam_sector[BAM_OFFSET_DOS_VERSION] = 0x41;
        //DOS TYPE
        bam_sector[BAM_OFFSET_DOS_TYPE] = '2';
        bam_sector[BAM_OFFSET_DOS_TYPE + 1] = 'A';
        bam_sector[BAM_OFFSET_DOS_TYPE + 2] = (byte) 0xA0;
//        bam_sector[BAM_OFFSET_DOS_TYPE + 3] = (byte) 0xA0;
//        bam_sector[BAM_OFFSET_DOS_TYPE + 4] = (byte) 0xA0;
//        bam_sector[BAM_OFFSET_DOS_TYPE + 5] = (byte) 0xA0;
        //DISK ID
        bam_sector[BAM_OFFSET_DISK_ID - 2] = (byte) 0xA0;
        bam_sector[BAM_OFFSET_DISK_ID - 1] = (byte) 0xA0;
        bam_sector[BAM_OFFSET_DISK_ID] = (byte) version[0];
        bam_sector[BAM_OFFSET_DISK_ID + 1] = (byte) version[1];
        bam_sector[BAM_OFFSET_DISK_ID + 2] = (byte) 0xA0;
        //INIT DIRECTORY TRACK/SECTOR
        bam_sector[BAM_OFFSET_DIRECTORY_TRACK_SECTOR] = 18;
        bam_sector[BAM_OFFSET_DIRECTORY_TRACK_SECTOR + 1] = 1;

        //INIT BAM TABLE
        for (int i = 0; i < 35; i++) {
            int numberOfSector = dev.getNumberOfSectors(i);
            bam_sector[BAM_OFFSET_TABLE + (i * 4)] = (byte) numberOfSector;
            bam_sector[BAM_OFFSET_TABLE + 1 + (i * 4)] = (byte) 0xff;
            bam_sector[BAM_OFFSET_TABLE + 2 + (i * 4)] = (byte) 0xff;
            switch (numberOfSector) {
                case 21:
                    bam_sector[BAM_OFFSET_TABLE + 3 + (i * 4)] = (byte) 0x1f;
                    break;
                case 20:
                    bam_sector[BAM_OFFSET_TABLE + 3 + (i * 4)] = (byte) 0x0f;
                    break;
                case 19:
                    bam_sector[BAM_OFFSET_TABLE + 3 + (i * 4)] = (byte) 0x07;
                    break;
                case 18:
                    bam_sector[BAM_OFFSET_TABLE + 3 + (i * 4)] = (byte) 0x03;
                    break;
                case 17:
                    bam_sector[BAM_OFFSET_TABLE + 3 + (i * 4)] = (byte) 0x01;
                    break;

            }
            if (i == 17) { //track 18
                bam_sector[BAM_OFFSET_TABLE + 1 + (i * 4)] = (byte) 0xFC;
                bam_sector[BAM_OFFSET_TABLE + (i * 4)] = (byte) (numberOfSector - 2);
            }
        }

        //WRITE BAM on drive
        dev.writeSector(18 - 1, 0, bam_sector);
        return true;
    }

    boolean getBitBAM(int trckNum, int sectornum) {
        //sectorBAM[BAM_OFFSET_TABLE + (trckNum * 4) + j];
        byte[] sectorRead = dev.readSector(BAM_TRACK_NUMBER, 0);
        int octet = 1;
        if (sectornum > 15) {
            octet++;
            sectornum -= 8;
        }
        if (sectornum > 7) {
            sectornum -= 8;
            octet++;
        }
        if ((sectorRead[BAM_OFFSET_TABLE + (trckNum * 4) + octet] & (bitInverse[sectornum] & 0xff)) != 0) {
            return true;
        }
        return false;
    }

    void setBitBAM(int trckNum, int sectornum, boolean val) {
        byte[] sectorRead = dev.readSector(BAM_TRACK_NUMBER, 0);
        int octet = 1;
        if (sectornum > 15) {
            octet++;
            sectornum -= 8;
        }
        if (sectornum > 7) {
            sectornum -= 8;
            octet++;
        }
        //System.out.println("b=" + sectornum + " pos=" + octet);

        //System.out.println("bit=" + sectornum + " rbit=" + Integer.toBinaryString(bitInverse[sectornum] & 0xff));
        byte memoctet = sectorRead[BAM_OFFSET_TABLE + (trckNum * 4) + octet];
        if (val) {
            sectorRead[BAM_OFFSET_TABLE + (trckNum * 4) + octet] |= (bitInverse[sectornum] & 0xff);
            if (memoctet != sectorRead[BAM_OFFSET_TABLE + (trckNum * 4) + octet]) {
                sectorRead[BAM_OFFSET_TABLE + (trckNum * 4)]++;
            }
        } else {
            byte rbit = bitInverse[sectornum];
            rbit = (byte) (~rbit & 0xff);
            sectorRead[BAM_OFFSET_TABLE + (trckNum * 4) + octet] &= rbit;
            if (memoctet != sectorRead[BAM_OFFSET_TABLE + (trckNum * 4) + octet]) {
                sectorRead[BAM_OFFSET_TABLE + (trckNum * 4)]--;
            }
        }
        dev.writeSector(BAM_TRACK_NUMBER, 0, sectorRead);
    }

    //functions data from sectors
    private void getDataFromSector(byte[] data, byte[] sector, int index) {
        getDataFromSector(data, sector, index, 254);
    }

    private void getDataFromSector(byte[] data, byte[] sector, int index, int datasize) {
        for (int i = 0; i < datasize; i++) {
            data[index * 254 + i] = sector[i + 2];
        }

    }

    private int[][] findTrackSectorFree(int nbsector) {

        int[][] tracksectorResult = new int[nbsector][2];
        int sectorfound = 0;
        //byte[] sectorBAM = dev.readSector(BAM_TRACK_NUMBER, 0);

        for (int trckNum = 0; trckNum < 35; trckNum++) {
            //byte[] BAMinfo = new byte[4];
//            for (int j = 0; j < 4; j++) {
//                BAMinfo[j] = sectorBAM[BAM_OFFSET_TABLE + (trckNum * 4) + j];
//            }
            //if (BAMinfo[0] > 0 && trckNum != 17) {
            if (trckNum != 17) {

                for (int i = 0; i < dev.getNumberOfSectors(trckNum); i++) {
                    if (sectorfound == nbsector) {
                        break;
                    } else if (getBitBAM(trckNum, i)) {
                        tracksectorResult[sectorfound][0] = trckNum + 1;
                        tracksectorResult[sectorfound][1] = i;
                        sectorfound++;
                    }
                }
            }
            //System.out.println("read track " + trckNum);
        }
        if (sectorfound < nbsector) {
            //System.out.println("No enougth free space");
            return null;
        }

//        for (int i = 0; i < tracksectorResult.length; i++) {
//            System.out.println("Free sectors found track=" + tracksectorResult[i][0] + " sector=" + tracksectorResult[i][1]);
//
//        }
        return tracksectorResult;
    }

    private boolean addDirectoryEntry(FileDescriptor fd) {

        //normalize finalename
        byte[] sectorRead = dev.readSector(BAM_TRACK_NUMBER, 0); // read first directory entry from track 18 and sector 0
        //check if there ara one entry
        if (sectorRead[0] == 0) { //check track number if zero then créate first directory table
            sectorRead[0] = 18;
            sectorRead[1] = 1;
            //System.out.println("First directory table");
        }

        //read directory table
        boolean entryfound = false;

        int s = 1;
        for (int countSector = 0; countSector < 17; countSector++) {
            int sectordir = ((3 * countSector) % 17 + 1); //use method from 1541 to find sector
            sectorRead = dev.readSector(BAM_TRACK_NUMBER, sectordir);

            //read all directory entries 
            for (int i = 0; i < FileSystemCBM.DIR_MAX_ENTRIES_PER_SECTOR; i++) {
                // find free entry
                if (sectorRead[DIR_OFFSET_FILETYPE + i * 32] == 00 || sectorRead[DIR_OFFSET_FILETYPE + i * 32] == (byte) 0x80 || sectorRead[FileSystemCBM.DIR_FIRST_TRACK_FILE + i * 32] == 0) {
                    prepareEntry(fd, sectorRead, i);
                    //System.out.println("New entry n°" + i);
                    dev.writeSector(BAM_TRACK_NUMBER, sectordir, sectorRead);
                    return true;
                }

            }
            sectorRead[0] = 18;
            sectorRead[1] = (byte) (3 * (countSector + 1) % 17 + 1);
            if (sectorRead[1] == 1) {
                sectorRead[1] = 18;
            }
            //System.out.println("allow new dir " + sectorRead[0] + " " + sectorRead[1]);

        }
        //last dir table available
        sectorRead = dev.readSector(BAM_TRACK_NUMBER, 18);

        //read all directory entries 
        for (int i = 0; i < FileSystemCBM.DIR_MAX_ENTRIES_PER_SECTOR; i++) {
            // find free entry
            if (sectorRead[DIR_OFFSET_FILETYPE + i * 32] == 00 || sectorRead[DIR_OFFSET_FILETYPE + i * 32] == (byte) 0x80 || sectorRead[FileSystemCBM.DIR_FIRST_TRACK_FILE + i * 32] == 0) {
                prepareEntry(fd, sectorRead, i);
                //System.out.println("New entry n°" + i);
                dev.writeSector(BAM_TRACK_NUMBER, 18, sectorRead);
                return true;
            }

        }
        return false;
    }

    private String normalizeFileName(String filename) {
        String filenameCBM = filename.toUpperCase();
        if (filename.length() > FileSystemCBM.DIR_MAX_LENGTH_FILENAME) {
            filenameCBM = filename.substring(0, FileSystemCBM.DIR_MAX_LENGTH_FILENAME);
        }
        return filenameCBM;
    }

    private void prepareEntry(FileDescriptor fd, byte[] sector, int entry) {
        String filenameCBM = normalizeFileName(fd.getFilename());
        if (fd.getFilename().length() > FileSystemCBM.DIR_MAX_LENGTH_FILENAME) {
            filenameCBM = fd.getFilename().substring(0, FileSystemCBM.DIR_MAX_LENGTH_FILENAME);
        }
        //System.out.println("filename CBM :" + filenameCBM);

        sector[DIR_FIRST_TRACK_FILE + entry * 32] = (byte) (fd.getParam(CBM_PARAM_TRACK_ENTRY));
        sector[DIR_FIRST_SECTOR_FILE + entry * 32] = (byte) (fd.getParam(CBM_PARAM_SECTOR_ENTRY));
        for (int j = 0; j < DIR_MAX_LENGTH_FILENAME; j++) {
            if (j < filenameCBM.length()) {
                sector[DIR_OFFSET_FILENAME + j + entry * 32] = (byte) filenameCBM.charAt(j);
            } else {
                sector[DIR_OFFSET_FILENAME + j + entry * 32] = (byte) 0xA0;
            }
        }
        //SECTOR USED
        int sectorUsed = (int) fd.getParam(CBM_PARAM_SECTORUSED);
        sector[DIR_SECTOR_SIZE_LOW + entry * 32] = (byte) (sectorUsed & 0xff);
        sector[DIR_SECTOR_SIZE_HIGHT + entry * 32] = (byte) ((sectorUsed >> 8) & 0xff);
        //FILE TYPE
        sector[DIR_OFFSET_FILETYPE + entry * 32] = (byte) fd.getParam(CBM_PARAM_FILETYPE);
    }

}
