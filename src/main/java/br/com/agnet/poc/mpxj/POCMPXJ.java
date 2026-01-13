/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package br.com.agnet.poc.mpxj;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.DayOfWeek;
import java.util.Map;
import org.mpxj.MPXJException;
import org.mpxj.ProjectCalendar;
import org.mpxj.ProjectFile;
import org.mpxj.primavera.PrimaveraXERFileReader;

/**
 *
 * @author diego.costa
 */
public class POCMPXJ {

    public static void main(String[] args) throws FileNotFoundException, MPXJException {

        listaProjetos("C:\\Users\\diego.costa\\Downloads\\C1N12-2025-10-31-1.xer");
        lerProjetoId("C:\\Users\\diego.costa\\Downloads\\C1N12-2025-10-31-1.xer", 30729);

    }

    public static void listaProjetos(String path) throws FileNotFoundException, MPXJException {
        PrimaveraXERFileReader reader = new PrimaveraXERFileReader();
        FileInputStream is = new FileInputStream(path);
        Map<Integer, String> projects = reader.listProjects(is);
        System.out.println("ID\tName");
        for (Map.Entry<Integer, String> entry : projects.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
    }

    public static void lerProjetoId(String path, int projectId) throws MPXJException {
        PrimaveraXERFileReader reader = new PrimaveraXERFileReader();
        reader.setProjectID(projectId);
        ProjectFile file = reader.read(path);

        ProjectCalendar calendar = file.addDefaultBaseCalendar();
        System.out.println("The calendar name is " + calendar.getName());

        for (DayOfWeek day : DayOfWeek.values()) {
            String dayType = calendar.getCalendarDayType(day).toString();
            System.out.println(day + " is a " + dayType + " day");
        }

        
    }

}
