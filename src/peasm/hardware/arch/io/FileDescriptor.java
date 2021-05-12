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
public class FileDescriptor {

    public final static int TYPE_FILE = 0x01;
    public final static int TYPE_FOLDER = 0x02;
    public final static int TYPE_LINK = 0x03;

    String filename;
    int filetype;
    HashMap<String, Object> param = new HashMap();

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getFiletype() {
        return filetype;
    }

    public void setFiletype(int filetype) {
        this.filetype = filetype;
    }

    public HashMap<String, Object> getParam() {
        return param;
    }

    public Object getParam(String name) {
        return param.get(name);
    }

    public void addParam(String name, Object value) {
        param.put(name, value);
    }

}
