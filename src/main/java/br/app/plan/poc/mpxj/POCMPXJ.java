package br.app.plan.poc.mpxj;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.mpxj.MPXJException;
import org.mpxj.ProjectCalendar;
import org.mpxj.ProjectFile;
import org.mpxj.ProjectProperties;
import org.mpxj.Task;
import org.mpxj.primavera.PrimaveraPMFileReader;

/**
 *
 * @author diego.costa
 */
public class POCMPXJ {

    public static void main(String[] args) throws FileNotFoundException, MPXJException {

        listaProjetos("C:\\Users\\diego.costa\\Downloads\\Cronograma Corrente.xml");
        lerProjetoId("C:\\Users\\diego.costa\\Downloads\\Cronograma Corrente.xml", 31537);
//        List<BaselineInfo> baselineIds = getBaselineIds("C:\\Users\\diego.costa\\Downloads\\Cronograma Corrente.xml", 31537);
//        for (BaselineInfo b : baselineIds){
//            System.out.println(b.baselineIndex + " - "+ b.getBaselineProjectName() + " - "+ b.baselineProjectUniqueId);
//        }
        List<BaselineInfo> ids = getBaselineIds("C:\\Users\\diego.costa\\Downloads\\Cronograma Corrente.xml", 31537);
        System.out.println("Baselines encontradas:");
        for (BaselineInfo bi : ids) {
            System.out.println(bi);
        }

// Ex: pegar as duas primeiras baselines
        List<Integer> baselineIds = new ArrayList<>();
        for (int i = 0; i < Math.min(2, ids.size()); i++) {
            baselineIds.add(ids.get(i).getBaselineIndex());
        }

        Map<Integer, List<BaselineTaskData>> data
                = getBaselineTasksByIds("C:\\Users\\diego.costa\\Downloads\\Cronograma Corrente.xml", 31537, baselineIds);

        System.out.println("Qtd tasks por baseline:");
        for (Map.Entry<Integer, List<BaselineTaskData>> e : data.entrySet()) {

            Integer baselineId = e.getKey();
            List<BaselineTaskData> tasks = e.getValue();

            System.out.println("===== BASELINE ID: " + baselineId + " =====");

            for (BaselineTaskData t : tasks) {
                if (t == null || t.getName() == null) {
                    continue;
                }

                System.out.println("Atividade: " + t.getName());
                System.out.println("Task UniqueID: " + t.getTaskUniqueId());
                System.out.println("Inicio BL: " + t.getStart());
                System.out.println("Fim BL: " + t.getFinish());
                System.out.println("Duração BL: " + t.getDuration());
                System.out.println("Custo BL: " + t.getCost());
                System.out.println("Custo BL: " + t.getCost());
                System.out.println("----------------------");
            }
        }

    }

    public static void listaProjetos(String path) throws FileNotFoundException, MPXJException {
        PrimaveraPMFileReader reader = new PrimaveraPMFileReader();
        FileInputStream is = new FileInputStream(path);
        Map<Integer, String> projects = reader.listProjects(is);
        System.out.println("ID\tName");
        for (Map.Entry<Integer, String> entry : projects.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
    }

    public static void lerProjetoId(String path, int projectId) throws MPXJException {
        PrimaveraPMFileReader reader = new PrimaveraPMFileReader();
//        reader.setProjectID(projectId);
        ProjectFile file = reader.read(path);

        ProjectCalendar calendar = file.addDefaultBaseCalendar();
        System.out.println("The calendar name is " + calendar.getName());

        for (DayOfWeek day : DayOfWeek.values()) {
            String dayType = calendar.getCalendarDayType(day).toString();
            System.out.println(day + " is a " + dayType + " day");
        }

    }

    public static List<BaselineInfo> getBaselineIds(String path, int projectId) throws MPXJException {
        PrimaveraPMFileReader reader = new PrimaveraPMFileReader();
        reader.setProjectID(projectId);

        ProjectFile project = reader.read(path);

        Map<Integer, ProjectFile> baselines = project.getBaselines();
        List<BaselineInfo> result = new ArrayList<>();

        if (baselines == null || baselines.isEmpty()) {
            return result; // vazio
        }

        for (Map.Entry<Integer, ProjectFile> entry : baselines.entrySet()) {
            Integer baselineIndex = entry.getKey();
            ProjectFile baselineProject = entry.getValue();

            if (baselineProject == null) {
                continue;
            }

            ProjectProperties props = baselineProject.getProjectProperties();
            Integer uniqueId = (props != null) ? props.getUniqueID() : null;
            String name = (props != null) ? props.getName() : null;

            result.add(new BaselineInfo(baselineIndex, uniqueId, name));
        }

        return result;
    }

    public static class BaselineInfo {

        private final int baselineIndex;            // <-- ID que você vai usar depois (chave do Map)
        private final Integer baselineProjectUniqueId; // UniqueID do projeto baseline (se vier preenchido)
        private final String baselineProjectName;

        public BaselineInfo(int baselineIndex, Integer baselineProjectUniqueId, String baselineProjectName) {
            this.baselineIndex = baselineIndex;
            this.baselineProjectUniqueId = baselineProjectUniqueId;
            this.baselineProjectName = baselineProjectName;
        }

        public int getBaselineIndex() {
            return baselineIndex;
        }

        public Integer getBaselineProjectUniqueId() {
            return baselineProjectUniqueId;
        }

        public String getBaselineProjectName() {
            return baselineProjectName;
        }

        @Override
        public String toString() {
            return "BaselineInfo{"
                    + "baselineIndex=" + baselineIndex
                    + ", baselineProjectUniqueId=" + baselineProjectUniqueId
                    + ", baselineProjectName='" + baselineProjectName + '\''
                    + '}';
        }
    }

    public static class BaselineTaskData {

        private final Integer taskUniqueId;
        private final String name;
        private final LocalDateTime start;
        private final LocalDateTime finish;
        private final Object duration; // MPXJ Duration (mantive como Object p/ evitar dor com import)
        private final Number cost;

        public BaselineTaskData(Integer taskUniqueId, String name, LocalDateTime start, LocalDateTime finish, Object duration, Number cost) {
            this.taskUniqueId = taskUniqueId;
            this.name = name;
            this.start = start;
            this.finish = finish;
            this.duration = duration;
            this.cost = cost;
        }

        public Integer getTaskUniqueId() {
            return taskUniqueId;
        }

        public String getName() {
            return name;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public LocalDateTime getFinish() {
            return finish;
        }

        public Object getDuration() {
            return duration;
        }

        public Number getCost() {
            return cost;
        }
    }

    public static Map<Integer, List<BaselineTaskData>> getBaselineTasksByIds(String path, int projectId, Collection<Integer> baselineIds)
            throws MPXJException {

        PrimaveraPMFileReader reader = new PrimaveraPMFileReader();
        reader.setProjectID(projectId);

        ProjectFile project = reader.read(path);

        Map<Integer, ProjectFile> baselines = project.getBaselines();
        Map<Integer, List<BaselineTaskData>> output = new LinkedHashMap<>();

        if (baselines == null || baselines.isEmpty() || baselineIds == null || baselineIds.isEmpty()) {
            return output;
        }

        for (Integer baselineId : baselineIds) {
            ProjectFile baselineProject = baselines.get(baselineId);

            if (baselineProject == null) {
                // baselineId não existe no arquivo
                output.put(baselineId, Collections.<BaselineTaskData>emptyList());
                continue;
            }

            List<BaselineTaskData> tasksData = new ArrayList<>();

            for (Task task : baselineProject.getTasks()) {
                if (task == null || task.getName() == null) {
                    continue;
                }

                tasksData.add(new BaselineTaskData(
                        task.getUniqueID(),
                        task.getName(),
                        task.getStart(),
                        task.getFinish(),
                        task.getDuration(),
                        task.getCost()
                ));
            }

            output.put(baselineId, tasksData);
        }

        return output;
    }

}
