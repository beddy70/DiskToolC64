/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peasm.hardware.arch.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import peasm.hardware.arch.io.floppy.DeviceEmul1541;
import peasm.hardware.arch.io.floppy.FileSystemCBM;

/**
 *
 * @author eddy
 */
public class ReadCBMFSD64 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.out.println("Usage:");
            System.out.println(" [D64 image file] -c [drivename]                       -> create and format disk");
            System.out.println(" [D64 image file] -f [drivename]                       -> format disk");
            System.out.println(" [D64 image file] -d                                   -> read directory");
            System.out.println(" [D64 image file] -p [local file] [new name]           -> put file");
            System.out.println(" [D64 image file] -g [cbm file]   [local path]         -> get file");
            System.out.println(" [D64 image file] -r [cbm file]                        -> remove file");
            System.out.println("(c) Written by Eddy Briere (2019) - peassembler@yahoo.fr http://somanybits.com");
            System.exit(0);
        }

        String d64image = args[0];
        char option = ' ';
        String param = "";
        if (args.length > 1) {
            if (!"-".equals(args[1].substring(0, 1))) {
                System.err.println("Error argument " + args[1] + " type ReadCBMFSD64 without parameters for more info.");
                System.exit(0);
            } else {
                option = args[1].charAt(1);
            }
        }

        DeviceEmul1541 floppy = new DeviceEmul1541();
        floppy.setLocalFile(d64image);
        FileSystemCBM fscbm = new FileSystemCBM(floppy);
        switch (option) {
            case 'd':
                System.out.println("Drive:'" + floppy.getDeviceName() + "' - Version:" + floppy.getDeviceVersion() + "\n");
                System.out.println("------------------------------------");
                DirectoryInfo dir = fscbm.readDirectory();
                List<FileDescriptor> files = dir.getFiles();
                for (int i = 0; i < files.size(); i++) {
                    FileDescriptor fd = files.get(i);
                    System.out.printf("%-20s    %5d bytes\n", fd.getFilename() + "." + fd.getParam(FileSystemCBM.CBM_PARAM_FILETYPE), fd.getParam(FileSystemCBM.CBM_PARAM_FILESIZE));
                }
                System.out.println("------------------------------------");
                System.out.println(dir.getFiles().size() + " file(s)" + " freespace " + dir.getSpaceAvailable() + " bytes\n");
                break;
            case 'c':
                System.out.println("Create drive : " + d64image);
                RandomAccessFile raf = new RandomAccessFile(d64image, "rw");
                try {
                    raf.setLength(FileSystemCBM.DISK_SIZE);
                } finally {
                    raf.close();
                }
            case 'f':
                if (args.length == 3) {
                    System.out.println("Format drive : " + d64image + " named : " + args[2]);
                    fscbm.format(args[2], null);
                    floppy.commit();
                }
                break;
            case 'p':
                if (args.length == 4) {
                    FileDescriptor fd = new FileDescriptor();
                    fd.setFilename(args[3]);
                    fd.setFiletype(FileDescriptor.TYPE_FILE);
                    fd.addParam(FileSystemCBM.CBM_PARAM_FILETYPE, FileSystemCBM.CBM_FILE_TYPE_PRG);
                    byte[] data;

                    data = Files.readAllBytes(Paths.get(args[2]));
                    //System.out.println("date size = " + data.length);
                    fscbm.addFile(fd, data);
                }
                //floppy.commit();

                break;
            case 'g':
                dir = fscbm.readDirectory();
                if (!dir.getFiles().isEmpty()) {
                    for (FileDescriptor diskfiles : dir.getFiles()) {
                        //System.out.println(">"+diskfiles.getFilename());
                        if (diskfiles.getFilename() == null ? args[2] == null : diskfiles.getFilename().equalsIgnoreCase(args[2])) {
                            String strext = "";
                            if (diskfiles.getParam(FileSystemCBM.CBM_PARAM_FILETYPE) != null) {
                                strext = ".PRG";
                            }
                            System.out.println("file found : " + diskfiles.getFilename()+strext);
                            byte[] data = fscbm.getFile(diskfiles);

                            //System.out.println("data " + data.length);
                            try (FileOutputStream fos = new FileOutputStream(args[3] + "/" + args[2] + strext)) {
                                fos.write(data);
                            }
                        }
                    }

                }
                break;
            case 'r':
                dir = fscbm.readDirectory();
                if (!dir.getFiles().isEmpty()) {
                    for (FileDescriptor diskfiles : dir.getFiles()) {
                        //System.out.println(">"+diskfiles.getFilename());
                        if (diskfiles.getFilename() == null ? args[2] == null : diskfiles.getFilename().equalsIgnoreCase(args[2])) {
                            //System.out.println("file found " + diskfiles.getFiletype());
                            fscbm.removeFile(diskfiles);
                            System.out.println(diskfiles.getFilename() + " removed");
                        }
                    }

                }
                break;
            default:
                System.err.println("Error argument " + args[1] + " type ReadCBMFSD64 without parameters for more info.");
                System.exit(0);
        }

        // TODO code application logic here
        //DeviceEmul1541 floppy = new DeviceEmul1541();
        //floppy.setLocalFile("/Users/eddy/Documents/GAME_C64/labyrin1.d64");
        //floppy.setLocalFile("/Users/eddy/Documents/GAME_C64/diskTest.d64");
//        floppy.setLocalFile("/Users/eddy/Documents/GAME_C64/test.d64");
//
//        System.out.println("Device : " + floppy.getDeviceName());
//        System.out.println();
//        FileSystemCBM fscbm = new FileSystemCBM(floppy);
////        fscbm.format("TEST PEA", null);
////        floppy.commit();
//
//        System.out.println(fscbm.getDriveName());
//        System.out.println("-----------------------");
//
//        DirectoryInfo dir = fscbm.readDirectory();
//        System.out.println("-----------------------");
//        System.out.println(dir.getFiles().size() + " file(s)" + " freespace " + dir.getSpaceAvailable() + " bytes\n");
//
//        FileDescriptor fd = new FileDescriptor();
//        fd.setFilename("test FS CBM4");
//        fd.setFiletype(FileDescriptor.TYPE_FILE);
//        fd.addParam(FileSystemCBM.CBM_PARAM_FILETYPE, FileSystemCBM.CBM_FILE_TYPE_PRG);
//        byte[] data;
//                
//        data = Files.readAllBytes(Paths.get("/Users/eddy/Documents/GAME_C64/text2.txt"));
//        System.out.println(fscbm.addFile(fd, data));
//        if (!dir.getFiles().isEmpty()) {
//            fscbm.getFile(dir.getFiles().get(0));
//        }
    }

}
