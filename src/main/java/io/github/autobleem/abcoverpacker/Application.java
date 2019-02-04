/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.autobleem.abcoverpacker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author artur.jakubowicz
 */
public class Application {

    public void unpack(String filename, String folder) throws ClassNotFoundException, SQLException, IOException {
        String fileWithPath = System.getProperty("user.dir") + File.separator + filename;
        String outputDir = System.getProperty("user.dir") + File.separator + folder;
        File f = new File(fileWithPath);
        if (!f.exists()) {
            System.out.println("CoverDB file does not exists");
            return;
        }

        File directory = new File(String.valueOf(outputDir));

        if (!directory.exists()) {
            directory.mkdir();
        }

        Class.forName("org.sqlite.JDBC"); // sqlite driver load
        String connectionStr = "jdbc:sqlite:./" + filename;
        Connection con = DriverManager.getConnection(connectionStr);
        String query = "select SERIAL,COVER from SERIALS S JOIN GAME G on G.ID = S.GAME;";
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
         
            String serial = rs.getString("SERIAL");
            System.out.println("Exporting:"+serial+".png");
            InputStream input = rs.getBinaryStream("COVER");
            Files.copy(input, Paths.get(outputDir+File.separator+serial+".png") , StandardCopyOption.REPLACE_EXISTING);
          

        }
        rs.close();
        ps.close();
        con.close();

    }
    
    private byte[] readFile(String file) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Files.copy(Paths.get(file), bos);
        return bos != null ? bos.toByteArray() : null;
      
    }

    public void pack(String filename, String folder) throws ClassNotFoundException, SQLException, IOException {
        String inputDir = System.getProperty("user.dir") + File.separator + folder;
        String fileWithPath = System.getProperty("user.dir") + File.separator + filename;
        File f = new File(fileWithPath);
        if (!f.exists()) {
            System.out.println("CoverDB file does not exists");
            return;
        }
        
        Class.forName("org.sqlite.JDBC"); // sqlite driver load
        String connectionStr = "jdbc:sqlite:./" + filename;
        Connection con = DriverManager.getConnection(connectionStr);
        File[] listOfFiles = new File(inputDir).listFiles();
        String query = "update GAME set COVER = ? where ID = (select GAME from SERIALS where SERIAL=?)";
        for (File file:listOfFiles)
        {
            String serial = file.getName().substring(0,file.getName().length()-4);
            System.out.println("Updating cover for:"+serial);
            PreparedStatement ps = con.prepareStatement(query);
            ps.setBytes(1, readFile(file.getAbsolutePath()));
            ps.setString(2, serial);
            ps.executeUpdate();
            ps.close();
            
        }
        con.close();
        
    }

    public void usage() {
        System.out.println("usage: java -jar ABCoverPacker.jar -u filename folder_name");
        System.out.println("        to unpack covers into folder");
        System.out.println("usage: java -jar ABCoverPacker.jar -p filename folder_name");
        System.out.println("        to pack covers in folder back into db");
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        if (args.length < 3) {
            new Application().usage();;
            return;
        }
        if ("-u".equals(args[0])) {
            new Application().unpack(args[1], args[2]);
            return;
        }
        if ("-p".equals(args[0])) {
            new Application().pack(args[1], args[2]);
            return;
        }
        new Application().usage();
    }

}
